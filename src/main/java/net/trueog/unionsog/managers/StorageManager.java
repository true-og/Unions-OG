package net.trueog.unionsog.managers;

import net.trueog.unionsog.*;
import net.trueog.unionsog.events.UnionBalanceUpdateEvent;
import net.trueog.unionsog.loggers.BankLogger;
import net.trueog.unionsog.loggers.BankOperator;
import net.trueog.unionsog.migrations.legacy.LegacyUnionsDatabaseMigrationRunner;
import net.trueog.unionsog.storage.DBCore;
import net.trueog.unionsog.storage.MySQLCore;
import net.trueog.unionsog.storage.SQLiteCore;
import net.trueog.unionsog.utils.ChatUtils;
import net.trueog.unionsog.utils.YAMLSerializer;
import net.trueog.unionsog.uuid.UUIDFetcher;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;

/**
 * @author phaed
 */
public final class StorageManager {

    private final UnionsOG plugin;
    private DBCore core;
    private final HashMap<String, ChatBlock> chatBlocks = new HashMap<>();
    private final Set<Union> modifiedUnions = new HashSet<>();
    private final Set<UnionPlayer> modifiedUnionPlayers = new HashSet<>();

    /**
     *
     */
    public StorageManager() {

        plugin = UnionsOG.getInstance();
        LegacyUnionsDatabaseMigrationRunner.validateConfiguration(plugin);
        initiateDB();
        updateDatabase();
        try {

            LegacyUnionsDatabaseMigrationRunner.run(plugin, core.getConnection());

        } catch (LegacyUnionsDatabaseMigrationRunner.MigrationStartupException ex) {

            core.close();
            throw ex;

        }

        importFromDatabase();

    }

    /**
     * Retrieve a player's pending chat lines
     *
     * @param player the Player
     * @return the ChatBlock
     */
    public ChatBlock getChatBlock(Player player) {

        return chatBlocks.get(player.getName());

    }

    /**
     * Store pending chat lines for a player
     *
     */
    public void addChatBlock(CommandSender player, ChatBlock cb) {

        chatBlocks.put(player.getName(), cb);

    }

    /**
     * Initiates the db
     */
    public void initiateDB() {

        SettingsManager settings = plugin.getSettingsManager();
        if (settings.is(MYSQL_ENABLE)) {

            core = new MySQLCore(settings.getString(MYSQL_HOST), settings.getString(MYSQL_DATABASE),
                    settings.getInt(MYSQL_PORT), settings.getString(MYSQL_USERNAME),
                    settings.getString(MYSQL_PASSWORD));

            if (core.checkConnection()) {

                plugin.getLogger().info(lang("mysql.connection.successful"));

                if (!core.existsTable(getPrefixedTable("clans"))) {

                    plugin.getLogger().info("Creating table: " + getPrefixedTable("clans"));

                    String query = "CREATE TABLE IF NOT EXISTS `" + getPrefixedTable("clans") + "` ("
                            + " `id` bigint(20) NOT NULL auto_increment," + " `verified` tinyint(1) default '0',"
                            + " `tag` varchar(25) NOT NULL," + " `color_tag` varchar(255) NOT NULL,"
                            + " `name` varchar(100) NOT NULL," + " `description` varchar(255),"
                            + " `friendly_fire` tinyint(1) default '0'," + " `founded` bigint NOT NULL,"
                            + " `last_used` bigint NOT NULL," + " `packed_allies` text NOT NULL,"
                            + " `packed_rivals` text NOT NULL," + " `packed_bb` mediumtext NOT NULL,"
                            + " `cape_url` varchar(255) NOT NULL," + " `flags` text NOT NULL,"
                            + " `balance` double(64,2)," + " `fee_enabled` tinyint(1) default '0',"
                            + " `fee_value` double(64,2)," + " `ranks` text NOT NULL," + " `banner` text,"
                            + " PRIMARY KEY  (`id`)," + " UNIQUE KEY `uq_simpleclans_1` (`tag`));";
                    core.execute(query);

                }

                if (!core.existsTable(getPrefixedTable("players"))) {

                    plugin.getLogger().info("Creating table: " + getPrefixedTable("players"));

                    String query = "CREATE TABLE IF NOT EXISTS `" + getPrefixedTable("players") + "` ("
                            + " `id` bigint(20) NOT NULL auto_increment," + " `name` varchar(16) NOT NULL,"
                            + " `leader` tinyint(1) default '0'," + " `tag` varchar(25) NOT NULL,"
                            + " `friendly_fire` tinyint(1) default '0'," + " `neutral_kills` int(11) default NULL,"
                            + " `rival_kills` int(11) default NULL," + " `civilian_kills` int(11) default NULL,"
                            + " `ally_kills` int(11) default NULL," + " `deaths` int(11) default NULL,"
                            + " `last_seen` bigint NOT NULL," + " `join_date` bigint NOT NULL,"
                            + " `trusted` tinyint(1) default '0'," + " `flags` text NOT NULL,"
                            + " `packed_past_clans` text," + " `resign_times` text," + " `locale` varchar(10),"
                            + " PRIMARY KEY  (`id`)," + " UNIQUE KEY `uq_sc_players_1` (`name`));";
                    core.execute(query);

                }

                if (!core.existsTable(getPrefixedTable("kills"))) {

                    plugin.getLogger().info("Creating table: " + getPrefixedTable("kills"));

                    String query = "CREATE TABLE IF NOT EXISTS `" + getPrefixedTable("kills") + "` ("
                            + " `kill_id` bigint(20) NOT NULL auto_increment," + " `attacker` varchar(16) NOT NULL,"
                            + " `attacker_tag` varchar(16) NOT NULL," + " `victim` varchar(16) NOT NULL,"
                            + " `victim_tag` varchar(16) NOT NULL," + " `kill_type` varchar(1) NOT NULL,"
                            + " `created_at` datetime NULL," + " PRIMARY KEY  (`kill_id`));";
                    core.execute(query);

                }

                if (!core.existsTable(getPrefixedTable("proposals"))) {

                    plugin.getLogger().info("Creating table: " + getPrefixedTable("proposals"));

                    String query = "CREATE TABLE IF NOT EXISTS `" + getPrefixedTable("proposals") + "` ("
                            + " `tag` varchar(25) NOT NULL," + " `type` varchar(32) NOT NULL,"
                            + " `target` varchar(255) NOT NULL," + " `proposer` varchar(255) NOT NULL,"
                            + " `created_at` bigint NOT NULL," + " `votes` text NOT NULL," + " PRIMARY KEY  (`tag`));";
                    core.execute(query);

                }

            } else {

                plugin.getServer().getConsoleSender()
                        .sendMessage("[Unions-OG] " + ChatColor.RED + lang("mysql.connection.failed"));

            }

        } else {

            core = new SQLiteCore(plugin.getDataFolder().getPath());

            if (core.checkConnection()) {

                plugin.getLogger().info(lang("sqlite.connection.successful"));

                if (!core.existsTable(getPrefixedTable("clans"))) {

                    plugin.getLogger().info("Creating table: " + getPrefixedTable("clans"));

                    String query = "CREATE TABLE IF NOT EXISTS `" + getPrefixedTable("clans") + "` ("
                            + " `id` bigint(20)," + " `verified` tinyint(1) default '0',"
                            + " `tag` varchar(25) NOT NULL," + " `color_tag` varchar(255) NOT NULL,"
                            + " `name` varchar(100) NOT NULL," + " `description` varchar(255),"
                            + " `friendly_fire` tinyint(1) default '0'," + " `founded` bigint NOT NULL,"
                            + " `last_used` bigint NOT NULL," + " `packed_allies` text NOT NULL,"
                            + " `packed_rivals` text NOT NULL," + " `packed_bb` mediumtext NOT NULL,"
                            + " `cape_url` varchar(255) NOT NULL," + " `flags` text NOT NULL,"
                            + " `balance` double(64,2) default 0.0," + " `fee_enabled` tinyint(1) default '0',"
                            + " `fee_value` double(64,2)," + " `ranks` text NOT NULL," + " `banner` text,"
                            + "  PRIMARY KEY  (`id`)," + " UNIQUE (`tag`));";
                    core.execute(query);

                }

                if (!core.existsTable(getPrefixedTable("players"))) {

                    plugin.getLogger().info("Creating table: " + getPrefixedTable("players"));

                    String query = "CREATE TABLE IF NOT EXISTS `" + getPrefixedTable("players") + "` ("
                            + " `id` bigint(20)," + " `name` varchar(16) NOT NULL,"
                            + " `leader` tinyint(1) default '0'," + " `tag` varchar(25) NOT NULL,"
                            + " `friendly_fire` tinyint(1) default '0'," + " `neutral_kills` int(11) default NULL,"
                            + " `rival_kills` int(11) default NULL," + " `civilian_kills` int(11) default NULL,"
                            + " `ally_kills` int(11) default NULL," + " `deaths` int(11) default NULL,"
                            + " `last_seen` bigint NOT NULL," + " `join_date` bigint NOT NULL,"
                            + " `trusted` tinyint(1) default '0'," + " `flags` text NOT NULL,"
                            + " `packed_past_clans` text," + " `resign_times` text," + " `locale` varchar(10),"
                            + " PRIMARY KEY  (`id`)," + " UNIQUE (`name`));";
                    core.execute(query);

                }

                if (!core.existsTable(getPrefixedTable("kills"))) {

                    plugin.getLogger().info("Creating table: " + getPrefixedTable("kills"));

                    String query = "CREATE TABLE IF NOT EXISTS `" + getPrefixedTable("kills") + "` ("
                            + " `kill_id` bigint(20)," + " `attacker` varchar(16) NOT NULL,"
                            + " `attacker_tag` varchar(16) NOT NULL," + " `victim` varchar(16) NOT NULL,"
                            + " `victim_tag` varchar(16) NOT NULL," + " `kill_type` varchar(1) NOT NULL,"
                            + " `created_at` datetime NULL," + " PRIMARY KEY  (`kill_id`));";
                    core.execute(query);

                }

                if (!core.existsTable(getPrefixedTable("proposals"))) {

                    plugin.getLogger().info("Creating table: " + getPrefixedTable("proposals"));

                    String query = "CREATE TABLE IF NOT EXISTS `" + getPrefixedTable("proposals") + "` ("
                            + " `tag` varchar(25) NOT NULL," + " `type` varchar(32) NOT NULL,"
                            + " `target` varchar(255) NOT NULL," + " `proposer` varchar(255) NOT NULL,"
                            + " `created_at` bigint NOT NULL," + " `votes` text NOT NULL," + " PRIMARY KEY  (`tag`));";
                    core.execute(query);

                }

            } else {

                plugin.getServer().getConsoleSender()
                        .sendMessage("[Unions-OG] " + ChatColor.RED + lang("sqlite.connection.failed"));

            }

        }

    }

    /**
     * Closes DB connection
     */
    public void closeConnection() {

        core.close();

    }

    /**
     * Import all data from database to memory
     */
    public void importFromDatabase() {

        plugin.getUnionManager().cleanData();

        List<Union> unions = retrieveUnions();
        purgeUnions(unions);

        for (Union union : unions) {

            plugin.getUnionManager().importUnion(union);

        }

        for (Union union : unions) {

            union.validateWarring();

        }

        if (!unions.isEmpty()) {

            plugin.getLogger().info(MessageFormat.format(lang("unions"), unions.size()));

        }

        List<UnionPlayer> cps = retrieveUnionPlayers();
        purgeUnionPlayers(cps);

        for (UnionPlayer cp : cps) {

            Union tm = cp.getUnion();

            if (tm != null) {

                tm.importMember(cp);

            }

            plugin.getUnionManager().importUnionPlayer(cp);

        }

        if (!cps.isEmpty()) {

            plugin.getLogger().info(MessageFormat.format(lang("union.players"), cps.size()));

        }

    }

    /**
     * Import one UnionPlayer data from database to memory Used for BungeeCord
     * Reload UnionPlayer and your Union
     *
     */
    @Deprecated
    public void importFromDatabaseOnePlayer(Player player) {

        plugin.getUnionManager().deleteUnionPlayerFromMemory(player.getUniqueId());

        UnionPlayer cp = retrieveOneUnionPlayer(player.getUniqueId());

        if (cp != null) {

            Union tm = cp.getUnion();

            if (tm != null) {

                tm.importMember(cp);

            }

            plugin.getUnionManager().importUnionPlayer(cp);

            plugin.getLogger().info("ClanPlayer Reloaded: " + player.getName() + ", UUID: " + player.getUniqueId());

        }

    }

    private void purgeUnions(List<Union> unions) {

        List<Union> purge = new ArrayList<>();

        for (Union union : unions) {

            if (union.isPermanent()) {

                continue;

            }

            int purgeUnion = plugin.getSettingsManager().getInt(PURGE_INACTIVE_UNION_DAYS);
            if (union.getInactiveDays() > purgeUnion && purgeUnion > 0) {

                purge.add(union);

            }

        }

        for (Union union : purge) {

            plugin.getLogger().info(lang("purging.union", union.getName()));
            for (UnionPlayer member : union.getMembers()) {

                union.removePlayerFromUnion(member.getUniqueId());

            }

            deleteUnion(union);
            unions.remove(union);

        }

    }

    private void purgeUnionPlayers(List<UnionPlayer> cps) {

        int purgePlayers = plugin.getSettingsManager().getInt(PURGE_INACTIVE_PLAYER_DAYS);
        if (purgePlayers < 1) {

            return;

        }

        List<UnionPlayer> purge = new ArrayList<>();

        for (UnionPlayer cp : cps) {

            // let the union be purged first
            if (cp.getUnion() != null) {

                continue;

            }

            if (cp.getInactiveDays() > purgePlayers) {

                purge.add(cp);

            }

        }

        for (UnionPlayer cp : purge) {

            plugin.getLogger().info(lang("purging.player.data", cp.getName()));
            deleteUnionPlayer(cp);
            cps.remove(cp);

        }

    }

    /**
     * Retrieves all unions from the database
     *
     */
    public List<Union> retrieveUnions() {

        List<Union> out = new ArrayList<>();

        String query = "SELECT * FROM `" + getPrefixedTable("clans") + "`;";
        ResultSet res = core.select(query);

        if (res != null) {

            try {

                while (res.next()) {

                    try {

                        boolean friendly_fire = res.getBoolean("friendly_fire");
                        String tag = res.getString("tag");
                        String color_tag = ChatUtils.parseColors(res.getString("color_tag"));
                        String name = res.getString("name");
                        String description = res.getString("description");
                        String packed_allies = res.getString("packed_allies");
                        String packed_rivals = res.getString("packed_rivals");
                        String packed_bb = res.getString("packed_bb");
                        String flags = res.getString("flags");
                        long founded = res.getLong("founded");
                        long last_used = res.getLong("last_used");
                        double balance = res.getDouble("balance");
                        double feeValue = res.getDouble("fee_value");
                        boolean feeEnabled = res.getBoolean("fee_enabled");
                        ItemStack banner = YAMLSerializer.deserialize(res.getString("banner"), ItemStack.class);

                        if (founded == 0) {

                            founded = (new Date()).getTime();

                        }

                        if (last_used == 0) {

                            last_used = (new Date()).getTime();

                        }

                        Union union = new Union();
                        union.setFlags(flags);
                        union.setFriendlyFire(friendly_fire);
                        union.setTag(tag);
                        union.setColorTag(color_tag);
                        union.setName(name);
                        union.setDescription(description);
                        union.setPackedAllies(packed_allies);
                        union.setPackedRivals(packed_rivals);
                        union.setPackedBb(packed_bb);
                        union.setFounded(founded);
                        union.setLastUsed(last_used);
                        union.setBalance(BankOperator.INTERNAL, UnionBalanceUpdateEvent.Cause.LOADING,
                                BankLogger.Operation.SET, balance);
                        union.setMemberFee(feeValue);
                        union.setMemberFeeEnabled(feeEnabled);
                        union.setBanner(banner);

                        out.add(union);

                    } catch (Exception ex) {

                        ex.printStackTrace();

                    }

                }

            } catch (SQLException ex) {

                plugin.getLogger().severe(String.format("An Error occurred: %s", ex.getErrorCode()));
                plugin.getLogger().log(Level.SEVERE, null, ex);

            }

        }

        return out;

    }

    /**
     * Retrieves one Union from the database Used for BungeeCord Reload UnionPlayer
     * and your Union
     */
    public @Nullable Union retrieveOneUnion(String tagUnion) {

        Union out = null;

        String query = "SELECT * FROM  `" + getPrefixedTable("clans") + "` WHERE `tag` = '" + tagUnion + "';";
        ResultSet res = core.select(query);

        if (res != null) {

            try {

                while (res.next()) {

                    try {

                        boolean friendly_fire = res.getBoolean("friendly_fire");
                        String tag = res.getString("tag");
                        String color_tag = ChatUtils.parseColors(res.getString("color_tag"));
                        String name = res.getString("name");
                        String description = res.getString("description");
                        String packed_allies = res.getString("packed_allies");
                        String packed_rivals = res.getString("packed_rivals");
                        String packed_bb = res.getString("packed_bb");
                        String flags = res.getString("flags");
                        long founded = res.getLong("founded");
                        long last_used = res.getLong("last_used");
                        double balance = res.getDouble("balance");
                        double feeValue = res.getDouble("fee_value");
                        boolean feeEnabled = res.getBoolean("fee_enabled");
                        ItemStack banner = YAMLSerializer.deserialize(res.getString("banner"), ItemStack.class);

                        if (founded == 0) {

                            founded = (new Date()).getTime();

                        }

                        if (last_used == 0) {

                            last_used = (new Date()).getTime();

                        }

                        Union union = new Union();
                        union.setFlags(flags);
                        union.setFriendlyFire(friendly_fire);
                        union.setTag(tag);
                        union.setColorTag(color_tag);
                        union.setName(name);
                        union.setDescription(description);
                        union.setPackedAllies(packed_allies);
                        union.setPackedRivals(packed_rivals);
                        union.setPackedBb(packed_bb);
                        union.setFounded(founded);
                        union.setLastUsed(last_used);
                        union.setBalance(BankOperator.INTERNAL, UnionBalanceUpdateEvent.Cause.LOADING,
                                BankLogger.Operation.SET, balance);
                        union.setMemberFee(feeValue);
                        union.setMemberFeeEnabled(feeEnabled);
                        union.setBanner(banner);

                        out = union;

                    } catch (Exception ex) {

                        ex.printStackTrace();

                    }

                }

            } catch (SQLException ex) {

                plugin.getLogger().severe(String.format("An Error occurred: %s", ex.getErrorCode()));
                plugin.getLogger().log(Level.SEVERE, null, ex);

            }

        }

        return out;

    }

    /**
     * Retrieves all union players from the database
     *
     */
    public List<UnionPlayer> retrieveUnionPlayers() {

        List<UnionPlayer> out = new ArrayList<>();

        String query = "SELECT * FROM  `" + getPrefixedTable("players") + "`;";
        ResultSet res = core.select(query);

        if (res != null) {

            try {

                while (res.next()) {

                    try {

                        String uuid = res.getString("uuid");
                        String name = res.getString("name");
                        String tag = res.getString("tag");
                        boolean friendly_fire = res.getBoolean("friendly_fire");
                        boolean trusted = res.getBoolean("trusted");
                        int neutral_kills = res.getInt("neutral_kills");
                        int rival_kills = res.getInt("rival_kills");
                        int civilian_kills = res.getInt("civilian_kills");
                        int ally_kills = res.getInt("ally_kills");
                        int deaths = res.getInt("deaths");
                        long last_seen = res.getLong("last_seen");
                        long join_date = res.getLong("join_date");
                        String flags = res.getString("flags");
                        String packed_past_unions = ChatUtils.parseColors(res.getString("packed_past_clans"));
                        String resign_times = res.getString("resign_times");
                        Locale locale = Helper.forLanguageTag(res.getString("locale"));

                        if (last_seen == 0) {

                            last_seen = (new Date()).getTime();

                        }

                        UnionPlayer cp = new UnionPlayer();
                        if (uuid != null) {

                            cp.setUniqueId(UUID.fromString(uuid));

                        }

                        cp.setFlags(flags);
                        cp.setName(name);
                        cp.setFriendlyFire(friendly_fire);
                        cp.setNeutralKills(neutral_kills);
                        cp.setRivalKills(rival_kills);
                        cp.setCivilianKills(civilian_kills);
                        cp.setAllyKills(ally_kills);
                        cp.setDeaths(deaths);
                        cp.setLastSeen(last_seen);
                        cp.setJoinDate(join_date);
                        cp.setPackedPastUnions(packed_past_unions);
                        cp.setTrusted(trusted);
                        cp.setResignTimes(Helper.resignTimesFromJson(resign_times));
                        cp.setLocale(locale);

                        if (!tag.isEmpty()) {

                            Union union = plugin.getUnionManager().getUnion(tag);

                            if (union != null) {

                                cp.setUnion(union);

                            }

                        }

                        out.add(cp);

                    } catch (Exception ex) {

                        ex.printStackTrace();

                    }

                }

            } catch (SQLException ex) {

                plugin.getLogger().severe(String.format("An Error occurred: %s", ex.getErrorCode()));
                plugin.getLogger().log(Level.SEVERE, null, ex);

            }

        }

        return out;

    }

    /**
     * Retrieves one union player from the database Used for BungeeCord Reload
     * UnionPlayer and your Union
     */
    public @Nullable UnionPlayer retrieveOneUnionPlayer(UUID playerUniqueId) {

        UnionPlayer out = null;

        String query = "SELECT * FROM `" + getPrefixedTable("players") + "` WHERE `uuid` = '"
                + playerUniqueId.toString() + "';";
        ResultSet res = core.select(query);

        if (res != null) {

            try {

                while (res.next()) {

                    try {

                        String uuid = res.getString("uuid");
                        String name = res.getString("name");
                        String tag = res.getString("tag");
                        boolean friendly_fire = res.getBoolean("friendly_fire");
                        boolean trusted = res.getBoolean("trusted");
                        int neutral_kills = res.getInt("neutral_kills");
                        int rival_kills = res.getInt("rival_kills");
                        int civilian_kills = res.getInt("civilian_kills");
                        int ally_kills = res.getInt("ally_kills");
                        int deaths = res.getInt("deaths");
                        long last_seen = res.getLong("last_seen");
                        long join_date = res.getLong("join_date");
                        String flags = res.getString("flags");
                        String packed_past_unions = ChatUtils.parseColors(res.getString("packed_past_clans"));
                        String resign_times = res.getString("resign_times");
                        Locale locale = Helper.forLanguageTag(res.getString("locale"));

                        if (last_seen == 0) {

                            last_seen = (new Date()).getTime();

                        }

                        UnionPlayer cp = new UnionPlayer();
                        if (uuid != null) {

                            cp.setUniqueId(UUID.fromString(uuid));

                        }

                        cp.setFlags(flags);
                        cp.setName(name);
                        cp.setFriendlyFire(friendly_fire);
                        cp.setNeutralKills(neutral_kills);
                        cp.setRivalKills(rival_kills);
                        cp.setCivilianKills(civilian_kills);
                        cp.setAllyKills(ally_kills);
                        cp.setDeaths(deaths);
                        cp.setLastSeen(last_seen);
                        cp.setJoinDate(join_date);
                        cp.setPackedPastUnions(packed_past_unions);
                        cp.setTrusted(trusted);
                        cp.setResignTimes(Helper.resignTimesFromJson(resign_times));
                        cp.setLocale(locale);

                        if (!tag.isEmpty()) {

                            Union unionDB = retrieveOneUnion(tag);
                            Union union = plugin.getUnionManager().getUnion(tag);

                            if (union != null && unionDB != null) {

                                Union unionReSync = UnionsOG.getInstance().getUnionManager().getUnion(tag);
                                unionReSync.setFlags(unionDB.getFlags());
                                unionReSync.setFriendlyFire(unionDB.isFriendlyFire());
                                unionReSync.setTag(unionDB.getTag());
                                unionReSync.setColorTag(unionDB.getColorTag());
                                unionReSync.setName(unionDB.getName());
                                unionReSync.setPackedAllies(unionDB.getPackedAllies());
                                unionReSync.setPackedRivals(unionDB.getPackedRivals());
                                unionReSync.setPackedBb(unionDB.getPackedBb());
                                unionReSync.setFounded(unionDB.getFounded());
                                unionReSync.setLastUsed(unionDB.getLastUsed());
                                unionReSync.setBalance(BankOperator.INTERNAL, UnionBalanceUpdateEvent.Cause.LOADING,
                                        BankLogger.Operation.SET, unionDB.getBalance());
                                cp.setUnion(unionReSync);

                            } else {

                                plugin.getUnionManager().importUnion(unionDB);
                                unionDB.validateWarring();
                                Union newUnion = plugin.getUnionManager().getUnion(unionDB.getTag());
                                cp.setUnion(newUnion);

                            }

                        }

                        out = cp;

                    } catch (Exception ex) {

                        ex.printStackTrace();

                    }

                }

            } catch (SQLException ex) {

                plugin.getLogger().severe(String.format("An Error occurred: %s", ex.getErrorCode()));
                plugin.getLogger().log(Level.SEVERE, null, ex);

            }

        }

        return out;

    }

    /**
     * Insert a union into the database
     *
     */
    public void insertUnion(Union union) {

        plugin.getProxyManager().sendUpdate(union);

        String query = "INSERT INTO `" + getPrefixedTable("clans")
                + "` (`banner`, `ranks`, `description`, `fee_enabled`, `fee_value`, `verified`, `tag`,"
                + " `color_tag`, `name`, `friendly_fire`, `founded`, `last_used`, `packed_allies`, `packed_rivals`, "
                + "`packed_bb`, `cape_url`, `flags`, `balance`) ";
        String values = "VALUES ( '" + Helper.escapeQuotes(YAMLSerializer.serialize(union.getBanner())) + "','','"
                + Helper.escapeQuotes(union.getDescription()) + "'," + (union.isMemberFeeEnabled() ? 1 : 0) + ","
                + Helper.escapeQuotes(String.valueOf(union.getMemberFee())) + ",1,'"
                + Helper.escapeQuotes(union.getTag()) + "','" + Helper.escapeQuotes(union.getColorTag()) + "','"
                + Helper.escapeQuotes(union.getName()) + "'," + (union.isFriendlyFire() ? 1 : 0) + ",'"
                + union.getFounded() + "','" + union.getLastUsed() + "','"
                + Helper.escapeQuotes(union.getPackedAllies()) + "','" + Helper.escapeQuotes(union.getPackedRivals())
                + "','" + Helper.escapeQuotes(union.getPackedBb()) + "','" + Helper.escapeQuotes(union.getCapeUrl())
                + "','" + Helper.escapeQuotes(union.getFlags()) + "','"
                + Helper.escapeQuotes(String.valueOf(union.getBalance())) + "');";
        core.executeUpdate(query + values);

    }

    /**
     * Update a union to the database asynchronously
     *
     */
    @Deprecated
    public void updateUnionAsync(final Union union) {

        new BukkitRunnable() {

            @Override
            public void run() {

                updateUnion(union);

            }

        }.runTaskAsynchronously(plugin);

    }

    /**
     * Change the name of a player in the database asynchronously
     *
     * @param cp to update
     */
    public void updatePlayerNameAsync(final @NotNull UnionPlayer cp) {

        new BukkitRunnable() {

            @Override
            public void run() {

                updatePlayerName(cp);

            }

        }.runTaskAsynchronously(plugin);

    }

    /**
     * Change the name of a player in the database
     *
     * @param cp to update
     */
    public void updatePlayerName(final @NotNull UnionPlayer cp) {

        String query = "UPDATE `" + getPrefixedTable("players") + "` SET `name` = '" + cp.getName() + "' WHERE uuid = '"
                + cp.getUniqueId() + "';";
        core.executeUpdate(query);

    }

    /**
     * Update a union to the database
     *
     */
    public void updateUnion(Union union) {

        updateUnion(union, true);

    }

    /**
     * Update a union to the database
     *
     * @param union          union to update
     *
     * @param updateLastUsed should the union's last used time be updated as well?
     */
    public void updateUnion(Union union, boolean updateLastUsed) {

        if (updateLastUsed) {

            union.updateLastUsed();

        }

        plugin.getProxyManager().sendUpdate(union);
        if (plugin.getSettingsManager().is(PERFORMANCE_SAVE_PERIODICALLY)) {

            modifiedUnions.add(union);
            return;

        }

        try (PreparedStatement st = prepareUpdateUnionStatement(core.getConnection())) {

            setValues(st, union);
            st.executeUpdate();

        } catch (SQLException ex) {

            plugin.getLogger().log(Level.SEVERE, String.format("Error updating Clan %s", union.getTag()), ex);

        }

    }

    private PreparedStatement prepareUpdateUnionStatement(Connection connection) throws SQLException {

        String sql = "UPDATE `" + getPrefixedTable("clans")
                + "` SET ranks = ?, banner = ?, description = ?, fee_enabled = ?, fee_value = ?, "
                + "verified = ?, tag = ?, color_tag = ?, `name` = ?, friendly_fire = ?, founded = ?, last_used = ?, "
                + "packed_allies = ?, packed_rivals = ?, packed_bb = ?, balance = ?, flags = ? WHERE tag = ?;";
        return connection.prepareStatement(sql);

    }

    private void setValues(PreparedStatement statement, Union union) throws SQLException {

        statement.setString(1, "");
        statement.setString(2, YAMLSerializer.serialize(union.getBanner()));
        statement.setString(3, union.getDescription());
        statement.setInt(4, union.isMemberFeeEnabled() ? 1 : 0);
        statement.setDouble(5, union.getMemberFee());
        statement.setInt(6, 1);
        statement.setString(7, union.getTag());
        statement.setString(8, union.getColorTag());
        statement.setString(9, union.getName());
        statement.setInt(10, union.isFriendlyFire() ? 1 : 0);
        statement.setLong(11, union.getFounded());
        statement.setLong(12, union.getLastUsed());
        statement.setString(13, union.getPackedAllies());
        statement.setString(14, union.getPackedRivals());
        statement.setString(15, union.getPackedBb());
        statement.setDouble(16, union.getBalance());
        statement.setString(17, union.getFlags());
        statement.setString(18, union.getTag());

    }

    /**
     * Delete a union from the database
     */
    /**
     * Retrieves all open proposals from the database
     *
     * @return the proposals, keyed by union tag
     */
    public Map<String, Proposal> retrieveProposals() {

        Map<String, Proposal> out = new HashMap<>();
        String query = "SELECT * FROM `" + getPrefixedTable("proposals") + "`;";

        try (ResultSet res = core.select(query)) {

            if (res == null) {

                return out;

            }

            while (res.next()) {

                String tag = res.getString("tag");
                ProposalType type = ProposalType.fromName(res.getString("type"));
                UUID proposer = parseUuid(res.getString("proposer"));
                if (type == null || proposer == null) {

                    continue;

                }

                Proposal proposal = new Proposal(type, tag, proposer, res.getString("target"),
                        res.getLong("created_at"));
                proposal.putVotes(votesFromString(res.getString("votes")));
                out.put(tag, proposal);

            }

        } catch (SQLException ex) {

            plugin.getLogger().log(Level.SEVERE, "Error retrieving proposals", ex);

        }

        return out;

    }

    /**
     * Inserts or updates a proposal. A union can only have one open proposal, so
     * the union's tag identifies the row.
     */
    public void saveProposal(Proposal proposal) {

        deleteProposal(proposal.getUnionTag());

        String query = "INSERT INTO `" + getPrefixedTable("proposals")
                + "` (`tag`, `type`, `target`, `proposer`, `created_at`, `votes`) VALUES ('"
                + Helper.escapeQuotes(proposal.getUnionTag()) + "','" + proposal.getType().name() + "','"
                + Helper.escapeQuotes(proposal.getTarget()) + "','" + proposal.getProposer() + "','"
                + proposal.getCreatedAt() + "','" + votesToString(proposal.getVotes()) + "');";
        core.executeUpdate(query);

    }

    public void deleteProposal(String unionTag) {

        String query = "DELETE FROM `" + getPrefixedTable("proposals") + "` WHERE tag = '"
                + Helper.escapeQuotes(unionTag) + "';";
        core.executeUpdate(query);

    }

    private @Nullable UUID parseUuid(@Nullable String uuid) {

        if (uuid == null) {

            return null;

        }

        try {

            return UUID.fromString(uuid);

        } catch (IllegalArgumentException ex) {

            return null;

        }

    }

    private String votesToString(Map<UUID, Boolean> votes) {

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<UUID, Boolean> vote : votes.entrySet()) {

            if (sb.length() > 0) {

                sb.append(',');

            }

            sb.append(vote.getKey()).append('=').append(vote.getValue() ? '1' : '0');

        }

        return sb.toString();

    }

    private Map<UUID, Boolean> votesFromString(@Nullable String votes) {

        Map<UUID, Boolean> out = new LinkedHashMap<>();
        if (votes == null || votes.isEmpty()) {

            return out;

        }

        for (String vote : votes.split(",")) {

            String[] parts = vote.split("=");
            if (parts.length != 2) {

                continue;

            }

            UUID voter = parseUuid(parts[0]);
            if (voter != null) {

                out.put(voter, "1".equals(parts[1]));

            }

        }

        return out;

    }

    public void deleteUnion(Union union) {

        plugin.getProxyManager().sendDelete(union);
        String query = "DELETE FROM `" + getPrefixedTable("clans") + "` WHERE tag = '" + union.getTag() + "';";
        core.executeUpdate(query);

    }

    /**
     * Insert a union player into the database
     *
     */
    public void insertUnionPlayer(UnionPlayer cp) {

        plugin.getProxyManager().sendUpdate(cp);

        String query = "INSERT INTO `" + getPrefixedTable("players")
                + "` (`uuid`, `name`, `leader`, `tag`, `friendly_fire`, `neutral_kills`, "
                + "`rival_kills`, `civilian_kills`, `deaths`, `last_seen`, `join_date`, `packed_past_clans`, `flags`) ";
        String values = "VALUES ('" + cp.getUniqueId().toString() + "', '" + cp.getName() + "'," + 0 + ",'"
                + Helper.escapeQuotes(cp.getTag()) + "'," + (cp.isFriendlyFire() ? 1 : 0) + "," + cp.getNeutralKills()
                + "," + cp.getRivalKills() + "," + cp.getCivilianKills() + "," + cp.getDeaths() + ",'"
                + cp.getLastSeen() + "',' " + cp.getJoinDate() + "','" + Helper.escapeQuotes(cp.getPackedPastUnions())
                + "','" + Helper.escapeQuotes(cp.getFlags()) + "');";
        core.executeUpdate(query + values);

    }

    /**
     * Update a union player to the database asynchronously
     *
     */
    @Deprecated
    public void updateUnionPlayerAsync(final UnionPlayer cp) {

        new BukkitRunnable() {

            @Override
            public void run() {

                updateUnionPlayer(cp);

            }

        }.runTaskAsynchronously(plugin);

    }

    /**
     * Update a union player to the database
     *
     */
    public void updateUnionPlayer(UnionPlayer cp) {

        cp.updateLastSeen();
        plugin.getProxyManager().sendUpdate(cp);
        if (plugin.getSettingsManager().is(PERFORMANCE_SAVE_PERIODICALLY)) {

            modifiedUnionPlayers.add(cp);
            return;

        }

        try (PreparedStatement st = prepareUpdateUnionPlayerStatement(core.getConnection())) {

            setValues(st, cp);
            st.executeUpdate();

        } catch (SQLException ex) {

            plugin.getLogger().log(Level.SEVERE, String.format("Error updating ClanPlayer %s", cp.getName()), ex);

        }

    }

    private PreparedStatement prepareUpdateUnionPlayerStatement(Connection connection) throws SQLException {

        String sql = "UPDATE `" + getPrefixedTable("players")
                + "` SET locale = ?, resign_times = ?, leader = ?, tag = ?, friendly_fire = ?,"
                + " neutral_kills = ?, ally_kills = ?, rival_kills = ?, civilian_kills = ?, deaths = ?, last_seen = ?,"
                + " packed_past_clans = ?, trusted = ?, flags = ?, `name` = ? WHERE `uuid` = ?;";
        return connection.prepareStatement(sql);

    }

    private void setValues(PreparedStatement statement, UnionPlayer cp) throws SQLException {

        statement.setString(1, Helper.toLanguageTag(cp.getLocale()));
        statement.setString(2, Helper.resignTimesToJson(cp.getResignTimes()));
        statement.setInt(3, 0);
        statement.setString(4, cp.getTag());
        statement.setInt(5, cp.isFriendlyFire() ? 1 : 0);
        statement.setInt(6, cp.getNeutralKills());
        statement.setInt(7, cp.getAllyKills());
        statement.setInt(8, cp.getRivalKills());
        statement.setInt(9, cp.getCivilianKills());
        statement.setInt(10, cp.getDeaths());
        statement.setLong(11, cp.getLastSeen());
        statement.setString(12, cp.getPackedPastUnions());
        statement.setInt(13, cp.isTrusted() ? 1 : 0);
        statement.setString(14, cp.getFlags());
        statement.setString(15, cp.getName());
        statement.setString(16, cp.getUniqueId().toString());

    }

    /**
     * Delete a union player from the database
     */
    public void deleteUnionPlayer(UnionPlayer cp) {

        final Union union = cp.getUnion();
        if (union != null) {

            union.addBbWithoutSaving(MessageFormat.format(lang("has.been.purged"), cp.getName()));
            updateUnion(union, false);

        }

        plugin.getProxyManager().sendDelete(cp);
        String query = "DELETE FROM `" + getPrefixedTable("players") + "` WHERE uuid = '" + cp.getUniqueId() + "';";
        core.executeUpdate(query);
        deleteKills(cp.getUniqueId());

    }

    /**
     * Insert a kill into the database
     *
     */
    @Deprecated
    public void insertKill(Player attacker, String attackerTag, Player victim, String victimTag, String type) {

        String query = "INSERT INTO `" + getPrefixedTable("kills")
                + "` (  `attacker_uuid`, `attacker`, `attacker_tag`, `victim_uuid`, `victim`, `victim_tag`, `kill_type`) ";
        String values = "VALUES ( '" + attacker.getUniqueId() + "','" + attacker.getName() + "','" + attackerTag + "','"
                + victim.getUniqueId() + "','" + victim.getName() + "','" + victimTag + "','" + type + "');";
        core.executeUpdate(query + values);

    }

    /**
     * Insert a kill into the database
     *
     * @param attacker the attacker
     * @param victim   the victim
     * @param type     the kill type
     */
    public void insertKill(@NotNull UnionPlayer attacker, @NotNull UnionPlayer victim, @NotNull String type,
            @NotNull LocalDateTime time)
    {

        String query = "INSERT INTO `" + getPrefixedTable("kills")
                + "` (  `attacker_uuid`, `attacker`, `attacker_tag`, `victim_uuid`, "
                + "`victim`, `victim_tag`, `kill_type`, `created_at`) ";
        String values = "VALUES ( '" + attacker.getUniqueId() + "','" + attacker.getName() + "','" + attacker.getTag()
                + "','" + victim.getUniqueId() + "','" + victim.getName() + "','" + victim.getTag() + "','" + type
                + "','" + time + "');";
        core.executeUpdate(query + values);

    }

    /**
     * Delete a player's kill record form the database
     *
     */
    @Deprecated
    public void deleteKills(String playerName) {

        String query = "DELETE FROM `" + getPrefixedTable("kills") + "` WHERE `attacker` = '" + playerName + "'";
        core.executeUpdate(query);

    }

    /**
     * Delete a player's kill record form the database
     *
     */
    public void deleteKills(UUID playerUniqueId) {

        String query = "DELETE FROM `" + getPrefixedTable("kills") + "` WHERE `attacker_uuid` = '" + playerUniqueId
                + "'";
        core.executeUpdate(query);

    }

    /**
     * Returns a map of victim-{@literal >}count of all kills that specific player
     * did
     *
     * @param playerName the attacker name
     *
     * @return a map of kills per victim
     *
     */
    public Map<String, Integer> getKillsPerPlayer(String playerName) {

        HashMap<String, Integer> out = new HashMap<>();

        String query = "SELECT victim, count(victim) AS kills FROM `" + getPrefixedTable("kills")
                + "` WHERE attacker = '" + playerName + "' GROUP BY victim ORDER BY count(victim) DESC;";
        ResultSet res = core.select(query);

        if (res != null) {

            try {

                while (res.next()) {

                    try {

                        String victim = res.getString("victim");
                        int kills = res.getInt("kills");
                        out.put(victim, kills);

                    } catch (Exception ex) {

                        plugin.getLogger().info(ex.getMessage());

                    }

                }

            } catch (SQLException ex) {

                plugin.getLogger().severe(String.format("An Error occurred: %s", ex.getErrorCode()));
                plugin.getLogger().log(Level.SEVERE, null, ex);

            }

        }

        return out;

    }

    /**
     * Returns a map of tag-{@literal >}count of all kills
     *
     * @return a map of kills per attacker+victim
     */
    public Map<String, Integer> getMostKilled() {

        HashMap<String, Integer> out = new HashMap<>();

        String query = "SELECT attacker, victim, count(victim) AS kills FROM `" + getPrefixedTable("kills")
                + "` GROUP BY attacker, victim ORDER BY 3 DESC;";
        ResultSet res = core.select(query);

        if (res != null) {

            try {

                while (res.next()) {

                    try {

                        String attacker = res.getString("attacker");
                        String victim = res.getString("victim");
                        int kills = res.getInt("kills");
                        out.put(attacker + " " + victim, kills);

                    } catch (Exception ex) {

                        plugin.getLogger().info(ex.getMessage());

                    }

                }

            } catch (SQLException ex) {

                plugin.getLogger().severe(String.format("An Error occurred: %s", ex.getErrorCode()));
                plugin.getLogger().log(Level.SEVERE, null, ex);

            }

        }

        return out;

    }

    /**
     * Gets, asynchronously, a map of tag-{@literal >}count of all kills and
     * notifies via callback when it's ready
     *
     * @param callback the callback
     */
    public void getMostKilled(DataCallback<Map<String, Integer>> callback) {

        new BukkitRunnable() {

            @Override
            public void run() {

                callback.onResultReady(getMostKilled());

            }

        }.runTaskAsynchronously(plugin);

    }

    /**
     * Gets, asynchronously, a map of victim-{@literal >}count of all kills that
     * specific player did and notifies via callback when it's ready
     *
     */
    public void getKillsPerPlayer(final String playerName, final DataCallback<Map<String, Integer>> callback) {

        new BukkitRunnable() {

            @Override
            public void run() {

                callback.onResultReady(getKillsPerPlayer(playerName));

            }

        }.runTaskAsynchronously(plugin);

    }

    /**
     * Callback that returns some data
     *
     * @author roinujnosde
     *
     */
    public interface DataCallback<T> {

        /**
         * Notifies when the result is ready
         *
         */
        void onResultReady(T data);

    }

    /**
     * Updates the database to the latest version
     *
     */
    private void updateDatabase() {

        String query;

        /*
         * From 2.2.6.3 to 2.3
         */
        if (!core.existsColumn(getPrefixedTable("clans"), "balance")) {

            query = "ALTER TABLE `" + getPrefixedTable("clans") + "` ADD COLUMN `balance` double(64,2);";
            core.execute(query);

        }

        /*
         * From 2.7.16 to 2.7.17
         */
        if (!core.existsColumn(getPrefixedTable("clans"), "fee_enabled")) {

            query = "ALTER TABLE `" + getPrefixedTable("clans") + "` ADD COLUMN `fee_enabled` tinyint(1) default '0';";
            core.execute(query);

        }

        if (!core.existsColumn(getPrefixedTable("clans"), "fee_value")) {

            query = "ALTER TABLE `" + getPrefixedTable("clans") + "` ADD COLUMN `fee_value` double(64,2);";
            core.execute(query);

        }

        /*
         * From 2.7.21 to 2.7.22
         */
        if (!core.existsColumn(getPrefixedTable("clans"), "description")) {

            query = "ALTER TABLE `" + getPrefixedTable("clans") + "` ADD COLUMN `description` varchar(255);";
            core.execute(query);

        }

        /*
         * From 2.7.22 to 2.7.23
         */
        if (!core.existsColumn(getPrefixedTable("players"), "resign_times")) {

            query = "ALTER TABLE `" + getPrefixedTable("players") + "` ADD COLUMN `resign_times` text;";
            core.execute(query);

        }

        /*
         * From 2.8.2 to 2.9
         */
        if (!core.existsColumn(getPrefixedTable("clans"), "ranks")) {

            query = "ALTER TABLE `" + getPrefixedTable("clans") + "` ADD COLUMN `ranks` text;";
            core.execute(query);

        }

        // From 2.12.1 to 2.13.0
        if (!core.existsColumn(getPrefixedTable("players"), "locale")) {

            query = "ALTER TABLE `" + getPrefixedTable("players") + "` ADD COLUMN `locale` varchar(10);";
            core.execute(query);

        }

        if (!core.existsColumn(getPrefixedTable("clans"), "banner")) {

            core.execute("ALTER TABLE `" + getPrefixedTable("clans") + "` ADD COLUMN `banner` text;");

        }

        // From 2.15.1 to 2.15.2
        if (!core.existsColumn(getPrefixedTable("players"), "ally_kills")) {

            core.execute(
                    "ALTER TABLE `" + getPrefixedTable("players") + "` ADD COLUMN `ally_kills` int(11) DEFAULT NULL;");

        }

        if (plugin.getSettingsManager().is(MYSQL_ENABLE)) {

            core.execute("ALTER TABLE `" + getPrefixedTable("clans") + "` MODIFY color_tag VARCHAR(255);");

        }

        /*
         * Bukkit 1.7.5+ UUID Migration
         */
        if (!core.existsColumn(getPrefixedTable("kills"), "attacker_uuid")) {

            query = "ALTER TABLE `" + getPrefixedTable("kills") + "` ADD attacker_uuid VARCHAR( 255 ) DEFAULT NULL;";
            core.execute(query);

        }

        if (!core.existsColumn(getPrefixedTable("kills"), "victim_uuid")) {

            query = "ALTER TABLE `" + getPrefixedTable("kills") + "` ADD victim_uuid VARCHAR( 255 ) DEFAULT NULL;";
            core.execute(query);

        }

        boolean useMysql = plugin.getSettingsManager().is(MYSQL_ENABLE);
        if (!core.existsColumn(getPrefixedTable("players"), "uuid")) {

            query = "ALTER TABLE `" + getPrefixedTable("players") + "` ADD uuid VARCHAR( 255 ) DEFAULT NULL;";
            core.execute(query);

            if (useMysql) {

                query = "ALTER TABLE `" + getPrefixedTable("players") + "` ADD UNIQUE `uq_player_uuid` (`uuid`);";
                core.execute(query);

            }

            updatePlayersToUUID();

            if (useMysql) {

                query = "ALTER TABLE `" + getPrefixedTable("players") + "` DROP INDEX uq_sc_players_1;";

            } else {

                query = "DROP INDEX IF EXISTS uq_sc_players_1;";

            }

            core.execute(query);

        }

        if (core.existsColumn(getPrefixedTable("players"), "uuid") && !useMysql) {

            query = "CREATE UNIQUE INDEX IF NOT EXISTS `uq_player_uuid` ON `" + getPrefixedTable("players")
                    + "` (`uuid`);";
            core.execute(query);

        }

        // From 2.19.3 to 2.20.0
        if (!core.existsColumn(getPrefixedTable("kills"), "created_at")) {

            query = "ALTER TABLE `" + getPrefixedTable("kills") + "` ADD `created_at` datetime NULL;";
            core.execute(query);

        }

    }

    /**
     * Updates the database to the latest version
     *
     */
    private void updatePlayersToUUID() {

        logMigrationStart();

        List<UnionPlayer> cps = retrieveUnionPlayers();
        Map<String, UUID> uuidMap = fetchUUIDs(cps);

        int totalPlayers = cps.size();
        for (int i = 0; i < totalPlayers; i++) {

            UnionPlayer cp = cps.get(i);
            try {

                UUID uuid = uuidMap.get(cp.getName());
                if (uuid != null) {

                    updatePlayerInDatabase(cp.getName(), uuid);
                    logSuccess(i + 1, totalPlayers, cp.getName(), uuid);

                }

            } catch (Exception ex) {

                logFailure(i + 1, totalPlayers, cp.getName(), ex);

            }

        }

        logMigrationEnd(totalPlayers);

    }

    private void updatePlayerInDatabase(String playerName, UUID uuid) {

        String[] tables = { "players", "kills", "kills" };
        String[] columns = { "uuid", "attacker_uuid", "victim_uuid" };
        String[] conditions = { "name", "attacker", "victim" };

        for (int i = 0; i < tables.length; i++) {

            String query = String.format("UPDATE `%s` SET %s = '%s' WHERE %s = '%s';", getPrefixedTable(tables[i]),
                    columns[i], uuid.toString(), conditions[i], playerName);
            core.executeUpdate(query);

        }

    }

    private Map<String, UUID> fetchUUIDs(List<UnionPlayer> unionPlayers) {

        Map<String, UUID> uuidMap = new HashMap<>();

        try {

            if (UnionsOG.getInstance().getServer().getOnlineMode()) {

                uuidMap = UUIDFetcher.fetchUUIDsForUnionPlayers(unionPlayers);

            } else {

                uuidMap = unionPlayers.stream().collect(Collectors.toMap(UnionPlayer::getName, player -> UUID
                        .nameUUIDFromBytes(("OfflinePlayer:" + player.getName()).getBytes(StandardCharsets.UTF_8))));

            }

        } catch (InterruptedException | ExecutionException ex) {

            plugin.getLogger().log(Level.SEVERE, "Error fetching UUIDs in bulk: " + ex.getMessage(), ex);

        }

        return uuidMap;

    }

    private void logSuccess(int current, int total, String playerName, UUID uuid) {

        plugin.getLogger().info(String.format("[%d / %d] Success: %s; UUID: %s", current, total, playerName, uuid));

    }

    private void logFailure(int current, int total, String playerName, Exception ex) {

        plugin.getLogger().log(Level.WARNING,
                String.format("[%d / %d] Failed [ERROR]: %s; UUID: ???", current, total, playerName), ex);

    }

    private void logMigrationStart() {

        plugin.getLogger().log(Level.WARNING, "Starting Migration to UUID Players!");
        plugin.getLogger().log(Level.WARNING, "==================== ATTENTION DON'T STOP BUKKIT! ====================");
        plugin.getLogger().log(Level.WARNING, "==================== ATTENTION DON'T STOP BUKKIT! ====================");
        plugin.getLogger().log(Level.WARNING, "==================== ATTENTION DON'T STOP BUKKIT! ====================");

    }

    private void logMigrationEnd(int totalPlayers) {

        plugin.getLogger().log(Level.WARNING, "==================== END OF MIGRATION ====================");
        plugin.getLogger().log(Level.WARNING, "==================== END OF MIGRATION ====================");
        plugin.getLogger().log(Level.WARNING, "==================== END OF MIGRATION ====================");

        if (totalPlayers > 0) {

            plugin.getLogger().info(MessageFormat.format(lang("union.players"), totalPlayers));

        }

    }

    private String getPrefixedTable(String name) {

        return plugin.getSettingsManager().getString(MYSQL_TABLE_PREFIX) + name;

    }

    /**
     * Saves modified Unions and UnionPlayers to the database
     * 
     * @since 2.10.2
     *
     *        <p>
     *        author: RoinujNosde
     *        </p>
     */
    public void saveModified() {

        try (PreparedStatement pst = prepareUpdateUnionPlayerStatement(core.getConnection())) {

            // removing purged players
            modifiedUnionPlayers.retainAll(plugin.getUnionManager().getAllUnionPlayers());
            for (UnionPlayer cp : modifiedUnionPlayers) {

                setValues(pst, cp);
                pst.addBatch();

            }

            pst.executeBatch();

            modifiedUnionPlayers.clear();

        } catch (SQLException ex) {

            plugin.getLogger().log(Level.SEVERE, "Error saving modified ClanPlayers:", ex);

        }

        try (PreparedStatement pst = prepareUpdateUnionStatement(core.getConnection())) {

            // removing disbanded unions
            modifiedUnions.retainAll(plugin.getUnionManager().getUnions());
            for (Union union : modifiedUnions) {

                setValues(pst, union);
                pst.addBatch();

            }

            pst.executeBatch();

            modifiedUnions.clear();

        } catch (SQLException ex) {

            plugin.getLogger().log(Level.SEVERE, "Error saving modified Clans:", ex);

        }

    }

}
