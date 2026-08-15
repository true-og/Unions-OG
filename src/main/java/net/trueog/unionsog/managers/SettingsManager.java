package net.trueog.unionsog.managers;

import com.cryptomorin.xseries.XMaterial;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.utils.ChatUtils;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;
import static net.trueog.unionsog.utils.RankingNumberResolver.RankingType;
import static org.bukkit.Bukkit.getPluginManager;
import static org.bukkit.util.NumberConversions.toDouble;
import static org.bukkit.util.NumberConversions.toInt;

/**
 * @author phaed
 */
public final class SettingsManager {

    private final UnionsOG plugin;

    private final FileConfiguration config;
    private final File configFile;

    public SettingsManager(UnionsOG plugin) {

        this.plugin = plugin;
        config = plugin.getConfig();
        config.options().copyDefaults(true);
        configFile = new File(plugin.getDataFolder() + File.separator + "config.yml");
        loadAndSave();
        warnAboutPluginDependencies();

    }

    public <T> void set(ConfigField field, T value) {

        config.set(field.path, value);

    }

    public int getInt(ConfigField field) {

        return config.getInt(field.path, toInt(field.defaultValue));

    }

    public double getDouble(ConfigField field) {

        return config.getDouble(field.path, toDouble(field.defaultValue));

    }

    public List<String> getStringList(ConfigField field) {

        return config.getStringList(field.path);

    }

    public String getString(ConfigField field) {

        return config.getString(field.path, String.valueOf(field.defaultValue));

    }

    public String getColored(ConfigField field) {

        String value = getString(field);
        return (value.length() == 1) ? ChatUtils.getColorByChar(value.charAt(0)) : ChatUtils.parseColors(value);

    }

    public int getMinutes(ConfigField field) {

        int value = getInt(field);
        return (value >= 1) ? value * 20 * 60 : toInt(field.defaultValue) * 20 * 60;

    }

    public int getSeconds(ConfigField field) {

        int value = getInt(field);
        return (value >= 1) ? value * 20 : toInt(field.defaultValue) * 20;

    }

    public double getPercent(ConfigField field) {

        double value = getDouble(field);
        return (getDouble(field) >= 0 || getDouble(field) <= 100) ? value : toDouble(field.defaultValue);

    }

    public boolean is(ConfigField field) {

        return config.getBoolean(field.path, (Boolean) field.defaultValue);

    }

    /**
     * Load the configuration
     */
    public void loadAndSave() {

        if (configFile.exists()) {

            try {

                config.load(configFile);

            } catch (IOException | InvalidConfigurationException ex) {

                UnionsOG.getInstance().getLogger().log(Level.SEVERE, ex.getMessage(), ex);

            }

        }

        save();

    }

    public void save() {

        try {

            config.save(configFile);

        } catch (IOException ex) {

            UnionsOG.getInstance().getLogger().log(Level.SEVERE, ex.getMessage(), ex);

        }

    }

    private void warnAboutPluginDependencies() {

        Plugin luckPerms = getPluginManager().getPlugin("LuckPerms");
        Plugin discordSrv = getPluginManager().getPlugin("DiscordSRV");

        if (luckPerms != null && is(PERMISSIONS_AUTO_GROUP_GROUPNAME)) {

            plugin.getLogger().warning("LuckPerms was found and the setting auto-group-groupname is enabled.");
            plugin.getLogger().warning("Be careful with that as players will be automatically added in the group"
                    + " that matches their clan tag.");

        }

        if (discordSrv == null && is(DISCORDCHAT_ENABLE)) {

            plugin.getLogger().warning("DiscordChat can't be initialized, please, install DiscordSRV.");

        }

    }

    public Locale getLanguage() {

        String language = getString(LANGUAGE);
        String[] split = language.split("_");

        if (split.length == 2) {

            return new Locale(split[0], split[1]);

        }

        return new Locale(language);

    }

    public List<Material> getItemList() {

        List<Material> itemsList = new ArrayList<>();
        for (String material : getStringList(ITEM_LIST)) {

            Optional<XMaterial> x = XMaterial.matchXMaterial(material);
            if (x.isPresent()) {

                itemsList.add(x.get().parseMaterial());

            } else {

                plugin.getLogger().warning("Error with Material: " + material);

            }

        }

        return itemsList;

    }

    /**
     * Check whether a word is disallowed
     *
     * @param word the world
     * @return whether its disallowed word
     */
    public boolean isDisallowedWord(String word) {

        if (matchesAnyIgnoreCase(word, getString(COMMANDS_UNION), "union", "unions", "clan", "clans")) {

            return true;

        }

        for (String disallowedTag : getStringList(DISALLOWED_TAGS)) {

            if (disallowedTag.equalsIgnoreCase(word)) {

                return true;

            }

        }

        return matchesAnyIgnoreCase(word, getString(COMMANDS_MORE), getString(COMMANDS_DENY),
                getString(COMMANDS_ACCEPT));

    }

    private boolean matchesAnyIgnoreCase(String word, String... values) {

        for (String value : values) {

            if (word.equalsIgnoreCase(value)) {

                return true;

            }

        }

        return false;

    }

    /**
     * Check whether a string has a disallowed color
     *
     * @param str the string
     * @return whether the string contains the color code
     */
    public boolean hasDisallowedColor(String str) {

        String loweredString = str.toLowerCase();
        return getStringList(DISALLOWED_TAG_COLORS).stream().map(String::toLowerCase)
                .anyMatch(color -> loweredString.contains("&" + color));

    }

    /**
     * @return a comma delimited string with all disallowed colors
     */
    public String getDisallowedColorString() {

        return String.join(", ", getStringList(DISALLOWED_TAG_COLORS));

    }

    /**
     * Check whether a union is unrivable
     *
     * @param tag the tag
     * @return whether the union is unrivable
     */
    public boolean isUnrivable(String tag) {

        return getStringList(UNRIVABLE_UNIONS).stream().map(String::toLowerCase)
                .anyMatch(unrivable -> unrivable.equals(tag.toLowerCase()));

    }

    /**
     * Add a player to the banned list
     *
     * @param playerUniqueId the player's name
     */
    public void addBanned(UUID playerUniqueId) {

        List<String> bannedPlayers = getStringList(BANNED_PLAYERS);
        if (isBanned(playerUniqueId)) {

            return;

        }

        bannedPlayers.add(playerUniqueId.toString());
        set(BANNED_PLAYERS, bannedPlayers);
        save();

    }

    /**
     * Check whether a player is banned
     *
     * @param playerUniqueId the player's name
     * @return whether player is banned
     */
    public boolean isBanned(UUID playerUniqueId) {

        return getStringList(BANNED_PLAYERS).contains(playerUniqueId.toString());

    }

    /**
     * Remove a player from the banned list
     *
     * @param playerUniqueId the player's name
     */
    public void removeBanned(UUID playerUniqueId) {

        List<String> bannedPlayers = getStringList(BANNED_PLAYERS);
        bannedPlayers.remove(playerUniqueId.toString());
        set(BANNED_PLAYERS, bannedPlayers);
        save();

    }

    public boolean isActionAllowedInWar(@NotNull ProtectionManager.Action action) {

        return is(ConfigField.valueOf("WAR_ACTIONS_" + action.name()));

    }

    public List<String> getIgnoredList(@NotNull ProtectionManager.Action action) {

        return getStringList(ConfigField.valueOf("WAR_LISTENERS_IGNORED_LIST_" + action.name()));

    }

    @NotNull
    public RankingType getRankingType() {

        try {

            return RankingType.valueOf(getString(RANKING_TYPE));

        } catch (IllegalArgumentException ex) {

            return RankingType.DENSE;

        }

    }

    public FileConfiguration getConfig() {

        return config;

    }

    public enum ConfigField {

        /*
         * ================ > General Settings ================
         *
         */
        ENABLE_GUI("settings.enable-gui", true), DISABLE_MESSAGES("settings.disable-messages", false),
        TAMABLE_MOBS_SHARING("settings.tameable-mobs-sharing", false),
        TELEPORT_BLOCKS("settings.teleport-blocks", false),
        TELEPORT_HOME_ON_SPAWN("settings.teleport-home-on-spawn", false),
        DROP_ITEMS_ON_UNION_HOME("settings.drop-items-on-union-home", false),
        KEEP_ITEMS_ON_UNION_HOME("settings.keep-items-on-union-home", false), ITEM_LIST("settings.item-list"),
        DEBUG("settings.show-debug-info", false), ENABLE_AUTO_GROUPS("settings.enable-auto-groups", false),
        CHAT_COMPATIBILITY_MODE("settings.chat-compatibility-mode", true),
        RIVAL_LIMIT_PERCENT("settings.rival-limit-percent", 50),
        COLOR_CODE_FROM_PREFIX_FOR_NAME("settings.use-colorcode-from-prefix-for-name", true),
        DISPLAY_CHAT_TAGS("settings.display-chat-tags", true),
        GLOBAL_FRIENDLY_FIRE("settings.global-friendly-fire", false), UNRIVABLE_UNIONS("settings.unrivable-unions"),
        BLACKLISTED_WORLDS("settings.blacklisted-worlds"), BANNED_PLAYERS("settings.banned-players"),
        DISALLOWED_TAGS("settings.disallowed-tags"), LANGUAGE("settings.language", "en"),
        LANGUAGE_SELECTOR("settings.user-language-selector", true),
        DISALLOWED_TAG_COLORS("settings.disallowed-tag-colors"), SERVER_NAME("settings.server-name", "&4UnionsOG"),
        ALLOW_REGROUP("settings.allow-regroup-command", true), ALLOW_RESET_KDR("settings.allow-reset-kdr", false),
        REJOIN_COOLDOWN("settings.rejoin-cooldown", 60),
        ENABLE_REJOIN_COOLDOWN("settings.rejoin-cooldown-enabled", false),
        RANKING_TYPE("settings.ranking-type", "DENSE"), LIST_DEFAULT_ORDER_BY("settings.list-default-order-by", "size"),
        LORE_LENGTH("settings.lore-length", 36), PVP_ONLY_WHILE_IN_WAR("settings.pvp-only-while-at-war", false),
        PAST_UNIONS_LIMIT("settings.past-unions-limit", 10),
        USERNAME_REGEX("settings.username-regex", "^\\**[a-zA-Z0-9_$]{1,16}$"), TAG_REGEX("settings.tag-regex", ""),
        ACCEPT_OTHER_ALPHABETS_LETTERS("settings.accept-other-alphabets-letters-on-tag", false),
        DATE_TIME_PATTERN("settings.date-time-pattern", "HH:mm - dd/MM/yyyy"),
        BUNGEE_SERVERS("settings.bungee-servers"),
        /*
         * ================ > Tag Settings ================
         *
         */
        TAG_DEFAULT_COLOR("tag.default-color", "8"), TAG_BRACKET_COLOR("tag.bracket.color", "8"),
        TAG_BRACKET_LEFT("tag.bracket.left", ""), TAG_MAX_LENGTH("tag.max-length", 5),
        TAG_MIN_LENGTH("tag.min-length", 2), TAG_BRACKET_RIGHT("tag.bracket.right", ""),
        TAG_SEPARATOR_COLOR("tag.separator.color", "8"), TAG_SEPARATOR_CHAR("tag.separator.char", " ."), @Deprecated
        TAG_SEPARATOR_char("tag.separator.char", " ."),
        /*
         * ================ > War and Protection Settings ================
         *
         */
        ENABLE_WAR("war-and-protection.war-enabled", false), LAND_SHARING("war-and-protection.land-sharing", true),
        LAND_PROTECTION_PROVIDERS("war-and-protection.protection-providers"),
        WAR_LISTENERS_PRIORITY("war-and-protection.listeners.priority", "HIGHEST"),
        WAR_LISTENERS_IGNORED_LIST_PLACE("war-and-protection.listeners.ignored-list.PLACE"),
        WAR_LISTENERS_IGNORED_LIST_BREAK("war-and-protection.listeners.ignored-list.BREAK"),
        LAND_SET_BASE_ONLY_IN_LAND("war-and-protection.set-base-only-in-land", false),
        WAR_NORMAL_EXPIRATION_TIME("war-and-protection.war-normal-expiration-time", 0),
        WAR_DISCONNECT_EXPIRATION_TIME("war-and-protection.war-disconnect-expiration-time", 0),
        LAND_EDIT_ALL_LANDS("war-and-protection.edit-all-lands", false),
        LAND_CREATION_ONLY_ONE_PER_UNION("war-and-protection.land-creation.only-one-per-union", false),
        WAR_ACTIONS_CONTAINER("war-and-protection.war-actions.CONTAINER", true),
        WAR_ACTIONS_INTERACT("war-and-protection.war-actions.INTERACT", true),
        WAR_ACTIONS_BREAK("war-and-protection.war-actions.BREAK", true),
        WAR_ACTIONS_PLACE("war-and-protection.war-actions.PLACE", true),
        WAR_ACTIONS_DAMAGE("war-and-protection.war-actions.DAMAGE", true),
        WAR_ACTIONS_INTERACT_ENTITY("war-and-protection.war-actions.INTERACT_ENTITY", true),
        WAR_MAX_MEMBERS_DIFFERENCE("war-and-protection.war-start.members-online-max-difference", 5),
        /*
         * ================ > KDR Grinding Prevention Settings ================
         *
         */
        KDR_ENABLE_MAX_KILLS("kdr-grinding-prevention.enable-max-kills", false),
        KDR_MAX_KILLS_PER_VICTIM("kdr-grinding-prevention.max-kills-per-victim", 10),
        KDR_ENABLE_KILL_DELAY("kdr-grinding-prevention.enable-kill-delay", false),
        KDR_DELAY_BETWEEN_KILLS("kdr-grinding-prevention.delay-between-kills", 5),
        /*
         * ================ > Commands Settings ================
         *
         */
        COMMANDS_MORE("commands.more", "more"), COMMANDS_ALLY("commands.ally", "ally"),
        COMMANDS_UNION("commands.union", "union"), COMMANDS_ACCEPT("commands.accept", "accept"),
        COMMANDS_DENY("commands.deny", "deny"), COMMANDS_GLOBAL("commands.global", "global"),
        COMMANDS_UNION_CHAT("commands.union_chat", "u"), COMMANDS_FORCE_PRIORITY("commands.force-priority", true),
        /*
         * ================ > Economy Settings ================
         *
         */
        ECONOMY_CREATION_PRICE("economy.creation-price", 100.0),
        ECONOMY_PURCHASE_UNION_CREATE("economy.purchase-union-create", false),
        ECONOMY_INVITE_PRICE("economy.invite-price", 20),
        ECONOMY_PURCHASE_UNION_INVITE("economy.purchase-union-invite", false),
        ECONOMY_HOME_TELEPORT_PRICE("economy.home-teleport-price", 5.0),
        ECONOMY_PURCHASE_HOME_TELEPORT("economy.purchase-home-teleport", false),
        ECONOMY_HOME_TELEPORT_SET_PRICE("economy.home-teleport-set-price", 5.0),
        ECONOMY_PURCHASE_HOME_TELEPORT_SET("economy.purchase-home-teleport-set", false),
        ECONOMY_REGROUP_PRICE("economy.home-regroup-price", 5.0),
        ECONOMY_PURCHASE_HOME_REGROUP("economy.purchase-home-regroup", false),
        ECONOMY_UNIQUE_TAX_ON_REGROUP("economy.unique-tax-on-regroup", true),
        ECONOMY_ISSUER_PAYS_REGROUP("economy.issuer-pays-regroup", true),
        ECONOMY_BANK_LOG_ENABLED("economy.bank-log.enable", true),
        /*
         * ================ > Kill Weights Settings ================
         *
         */
        KILL_WEIGHTS_RIVAL("kill-weights.rival", 2.0), KILL_WEIGHTS_CIVILIAN("kill-weights.civilian", 0.0),
        KILL_WEIGHTS_NEUTRAL("kill-weights.neutral", 1.0), KILL_WEIGHTS_ALLY("kill-weights.ally", -1.0),
        KILL_WEIGHTS_DENY_SAME_IP_KILLS("kill-weights.deny-same-ip-kills", false),
        /*
         * ================ > Union Settings ================
         *
         */
        UNION_TELEPORT_DELAY("union.homebase-teleport-wait-secs", 10),
        UNION_HOMEBASE_CAN_BE_SET_ONLY_ONCE("union.homebase-can-be-set-only-once", true),
        UNION_MIN_SIZE_TO_SET_RIVAL("union.min-size-to-set-rival", 3),
        UNION_MIN_SIZE_TO_SET_ALLY("union.min-size-to-set-ally", 3), UNION_MAX_LENGTH("union.max-length", 25),
        UNION_MIN_LENGTH("union.min-length", 2), UNION_MAX_DESCRIPTION_LENGTH("union.max-description-length", 120),
        UNION_MIN_DESCRIPTION_LENGTH("union.min-description-length", 10), UNION_MAX_MEMBERS("union.max-members", 25),
        UNION_MAX_ALLIANCES("union.max-alliances", -1),
        UNION_TRUST_MEMBERS_BY_DEFAULT("union.trust-members-by-default", true),
        UNION_FF_ON_BY_DEFAULT("union.ff-on-by-default", false),
        /*
         * ================ > Page Settings ================
         */
        PAGE_UNTRUSTED_COLOR("page.untrusted-color", "8"), PAGE_TRUSTED_COLOR("page.trusted-color", "f"),
        PAGE_UNION_NAME_COLOR("page.union-name-color", "b"), PAGE_SUBTITLE_COLOR("page.subtitle-color", "7"),
        PAGE_HEADINGS_COLOR("page.headings-color", "8"), PAGE_SEPARATOR("page.separator", "-"),
        PAGE_SIZE("page.size", 100), HELP_SIZE("page.help-size", 10),
        /*
         * ================ > Union Chat Settings ================
         *
         */
        UNIONCHAT_ENABLE("unionchat.enable", true), UNIONCHAT_TAG_BASED("unionchat.tag-based-union-chat", false),
        UNIONCHAT_ANNOUNCEMENT_COLOR("unionchat.announcement-color", "e"),
        UNIONCHAT_FORMAT("unionchat.format", "&b[%union%&b] &4<%nick-color%%player%&4>: &b%message%"),
        UNIONCHAT_SPYFORMAT("unionchat.spy-format",
                "&8[Spy] [&bC&8] <%union%&8> <%nick-color%*&8%player%>&8: %message%"),
        UNIONCHAT_TRUSTED_COLOR("unionchat.trusted-color", "f"), UNIONCHAT_MEMBER_COLOR("unionchat.member-color", "7"),
        UNIONCHAT_BRACKET_COLOR("unionchat.tag-bracket.color", "e"),
        UNIONCHAT_BRACKET_LEFT("unionchat.tag-bracket.left", ""),
        UNIONCHAT_BRACKET_RIGHT("unionchat.tag-bracket.right", ""), UNIONCHAT_NAME_COLOR("unionchat.name-color", "e"),
        UNIONCHAT_PLAYER_BRACKET_LEFT("unionchat.player-bracket.left", ""),
        UNIONCHAT_PLAYER_BRACKET_RIGHT("unionchat.player-bracket.right", ""),
        UNIONCHAT_MESSAGE_COLOR("unionchat.message-color", "b"),
        UNIONCHAT_LISTENER_PRIORITY("unionchat.listener-priority", "LOW"),
        /*
         * ================ > Request Settings ================
         *
         */
        REQUEST_MESSAGE_COLOR("request.message-color", "b"), REQUEST_FREQUENCY("request.ask-frequency-secs", 60),
        REQUEST_MAX("request.max-asks-per-request", 1440),
        /*
         * ================ > BB Settings ================
         */
        BB_PREFIX("bb.prefix", "&8* &e"), BB_SHOW_ON_LOGIN("bb.show-on-login", true), BB_SIZE("bb.size", 6),
        BB_LOGIN_SIZE("bb.login-size", 6),
        /*
         * ================ > Ally Chat Settings ================
         */
        ALLYCHAT_ENABLE("allychat.enable", true),
        ALLYCHAT_FORMAT("allychat.format", "&b[Ally Chat] &4<%union%&4> <%nick-color%%player%&4>: &b%message%"),
        ALLYCHAT_SPYFORMAT("allychat.spy-format", "&8[Spy] [&cA&8] <%union%&8> <%nick-color%*&8%player%>&8: %message%"),
        ALLYCHAT_TRUSTED_COLOR("allychat.trusted-color", "f"), ALLYCHAT_MEMBER_COLOR("allychat.member-color", "7"),
        ALLYCHAT_BRACKET_COLOR("allychat.tag-bracket.color", "8"),
        ALLYCHAT_BRACKET_lEFT("allychat.tag-bracket.left", ""),
        ALLYCHAT_BRACKET_RIGHT("allychat.tag-bracket.right", ""),
        ALLYCHAT_PLAYER_BRACKET_LEFT("allychat.player-bracket.left", ""),
        ALLYCHAT_PLAYER_BRACKET_RIGHT("allychat.player-bracket.right", ""),
        ALLYCHAT_MESSAGE_COLOR("allychat.message-color", "3"), ALLYCHAT_TAG_COLOR("allychat.tag-color", ""),
        /*
         * ================ > Discord Chat Settings ================
         */
        DISCORDCHAT_ENABLE("discordchat.enable", false), DISCORDCHAT_AUTO_CREATION("discordchat.auto-creation", true),
        DISCORDCHAT_FORMAT_TO("discordchat.discord-format", "%player% » %message%"),
        DISCORDCHAT_FORMAT("discordchat.format", "&b[&9D&b] &b[%union%&b] &4<%nick-color%%player%&4>: &b%message%"),
        DISCORDCHAT_SPYFORMAT("discordchat.spy-format",
                "&8[Spy] [&9D&8] <%union%&8> <%nick-color%*&8%player%>&8: %message%"),
        DISCORDCHAT_TEXT_CATEGORY_FORMAT("discordchat.text.category-format", "SC - TextChannels"),
        DISCORDCHAT_TEXT_CATEGORY_IDS("discordchat.text.category-ids"),
        DISCORDCHAT_TEXT_WHITELIST("discordchat.text.whitelist"),
        DISCORDCHAT_TEXT_LIMIT("discordchat.text.unions-limit", 100),
        DISCORDCHAT_MINIMUM_LINKED_PLAYERS("discordchat.min-linked-players-to-create", 3),
        /*
         * ================ > MySQL Settings ================
         */
        MYSQL_USERNAME("mysql.username", ""), MYSQL_HOST("mysql.host", "localhost"), MYSQL_PORT("mysql.port", 3306),
        MYSQL_ENABLE("mysql.enable", false), MYSQL_PASSWORD("mysql.password", ""), MYSQL_DATABASE("mysql.database", ""),
        MYSQL_TABLE_PREFIX("mysql.table_prefix", "sc_"),
        MYSQL_MIGRATE_LEGACY_UNIONS_DATABASE("mysql.migrate-legacy-unions-database", false),
        /*
         * ================ > Permissions Settings ================
         */
        PERMISSIONS_AUTO_GROUP_GROUPNAME("permissions.auto-group-groupname", false),
        /*
         * ================ > Performance Settings ================
         */
        PERFORMANCE_SAVE_PERIODICALLY("performance.save-periodically", true),
        PERFORMANCE_SAVE_INTERVAL("performance.save-interval", 10),
        PERFORMANCE_USE_THREADS("performance.use-threads", true),
        PERFORMANCE_USE_BUNGEECORD("performance.use-bungeecord", false),
        PERFORMANCE_HEAD_CACHING("performance.cache-player-heads", false),

        SAFE_CIVILIANS("safe-civilians", false);

        private final String path;
        private final Object defaultValue;

        ConfigField(String path, Object defaultValue) {

            this.path = path;
            this.defaultValue = defaultValue;

        }

        ConfigField(String path) {

            this.path = path;
            defaultValue = null;

        }

    }

}
