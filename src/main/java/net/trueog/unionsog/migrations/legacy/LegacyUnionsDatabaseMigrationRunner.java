package net.trueog.unionsog.migrations.legacy;

import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.managers.SettingsManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Function;

import static net.trueog.unionsog.managers.SettingsManager.ConfigField.MYSQL_ENABLE;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.MYSQL_MIGRATE_LEGACY_UNIONS_DATABASE;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.MYSQL_TABLE_PREFIX;

/**
 * Contains the temporary plugin lifecycle integration for the legacy database
 * migration.
 */
public final class LegacyUnionsDatabaseMigrationRunner {

    private LegacyUnionsDatabaseMigrationRunner() {

    }

    public static void validateConfiguration(UnionsOG plugin) {

        final SettingsManager settings = plugin.getSettingsManager();
        if (!settings.is(MYSQL_ENABLE) || !settings.is(MYSQL_MIGRATE_LEGACY_UNIONS_DATABASE)) {

            return;

        }

        try {

            LegacyUnionsDatabaseMigration.validateTargetTablePrefix(settings.getString(MYSQL_TABLE_PREFIX));

        } catch (RuntimeException ex) {

            throw new MigrationStartupException(
                    "Legacy unions database migration configuration is invalid. Plugin "
                            + "startup was aborted so the destination database cannot be populated before a retry.",
                    ex);

        }

    }

    public static void run(UnionsOG plugin, Connection connection) {

        final SettingsManager settings = plugin.getSettingsManager();
        if (!settings.is(MYSQL_MIGRATE_LEGACY_UNIONS_DATABASE)) {

            return;

        }

        if (!settings.is(MYSQL_ENABLE)) {

            throw new MigrationStartupException("Legacy unions database migration requires mysql.enable to be true. "
                    + "Plugin startup was aborted so the destination database cannot be populated before a retry.");

        }

        if (connection == null) {

            throw new MigrationStartupException("Legacy unions database migration could not open the MySQL "
                    + "connection. Plugin startup was aborted so the destination database cannot be populated before "
                    + "a retry.");

        }

        plugin.getLogger().info("Legacy unions database migration starting.");

        final LegacyUnionsDatabaseMigration.Result result = migrateOrThrow(connection,
                settings.getString(MYSQL_TABLE_PREFIX), uuid -> plugin.getServer().getOfflinePlayer(uuid).getName());
        logResult(plugin, result);
        settings.set(MYSQL_MIGRATE_LEGACY_UNIONS_DATABASE, false);
        settings.save();

    }

    static LegacyUnionsDatabaseMigration.Result migrateOrThrow(Connection connection, String targetTablePrefix,
            Function<UUID, String> playerNameResolver)
    {

        try {

            return new LegacyUnionsDatabaseMigration(connection, targetTablePrefix, playerNameResolver).migrate();

        } catch (SQLException | RuntimeException ex) {

            throw new MigrationStartupException("Legacy unions database migration failed. Plugin startup was aborted "
                    + "so the destination database cannot be populated before a retry.", ex);

        }

    }

    private static void logResult(UnionsOG plugin, LegacyUnionsDatabaseMigration.Result result) {

        if (result.alreadyApplied()) {

            plugin.getLogger().info("Legacy unions database migration was already applied.");
            return;

        }

        plugin.getLogger().info(String.format("Legacy unions database migration imported %d union(s) and %d player(s).",
                result.unions(), result.players()));
        if (result.placeholderNames() > 0) {

            plugin.getLogger().warning(String.format("%d migrated player name(s) were unavailable in the server "
                    + "profile cache. Stable placeholders were used and will be replaced when those players next "
                    + "join.", result.placeholderNames()));

        }

        if (result.adjustedTags() > 0) {

            plugin.getLogger().warning(
                    String.format("%d duplicate union tag(s) received deterministic suffixes.", result.adjustedTags()));

        }

        if (result.orphanedMemberships() > 0) {

            plugin.getLogger().warning(String.format("%d player membership(s) referenced a missing union and were "
                    + "imported without an active union.", result.orphanedMemberships()));

        }

    }

    public static final class MigrationStartupException extends IllegalStateException {

        private static final long serialVersionUID = 1L;

        private MigrationStartupException(String message) {

            super(message);

        }

        private MigrationStartupException(String message, Throwable cause) {

            super(message, cause);

        }

    }

}
