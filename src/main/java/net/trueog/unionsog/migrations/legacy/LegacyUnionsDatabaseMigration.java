package net.trueog.unionsog.migrations.legacy;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Imports the legacy unions database schema into Unions-OG.
 * <p>
 * The importer is intentionally based only on the database schema. It never
 * changes the source tables, requires empty destination tables, and records a
 * successful import in a destination-owned history table.
 * </p>
 */
public final class LegacyUnionsDatabaseMigration {

    static final String SOURCE_UNIONS_TABLE = "unions_parties";
    static final String SOURCE_PLAYERS_TABLE = "unions_players";
    private static final String MIGRATION_ID = "legacy-unions-database-v1";
    private static final int MAX_TAG_LENGTH = 25;
    private static final int MAX_UNION_NAME_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 255;
    private static final int MAX_PLAYER_NAME_LENGTH = 16;
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z0-9_]+");
    private static final Pattern COLOR_CODE = Pattern.compile("(?i)[&\u00a7][0-9A-FK-OR]");
    private static final Pattern HEX_COLOR_CODE = Pattern
            .compile("(?i)(?:[&\u00a7]#[0-9A-F]{6}|[&\u00a7]x(?:[&\u00a7][0-9A-F]){6})");

    private static final Set<String> SOURCE_UNION_COLUMNS = Set.of("id", "name", "tag", "leader", "description", "motd",
            "color", "home");
    private static final Set<String> SOURCE_PLAYER_COLUMNS = Set.of("uuid", "party");

    private final Connection connection;
    private final String targetUnionsTable;
    private final String targetPlayersTable;
    private final String targetKillsTable;
    private final String historyTable;
    private final String ranksJson;
    private final Function<UUID, @Nullable String> playerNameResolver;
    private final long migrationTime;

    public LegacyUnionsDatabaseMigration(Connection connection, String targetTablePrefix, String ranksJson,
            Function<UUID, @Nullable String> playerNameResolver)
    {

        this.connection = Objects.requireNonNull(connection, "connection");
        Objects.requireNonNull(targetTablePrefix, "targetTablePrefix");
        this.ranksJson = Objects.requireNonNull(ranksJson, "ranksJson");
        this.playerNameResolver = Objects.requireNonNull(playerNameResolver, "playerNameResolver");
        validateTargetTablePrefix(targetTablePrefix);
        targetUnionsTable = validateIdentifier(targetTablePrefix + "clans");
        targetPlayersTable = validateIdentifier(targetTablePrefix + "players");
        targetKillsTable = validateIdentifier(targetTablePrefix + "kills");
        historyTable = validateIdentifier(targetTablePrefix + "unionsog_migrations");
        migrationTime = System.currentTimeMillis();

    }

    public static void validateTargetTablePrefix(String targetTablePrefix) {

        Objects.requireNonNull(targetTablePrefix, "targetTablePrefix");
        validateIdentifier(targetTablePrefix + "clans");
        final String playersTable = validateIdentifier(targetTablePrefix + "players");
        validateIdentifier(targetTablePrefix + "kills");
        validateIdentifier(targetTablePrefix + "unionsog_migrations");
        if (playersTable.equalsIgnoreCase(SOURCE_PLAYERS_TABLE)) {

            throw new IllegalArgumentException("The target table prefix makes the destination players table collide "
                    + "with the legacy source table. Use a different mysql.table_prefix.");

        }

    }

    /**
     * Executes the import once.
     *
     * @return a summary of the imported records
     * @throws SQLException when validation or import fails
     */
    public Result migrate() throws SQLException {

        validateColumns(targetUnionsTable,
                Set.of("verified", "tag", "color_tag", "name", "description", "friendly_fire", "founded", "last_used",
                        "packed_allies", "packed_rivals", "packed_bb", "cape_url", "flags", "balance", "fee_enabled",
                        "fee_value", "ranks", "banner"));
        validateColumns(targetPlayersTable,
                Set.of("uuid", "name", "leader", "tag", "friendly_fire", "neutral_kills", "rival_kills",
                        "civilian_kills", "ally_kills", "deaths", "last_seen", "join_date", "trusted", "flags",
                        "packed_past_clans", "resign_times", "locale"));
        validateColumns(targetKillsTable, Set.of("kill_id"));
        createHistoryTable();

        if (wasApplied()) {

            return Result.previouslyApplied();

        }

        requireTransactionalDestination();
        validateColumns(SOURCE_UNIONS_TABLE, SOURCE_UNION_COLUMNS);
        validateColumns(SOURCE_PLAYERS_TABLE, SOURCE_PLAYER_COLUMNS);

        final boolean originalAutoCommit = connection.getAutoCommit();
        try {

            connection.setAutoCommit(false);
            // Reserving the migration id first serializes concurrent startup attempts.
            // The row is rolled back with the imported data if anything fails.
            recordApplied();
            requireEmptyTarget();
            final List<LegacyUnion> legacyUnions = readLegacyUnions();
            final List<LegacyPlayer> legacyPlayers = readLegacyPlayers();
            final TagAssignment tagAssignment = assignTags(legacyUnions);
            final NameAssignment nameAssignment = assignPlayerNames(legacyPlayers);

            insertUnions(legacyUnions, tagAssignment.tags());
            final PlayerImportCounts playerCounts = insertPlayers(legacyPlayers, legacyUnions, tagAssignment.tags(),
                    nameAssignment.names());
            connection.commit();

            return new Result(false, legacyUnions.size(), legacyPlayers.size(), nameAssignment.placeholderCount(),
                    tagAssignment.adjustedCount(), playerCounts.orphanedMemberships(),
                    playerCounts.unionsWithoutLeaders());

        } catch (SQLException | RuntimeException ex) {

            connection.rollback();
            throw ex;

        } finally {

            connection.setAutoCommit(originalAutoCommit);

        }

    }

    private void createHistoryTable() throws SQLException {

        final String sql = "CREATE TABLE IF NOT EXISTS " + quote(historyTable) + " ("
                + " `migration` varchar(100) NOT NULL," + " `applied_at` bigint NOT NULL,"
                + " PRIMARY KEY (`migration`)" + ") ENGINE=InnoDB;";
        try (Statement statement = connection.createStatement()) {

            statement.execute(sql);

        }

    }

    private boolean wasApplied() throws SQLException {

        final String sql = "SELECT 1 FROM " + quote(historyTable) + " WHERE `migration` = ?;";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, MIGRATION_ID);
            try (ResultSet result = statement.executeQuery()) {

                return result.next();

            }

        }

    }

    private void recordApplied() throws SQLException {

        final String sql = "INSERT INTO " + quote(historyTable) + " (`migration`, `applied_at`) VALUES (?, ?);";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, MIGRATION_ID);
            statement.setLong(2, migrationTime);
            statement.executeUpdate();

        }

    }

    private void requireEmptyTarget() throws SQLException {

        final long unions = countRows(targetUnionsTable);
        final long players = countRows(targetPlayersTable);
        final long kills = countRows(targetKillsTable);
        if (unions != 0 || players != 0 || kills != 0) {

            throw new SQLException(String.format("Legacy database migration requires empty destination tables "
                    + "(found %d union(s), %d player(s), and %d kill(s)).", unions, players, kills));

        }

    }

    private void requireTransactionalDestination() throws SQLException {

        if (!connection.getMetaData().supportsTransactions()) {

            throw new SQLException("Legacy database migration requires a transaction-capable MySQL connection.");

        }

        // The plugin uses MySQL/MariaDB in production. Other transactional JDBC
        // databases are accepted here so the complete migration can be tested
        // in-memory.
        final String databaseProduct = connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT);
        if (!databaseProduct.contains("mysql") && !databaseProduct.contains("mariadb")) {

            return;

        }

        final String sql = "SELECT `ENGINE` FROM `information_schema`.`TABLES`"
                + " WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = ?;";
        for (final String table : List.of(targetUnionsTable, targetPlayersTable, targetKillsTable, historyTable)) {

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, table);
                try (ResultSet result = statement.executeQuery()) {

                    if (!result.next() || !"InnoDB".equalsIgnoreCase(result.getString("ENGINE"))) {

                        throw new SQLException("Legacy database migration requires the destination table `" + table
                                + "` to use the transactional InnoDB engine.");

                    }

                }

            }

        }

    }

    private long countRows(String table) throws SQLException {

        try (Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM " + quote(table) + ";"))
        {

            result.next();
            return result.getLong(1);

        }

    }

    private List<LegacyUnion> readLegacyUnions() throws SQLException {

        final String sql = "SELECT `id`, `name`, `tag`, `leader`, `description`, `motd`, `color`, `home` FROM "
                + quote(SOURCE_UNIONS_TABLE) + " ORDER BY `id`;";
        final List<LegacyUnion> unions = new ArrayList<>();
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sql)) {

            while (result.next()) {

                final UUID id = requiredUuid(result.getString("id"), SOURCE_UNIONS_TABLE + ".id");
                final UUID leader = optionalUuid(result.getString("leader"), SOURCE_UNIONS_TABLE + ".leader");
                unions.add(new LegacyUnion(id, result.getString("name"), result.getString("tag"), leader,
                        result.getString("description"), result.getString("motd"), result.getString("color"),
                        result.getString("home")));

            }

        }

        return unions;

    }

    private List<LegacyPlayer> readLegacyPlayers() throws SQLException {

        final String sql = "SELECT `uuid`, `party` FROM " + quote(SOURCE_PLAYERS_TABLE) + " ORDER BY `uuid`;";
        final List<LegacyPlayer> players = new ArrayList<>();
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sql)) {

            while (result.next()) {

                final UUID uuid = requiredUuid(result.getString("uuid"), SOURCE_PLAYERS_TABLE + ".uuid");
                final UUID unionId = optionalUuid(result.getString("party"), SOURCE_PLAYERS_TABLE + ".party");
                players.add(new LegacyPlayer(uuid, unionId));

            }

        }

        return players;

    }

    private void insertUnions(List<LegacyUnion> unions, Map<UUID, String> tags) throws SQLException {

        final String sql = "INSERT INTO " + quote(targetUnionsTable)
                + " (`verified`, `tag`, `color_tag`, `name`, `description`, `friendly_fire`, `founded`,"
                + " `last_used`, `packed_allies`, `packed_rivals`, `packed_bb`, `cape_url`, `flags`, `balance`,"
                + " `fee_enabled`, `fee_value`, `ranks`, `banner`) VALUES"
                + " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            for (LegacyUnion union : unions) {

                final String displayTag = tags.get(union.id());
                statement.setInt(1, 1);
                statement.setString(2, displayTag.toLowerCase(Locale.ROOT));
                statement.setString(3, coloredTag(union.color(), displayTag));
                statement.setString(4, truncate(nonBlankOr(union.name(), displayTag), MAX_UNION_NAME_LENGTH));
                statement.setString(5, truncateNullable(union.description(), MAX_DESCRIPTION_LENGTH));
                statement.setInt(6, 0);
                statement.setLong(7, migrationTime);
                statement.setLong(8, migrationTime);
                statement.setString(9, "");
                statement.setString(10, "");
                statement.setString(11, packedBulletin(union.motd()));
                statement.setString(12, "");
                statement.setString(13, homeFlags(union.home()));
                statement.setDouble(14, 0);
                statement.setInt(15, 0);
                statement.setDouble(16, 0);
                statement.setString(17, ranksJson);
                statement.setNull(18, Types.LONGVARCHAR);
                statement.addBatch();

            }

            statement.executeBatch();

        }

    }

    private PlayerImportCounts insertPlayers(List<LegacyPlayer> players, List<LegacyUnion> unions,
            Map<UUID, String> tags, Map<UUID, String> names) throws SQLException
    {

        final Map<UUID, LegacyUnion> unionsById = new HashMap<>();
        for (LegacyUnion union : unions) {

            unionsById.put(union.id(), union);

        }

        final Set<UUID> unionsWithLeaders = new HashSet<>();
        int orphanedMemberships = 0;
        final String sql = "INSERT INTO " + quote(targetPlayersTable)
                + " (`uuid`, `name`, `leader`, `tag`, `friendly_fire`, `neutral_kills`, `rival_kills`,"
                + " `civilian_kills`, `ally_kills`, `deaths`, `last_seen`, `join_date`, `trusted`, `flags`,"
                + " `packed_past_clans`, `resign_times`, `locale`) VALUES"
                + " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            for (LegacyPlayer player : players) {

                final LegacyUnion union = unionsById.get(player.unionId());
                final boolean orphaned = player.unionId() != null && union == null;
                if (orphaned) {

                    orphanedMemberships++;

                }

                final boolean leader = union != null && player.uuid().equals(union.leader());
                if (leader) {

                    unionsWithLeaders.add(union.id());

                }

                final String tag = union != null ? tags.get(union.id()).toLowerCase(Locale.ROOT) : "";

                statement.setString(1, player.uuid().toString());
                statement.setString(2, names.get(player.uuid()));
                statement.setInt(3, leader ? 1 : 0);
                statement.setString(4, tag);
                statement.setInt(5, 0);
                statement.setInt(6, 0);
                statement.setInt(7, 0);
                statement.setInt(8, 0);
                statement.setInt(9, 0);
                statement.setInt(10, 0);
                statement.setLong(11, migrationTime);
                statement.setLong(12, migrationTime);
                statement.setInt(13, leader ? 1 : 0);
                statement.setString(14, "{}");
                statement.setString(15, "");
                statement.setString(16, "{}");
                statement.setNull(17, Types.VARCHAR);
                statement.addBatch();

            }

            statement.executeBatch();

        }

        return new PlayerImportCounts(orphanedMemberships, unions.size() - unionsWithLeaders.size());

    }

    private TagAssignment assignTags(List<LegacyUnion> unions) {

        final Map<UUID, String> tags = new LinkedHashMap<>();
        final Set<String> used = new HashSet<>();
        int adjusted = 0;
        for (LegacyUnion union : unions) {

            final String base = normalizedTag(union.tag(), union.name(), union.id());
            final String assigned = uniqueTag(base, union.id(), used);
            if (!assigned.equals(base)) {

                adjusted++;

            }

            tags.put(union.id(), assigned);

        }

        return new TagAssignment(tags, adjusted);

    }

    private NameAssignment assignPlayerNames(List<LegacyPlayer> players) {

        final Map<UUID, String> names = new LinkedHashMap<>();
        final Set<String> used = new HashSet<>();
        int placeholders = 0;
        for (LegacyPlayer player : players) {

            String resolved = null;
            try {

                resolved = playerNameResolver.apply(player.uuid());

            } catch (RuntimeException ignored) {

                // A missing or unavailable server profile cache must not lose membership.

            }

            if (!isUsablePlayerName(resolved) || !used.add(resolved.toLowerCase(Locale.ROOT))) {

                resolved = uniquePlaceholderName(player.uuid(), used);
                placeholders++;

            }

            names.put(player.uuid(), resolved);

        }

        return new NameAssignment(names, placeholders);

    }

    private void validateColumns(String table, Set<String> requiredColumns) throws SQLException {

        final Set<String> available = new HashSet<>();
        final String sql = "SELECT * FROM " + quote(validateIdentifier(table)) + " WHERE 1 = 0;";
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sql)) {

            final ResultSetMetaData metadata = result.getMetaData();
            for (int i = 1; i <= metadata.getColumnCount(); i++) {

                available.add(metadata.getColumnLabel(i).toLowerCase(Locale.ROOT));

            }

        } catch (SQLException ex) {

            throw new SQLException("Required migration table `" + table + "` is missing or unreadable.", ex);

        }

        final Set<String> missing = new TreeSet<>(requiredColumns);
        missing.removeAll(available);
        if (!missing.isEmpty()) {

            throw new SQLException(
                    "Table `" + table + "` is missing required column(s): " + String.join(", ", missing));

        }

    }

    static String normalizedTag(@Nullable String preferredTag, @Nullable String unionName, UUID unionId) {

        String candidate = nonBlankOr(preferredTag, unionName);
        candidate = candidate == null ? "" : stripColors(candidate);
        final StringBuilder normalized = new StringBuilder();
        for (int i = 0; i < candidate.length(); i++) {

            final char character = candidate.charAt(i);
            if ((character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z')
                    || (character >= '0' && character <= '9'))
            {

                normalized.append(character);

            }

        }

        if (normalized.length() == 0) {

            normalized.append("union");
            normalized.append(uuidCharacters(unionId).substring(0, 8));

        }

        return truncate(normalized.toString(), MAX_TAG_LENGTH);

    }

    static String uniqueTag(String base, UUID unionId, Set<String> usedTags) {

        // Tags keep the capitalization of the legacy union while uniqueness is
        // decided case-insensitively, matching how the plugin matches tags.
        if (usedTags.add(base.toLowerCase(Locale.ROOT))) {

            return base;

        }

        final String uuid = uuidCharacters(unionId);
        for (int suffixLength = 8; suffixLength <= Math.min(uuid.length(), MAX_TAG_LENGTH); suffixLength += 4) {

            final String suffix = uuid.substring(0, suffixLength);
            final String candidate = truncate(base, MAX_TAG_LENGTH - suffix.length()) + suffix;
            if (usedTags.add(candidate.toLowerCase(Locale.ROOT))) {

                return candidate;

            }

        }

        throw new IllegalStateException("Could not derive a unique tag for legacy union " + unionId);

    }

    static String coloredTag(@Nullable String colorName, String tag) {

        final String code;
        final String normalizedColor = colorName == null ? ""
                : colorName.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "");
        switch (normalizedColor) {

            case "black":
                code = "0";
                break;
            case "darkblue":
                code = "1";
                break;
            case "darkgreen":
                code = "2";
                break;
            case "darkaqua":
                code = "3";
                break;
            case "darkred":
                code = "4";
                break;
            case "darkpurple":
                code = "5";
                break;
            case "gold":
                code = "6";
                break;
            case "gray":
            case "grey":
                code = "7";
                break;
            case "darkgray":
            case "darkgrey":
                code = "8";
                break;
            case "blue":
                code = "9";
                break;
            case "green":
                code = "a";
                break;
            case "aqua":
                code = "b";
                break;
            case "red":
                code = "c";
                break;
            case "lightpurple":
                code = "d";
                break;
            case "yellow":
                code = "e";
                break;
            case "white":
                code = "f";
                break;
            default:
                return tag;

        }

        return "&" + code + tag;

    }

    static String fallbackPlayerName(UUID uuid) {

        return uuidCharacters(uuid).substring(0, MAX_PLAYER_NAME_LENGTH);

    }

    static String homeFlags(@Nullable String packedHome) {

        final JsonObject flags = new JsonObject();
        if (packedHome == null || packedHome.trim().isEmpty()) {

            return flags.toString();

        }

        final String[] parts = packedHome.split(",", -1);
        if (parts.length < 7 || parts[1].trim().isEmpty()) {

            return flags.toString();

        }

        try {

            final double x = finiteDouble(parts[2]);
            final double y = finiteDouble(parts[3]);
            final double z = finiteDouble(parts[4]);
            final double yaw = finiteDouble(parts[5]);
            final double pitch = finiteDouble(parts[6]);
            flags.addProperty("homeWorld", parts[1].trim());
            flags.addProperty("homeX", x);
            flags.addProperty("homeY", y);
            flags.addProperty("homeZ", z);
            flags.addProperty("homeYaw", yaw);
            flags.addProperty("homePitch", pitch);

        } catch (IllegalArgumentException ignored) {

            return "{}";

        }

        return flags.toString();

    }

    private String packedBulletin(@Nullable String motd) {

        if (motd == null || motd.trim().isEmpty()) {

            return "";

        }

        return migrationTime + "_" + motd.replace('|', '\u00a6');

    }

    private static boolean isUsablePlayerName(@Nullable String playerName) {

        if (playerName == null || playerName.trim().isEmpty() || playerName.length() > MAX_PLAYER_NAME_LENGTH) {

            return false;

        }

        for (int i = 0; i < playerName.length(); i++) {

            if (Character.isISOControl(playerName.charAt(i))) {

                return false;

            }

        }

        return true;

    }

    private static double finiteDouble(String value) {

        final double parsed = Double.parseDouble(value.trim());
        if (!Double.isFinite(parsed)) {

            throw new IllegalArgumentException("Non-finite coordinate");

        }

        return parsed;

    }

    private static String uniquePlaceholderName(UUID uuid, Set<String> usedNames) {

        final String characters = uuidCharacters(uuid);
        for (int start = 0; start <= characters.length() - MAX_PLAYER_NAME_LENGTH; start++) {

            final String candidate = characters.substring(start, start + MAX_PLAYER_NAME_LENGTH);
            if (usedNames.add(candidate.toLowerCase(Locale.ROOT))) {

                return candidate;

            }

        }

        throw new IllegalStateException("Could not derive a unique placeholder name for player " + uuid);

    }

    private static UUID requiredUuid(@Nullable String value, String field) throws SQLException {

        final UUID uuid = optionalUuid(value, field);
        if (uuid == null) {

            throw new SQLException("Required UUID field `" + field + "` is null or empty.");

        }

        return uuid;

    }

    private static @Nullable UUID optionalUuid(@Nullable String value, String field) throws SQLException {

        if (value == null || value.trim().isEmpty()) {

            return null;

        }

        try {

            return UUID.fromString(value.trim());

        } catch (IllegalArgumentException ex) {

            throw new SQLException("Invalid UUID in `" + field + "`: " + value, ex);

        }

    }

    private static String validateIdentifier(String identifier) {

        if (!IDENTIFIER.matcher(identifier).matches() || identifier.length() > 64) {

            throw new IllegalArgumentException("Unsafe or unsupported SQL identifier: " + identifier);

        }

        return identifier;

    }

    private static String quote(String identifier) {

        return "`" + validateIdentifier(identifier) + "`";

    }

    private static String stripColors(String value) {

        return COLOR_CODE.matcher(HEX_COLOR_CODE.matcher(value).replaceAll("")).replaceAll("");

    }

    private static String uuidCharacters(UUID uuid) {

        return uuid.toString().replace("-", "");

    }

    private static @Nullable String nonBlankOr(@Nullable String preferred, @Nullable String fallback) {

        if (preferred != null && !preferred.trim().isEmpty()) {

            return preferred.trim();

        }

        return fallback == null ? null : fallback.trim();

    }

    private static String truncate(String value, int maxLength) {

        return value.length() <= maxLength ? value : value.substring(0, maxLength);

    }

    private static @Nullable String truncateNullable(@Nullable String value, int maxLength) {

        return value == null ? null : truncate(value, maxLength);

    }

    public record Result(boolean alreadyApplied, int unions, int players, int placeholderNames, int adjustedTags,
            int orphanedMemberships, int unionsWithoutLeaders)
    {

        private static Result previouslyApplied() {

            return new Result(true, 0, 0, 0, 0, 0, 0);

        }

    }

    private record LegacyUnion(UUID id, @Nullable String name, @Nullable String tag, @Nullable UUID leader,
            @Nullable String description, @Nullable String motd, @Nullable String color, @Nullable String home)
    {

    }

    private record LegacyPlayer(UUID uuid, @Nullable UUID unionId) {

    }

    private record TagAssignment(Map<UUID, String> tags, int adjustedCount) {

    }

    private record NameAssignment(Map<UUID, String> names, int placeholderCount) {

    }

    private record PlayerImportCounts(int orphanedMemberships, int unionsWithoutLeaders) {

    }

}
