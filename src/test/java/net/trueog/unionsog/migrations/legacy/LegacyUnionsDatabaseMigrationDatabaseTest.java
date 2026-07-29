package net.trueog.unionsog.migrations.legacy;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyUnionsDatabaseMigrationDatabaseTest {

    private static final String RANKS_JSON = "{\"ranks\":[],\"defaultRank\":null}";
    private Connection connection;

    @BeforeEach
    void createDatabase() throws SQLException {

        final String database = "legacy_migration_" + UUID.randomUUID().toString().replace("-", "");
        connection = DriverManager
                .getConnection("jdbc:h2:mem:" + database + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        createSourceTables();
        createDestinationTables();

    }

    @AfterEach
    void closeDatabase() throws SQLException {

        connection.close();

    }

    @Test
    void migratesLegacyRowsAndRecordsIdempotency() throws SQLException {

        final UUID firstUnion = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final UUID secondUnion = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        final UUID leader = UUID.fromString("11111111-2222-3333-4444-555555555555");
        final UUID member = UUID.fromString("22222222-3333-4444-5555-666666666666");
        final UUID orphan = UUID.fromString("33333333-4444-5555-6666-777777777777");
        final UUID missingUnion = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

        insertLegacyUnion(firstUnion, "Alpha Union", "&4Alpha-Tag", leader, "First description", "Legacy|MOTD",
                "dark_red", "default,world,-12.5,64,23.25,90,10");
        insertLegacyUnion(secondUnion, "Second Union", "Alpha Tag", null, null, null, "yellow", null);
        insertLegacyPlayer(leader, firstUnion);
        insertLegacyPlayer(member, secondUnion);
        insertLegacyPlayer(orphan, missingUnion);

        final Map<UUID, String> playerNames = Map.of(leader, "Alice", orphan, "Orphan");
        final LegacyUnionsDatabaseMigration.Result result = migration(playerNames).migrate();

        assertFalse(result.alreadyApplied());
        assertEquals(2, result.unions());
        assertEquals(3, result.players());
        assertEquals(1, result.placeholderNames());
        assertEquals(1, result.adjustedTags());
        assertEquals(1, result.orphanedMemberships());
        assertEquals(1, result.unionsWithoutLeaders());

        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT `color_tag`, `description`, `packed_bb`, `flags`, `balance` FROM `sc_clans` WHERE `tag` = ?;"))
        {

            statement.setString(1, "alphatag");
            try (ResultSet row = statement.executeQuery()) {

                assertTrue(row.next());
                assertEquals("&4alphatag", row.getString("color_tag"));
                assertEquals("First description", row.getString("description"));
                assertTrue(row.getString("packed_bb").matches("\\d+_Legacy\u00a6MOTD"));
                assertEquals(0, row.getDouble("balance"));

                final JsonObject flags = JsonParser.parseString(row.getString("flags")).getAsJsonObject();
                assertEquals("world", flags.get("homeWorld").getAsString());
                assertEquals(-12.5, flags.get("homeX").getAsDouble());
                assertEquals(64, flags.get("homeY").getAsDouble());

            }

        }

        assertEquals(1, countRows("sc_clans", "`tag` = 'alphatagaaaaaaaa'"));
        assertPlayer(leader, "Alice", "alphatag", true, true);
        assertPlayer(member, "2222222233334444", "alphatagaaaaaaaa", false, false);
        assertPlayer(orphan, "Orphan", "", false, false);
        assertEquals(1, countRows("sc_unionsog_migrations", null));

        final LegacyUnionsDatabaseMigration.Result repeated = migration(playerNames).migrate();
        assertTrue(repeated.alreadyApplied());
        assertEquals(2, countRows("sc_clans", null));
        assertEquals(3, countRows("sc_players", null));
        assertEquals(1, countRows("sc_unionsog_migrations", null));

    }

    @Test
    void rollsBackAllDestinationRowsWhenAnInsertFails() throws SQLException {

        try (Statement statement = connection.createStatement()) {

            statement.execute(
                    "ALTER TABLE `sc_players` ADD CONSTRAINT `reject_blocked_name` CHECK (`name` <> 'Blocked');");

        }

        final UUID union = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final UUID player = UUID.fromString("11111111-2222-3333-4444-555555555555");
        insertLegacyUnion(union, "Alpha Union", "alpha", player, null, null, null, null);
        insertLegacyPlayer(player, union);

        assertThrows(SQLException.class, () -> migration(Map.of(player, "Blocked")).migrate());

        assertEquals(0, countRows("sc_clans", null));
        assertEquals(0, countRows("sc_players", null));
        assertEquals(0, countRows("sc_unionsog_migrations", null));
        assertEquals(1, countRows("unions_parties", null));
        assertEquals(1, countRows("unions_players", null));
        assertTrue(connection.getAutoCommit());

    }

    @Test
    void refusesToOverwritePopulatedDestinationAndRollsBackReservation() throws SQLException {

        try (Statement statement = connection.createStatement()) {

            statement.executeUpdate("INSERT INTO `sc_clans` (`tag`) VALUES ('existing');");

        }

        final SQLException failure = assertThrows(SQLException.class, () -> migration(Map.of()).migrate());

        assertTrue(failure.getMessage().contains("requires empty destination tables"));
        assertEquals(1, countRows("sc_clans", "`tag` = 'existing'"));
        assertEquals(0, countRows("sc_unionsog_migrations", null));
        assertTrue(connection.getAutoCommit());

    }

    @Test
    void wrapsDatabaseFailureToAbortPluginStartup() throws SQLException {

        try (Statement statement = connection.createStatement()) {

            statement.execute("DROP TABLE `unions_players`;");

        }

        final LegacyUnionsDatabaseMigrationRunner.MigrationStartupException failure = assertThrows(
                LegacyUnionsDatabaseMigrationRunner.MigrationStartupException.class,
                () -> LegacyUnionsDatabaseMigrationRunner.migrateOrThrow(connection, "sc_", RANKS_JSON, uuid -> null));

        assertTrue(failure.getMessage().contains("Plugin startup was aborted"));
        assertTrue(failure.getCause() instanceof SQLException);

    }

    private LegacyUnionsDatabaseMigration migration(Map<UUID, String> playerNames) {

        return new LegacyUnionsDatabaseMigration(connection, "sc_", RANKS_JSON, playerNames::get);

    }

    private void createSourceTables() throws SQLException {

        try (Statement statement = connection.createStatement()) {

            statement.execute("""
                    CREATE TABLE `unions_parties` (
                        `id` varchar(36) PRIMARY KEY,
                        `name` varchar(255),
                        `tag` varchar(255),
                        `leader` varchar(36),
                        `description` varchar(255),
                        `motd` varchar(500),
                        `color` varchar(255),
                        `home` varchar(255)
                    );
                    """);
            statement.execute("""
                    CREATE TABLE `unions_players` (
                        `uuid` varchar(36) PRIMARY KEY,
                        `party` varchar(36)
                    );
                    """);

        }

    }

    private void createDestinationTables() throws SQLException {

        try (Statement statement = connection.createStatement()) {

            statement.execute("""
                    CREATE TABLE `sc_clans` (
                        `id` bigint AUTO_INCREMENT PRIMARY KEY,
                        `verified` tinyint,
                        `tag` varchar(25) UNIQUE,
                        `color_tag` varchar(255),
                        `name` varchar(100),
                        `description` varchar(255),
                        `friendly_fire` tinyint,
                        `founded` bigint,
                        `last_used` bigint,
                        `packed_allies` clob,
                        `packed_rivals` clob,
                        `packed_bb` clob,
                        `cape_url` varchar(255),
                        `flags` clob,
                        `balance` double,
                        `fee_enabled` tinyint,
                        `fee_value` double,
                        `ranks` clob,
                        `banner` clob
                    );
                    """);
            statement.execute("""
                    CREATE TABLE `sc_players` (
                        `id` bigint AUTO_INCREMENT PRIMARY KEY,
                        `uuid` varchar(255) UNIQUE,
                        `name` varchar(16),
                        `leader` tinyint,
                        `tag` varchar(25),
                        `friendly_fire` tinyint,
                        `neutral_kills` int,
                        `rival_kills` int,
                        `civilian_kills` int,
                        `ally_kills` int,
                        `deaths` int,
                        `last_seen` bigint,
                        `join_date` bigint,
                        `trusted` tinyint,
                        `flags` clob,
                        `packed_past_clans` clob,
                        `resign_times` clob,
                        `locale` varchar(10)
                    );
                    """);
            statement.execute("""
                    CREATE TABLE `sc_kills` (
                        `kill_id` bigint AUTO_INCREMENT PRIMARY KEY
                    );
                    """);

        }

    }

    private void insertLegacyUnion(UUID id, String name, String tag, UUID leader, String description, String motd,
            String color, String home) throws SQLException
    {

        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO `unions_parties` (`id`, `name`, `tag`, `leader`, `description`, `motd`, `color`, `home`)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?);"))
        {

            statement.setString(1, id.toString());
            statement.setString(2, name);
            statement.setString(3, tag);
            statement.setString(4, leader == null ? null : leader.toString());
            statement.setString(5, description);
            statement.setString(6, motd);
            statement.setString(7, color);
            statement.setString(8, home);
            statement.executeUpdate();

        }

    }

    private void insertLegacyPlayer(UUID player, UUID union) throws SQLException {

        try (PreparedStatement statement = connection
                .prepareStatement("INSERT INTO `unions_players` (`uuid`, `party`) VALUES (?, ?);"))
        {

            statement.setString(1, player.toString());
            statement.setString(2, union == null ? null : union.toString());
            statement.executeUpdate();

        }

    }

    private void assertPlayer(UUID uuid, String name, String tag, boolean leader, boolean trusted) throws SQLException {

        try (PreparedStatement statement = connection
                .prepareStatement("SELECT `name`, `tag`, `leader`, `trusted` FROM `sc_players` WHERE `uuid` = ?;"))
        {

            statement.setString(1, uuid.toString());
            try (ResultSet row = statement.executeQuery()) {

                assertTrue(row.next());
                assertEquals(name, row.getString("name"));
                assertEquals(tag, row.getString("tag"));
                assertEquals(leader, row.getBoolean("leader"));
                assertEquals(trusted, row.getBoolean("trusted"));

            }

        }

    }

    private long countRows(String table, String condition) throws SQLException {

        final String where = condition == null ? "" : " WHERE " + condition;
        try (Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM `" + table + "`" + where + ";"))
        {

            result.next();
            return result.getLong(1);

        }

    }

}
