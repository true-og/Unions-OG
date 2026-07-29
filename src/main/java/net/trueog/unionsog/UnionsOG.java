package net.trueog.unionsog;

import co.aikar.commands.BukkitCommandIssuer;
import net.trueog.unionsog.commands.SCCommandManager;
import net.trueog.unionsog.hooks.papi.UnionsOGExpansion;
import net.trueog.unionsog.hooks.papi.UnionsOGMiniPlaceholders;
import net.trueog.unionsog.language.LanguageResource;
import net.trueog.unionsog.listeners.*;
import net.trueog.unionsog.loggers.BankLogger;
import net.trueog.unionsog.loggers.CSVBankLogger;
import net.trueog.unionsog.managers.*;
import net.trueog.unionsog.migrations.BbMigration;
import net.trueog.unionsog.migrations.ChatFormatMigration;
import net.trueog.unionsog.migrations.LanguageMigration;
import net.trueog.unionsog.migrations.UnionBankZeroMigration;
import net.trueog.unionsog.migrations.legacy.LegacyUnionsDatabaseMigrationRunner.MigrationStartupException;
import net.trueog.unionsog.proxy.BungeeManager;
import net.trueog.unionsog.proxy.ProxyManager;
import net.trueog.unionsog.tasks.*;
import net.trueog.unionsog.ui.InventoryController;
import net.trueog.unionsog.utils.ChatUtils;
import net.trueog.unionsog.utils.TagValidator;
import net.trueog.unionsog.uuid.UUIDMigration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;
import static org.bukkit.Bukkit.getPluginManager;

/**
 * @author Phaed
 */
public class UnionsOG extends JavaPlugin {

    private final ArrayList<String> messages = new ArrayList<>();

    private static UnionsOG instance;
    private static LanguageResource languageResource;
    private static final Logger logger = Logger.getLogger("Unions-OG");
    private SCCommandManager commandManager;
    private ClanManager clanManager;
    private RequestManager requestManager;
    private StorageManager storageManager;
    private SettingsManager settingsManager;
    private PermissionsManager permissionsManager;
    private TeleportManager teleportManager;
    private ProtectionManager protectionManager;
    private ChatManager chatManager;
    private ProxyManager proxyManager;
    private boolean hasUUID;
    private boolean startupComplete;
    private static final Pattern ACF_PLACEHOLDER_PATTERN = Pattern.compile("\\{(?<key>[a-zA-Z]+?)}");

    private BankLogger bankLogger;
    private TagValidator tagValidator;

    /**
     * @return the logger
     */
    @Deprecated
    public static Logger getLog() {

        return logger;

    }

    public static void debug(String msg) {

        // instance may be null during tests
        if (getInstance() == null || getInstance().getSettingsManager().is(DEBUG)) {

            logger.log(Level.INFO, msg);

        }

    }

    /**
     * @return the instance
     */
    public static UnionsOG getInstance() {

        return instance;

    }

    @Deprecated
    public static void log(String msg, Object... arg) {

        if (arg == null || arg.length == 0) {

            logger.log(Level.INFO, msg);

        } else {

            logger.log(Level.INFO, MessageFormat.format(msg, arg));

        }

    }

    @Override
    public void onEnable() {

        instance = this;
        new LanguageMigration(this).migrate();
        settingsManager = new SettingsManager(this);
        new BbMigration(settingsManager);
        new ChatFormatMigration(settingsManager);
        languageResource = new LanguageResource();
        this.hasUUID = UUIDMigration.canReturnUUID();

        permissionsManager = new PermissionsManager();
        requestManager = new RequestManager();
        clanManager = new ClanManager();
        proxyManager = new BungeeManager(this);
        try {

            storageManager = new StorageManager();

        } catch (MigrationStartupException ex) {

            getLogger().log(Level.SEVERE, ex.getMessage(), ex);
            getServer().getPluginManager().disablePlugin(this);
            return;

        }

        teleportManager = new TeleportManager();
        protectionManager = new ProtectionManager();
        protectionManager.registerListeners();
        chatManager = new ChatManager(this);
        registerEvents();
        permissionsManager.loadPermissions();
        commandManager = new SCCommandManager(this);
        bankLogger = new CSVBankLogger(this);
        // TODO: start - remove with UnionBankZeroMigration once union bank accounts
        // exist.
        new UnionBankZeroMigration(this).migrate();
        // TODO: end

        tagValidator = new TagValidator(settingsManager, permissionsManager);

        logStatus();
        startTasks();
        hookIntoPAPI();
        hookIntoMiniPlaceholders();
        startupComplete = true;

    }

    private void logStatus() {

        getLogger().info("Multithreading: " + settingsManager.is(PERFORMANCE_USE_THREADS));
        getLogger().info("BungeeCord: " + settingsManager.is(PERFORMANCE_USE_BUNGEECORD));
        getLogger().info("HEX support: " + ChatUtils.HEX_COLOR_SUPPORT);
        getLogger().info(
                "Help us translate Unions-OG to your language! " + "Access https://crowdin.com/project/simpleclans/");

    }

    private void registerEvents() {

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerDeath(this), this);
        pm.registerEvents(new SCPlayerListener(this), this);
        pm.registerEvents(new InventoryController(), this);
        pm.registerEvents(new TamableMobsSharing(this), this);
        pm.registerEvents(new PvPOnlyInWar(this), this);
        pm.registerEvents(new FriendlyFire(this), this);

    }

    private void hookIntoPAPI() {

        if (getPluginManager().getPlugin("PlaceholderAPI") != null) {

            getLogger().info("PlaceholderAPI found. Registering hook...");
            new UnionsOGExpansion(this).register();
            new UnionsOGExpansion(this, "simpleunions").register();

        }

    }

    private void hookIntoMiniPlaceholders() {

        if (getPluginManager().getPlugin("Utilities-OG") != null) {

            getLogger().info("Utilities-OG found. Registering MiniPlaceholders...");
            new UnionsOGMiniPlaceholders(this).register();

        }

    }

    private void startTasks() {

        if (getSettingsManager().is(PERFORMANCE_SAVE_PERIODICALLY)) {

            new SaveDataTask().start();

        }

        if (getSettingsManager().is(PERFORMANCE_HEAD_CACHING)) {

            new PlayerHeadCacheTask(this).start();

        }

    }

    @Override
    public void onDisable() {

        if (storageManager != null) {

            if (startupComplete && settingsManager != null && settingsManager.is(PERFORMANCE_SAVE_PERIODICALLY)) {

                storageManager.saveModified();

            }

            storageManager.closeConnection();

        }

        if (!startupComplete) {

            return;

        }

        permissionsManager.savePermissions();
        settingsManager.loadAndSave();

    }

    /**
     * @return the clanManager
     */
    public ClanManager getClanManager() {

        return clanManager;

    }

    /**
     * @return the requestManager
     */
    public RequestManager getRequestManager() {

        return requestManager;

    }

    /**
     * @return the storageManager
     */
    public StorageManager getStorageManager() {

        return storageManager;

    }

    /**
     * @return the settingsManager
     */
    public SettingsManager getSettingsManager() {

        return settingsManager;

    }

    /**
     * @return the permissionsManager
     */
    @NotNull
    public PermissionsManager getPermissionsManager() {

        return permissionsManager;

    }

    public SCCommandManager getCommandManager() {

        return commandManager;

    }

    public ProtectionManager getProtectionManager() {

        return protectionManager;

    }

    public ChatManager getChatManager() {

        return chatManager;

    }

    public ProxyManager getProxyManager() {

        return proxyManager;

    }

    public BankLogger getBankLogger() {

        return bankLogger;

    }

    /**
     * @param key the path within the language file
     * @return the lang
     */
    @Deprecated
    public String getLang(@NotNull String key) {

        return getLang(key, null);

    }

    @Deprecated
    public String getLang(@NotNull String key, @Nullable Player player) {

        return lang(key, player);

    }

    @Nullable
    public static String optionalLang(@NotNull String key, @Nullable ClanPlayer clanPlayer, Object... arguments) {

        Locale locale = instance.getSettingsManager().getLanguage();
        if (clanPlayer != null && clanPlayer.getLocale() != null
                && instance.getSettingsManager().is(LANGUAGE_SELECTOR))
        {

            locale = clanPlayer.getLocale();

        }

        String lang = languageResource.getLang(key, locale);
        if (lang == null) {

            return null;

        }

        String message = ChatUtils.parseColors(lang);
        // contains acf placeholders like {commandprefix}
        if (ACF_PLACEHOLDER_PATTERN.matcher(message).find()) {

            return message;

        }

        return MessageFormat.format(message, arguments);

    }

    @Nullable
    public static String optionalLang(@NotNull String key, @Nullable Player player, Object... arguments) {

        ClanPlayer clanPlayer = null;
        if (player != null) {

            clanPlayer = instance.getClanManager().getAnyClanPlayer(player.getUniqueId());

        }

        return optionalLang(key, clanPlayer, arguments);

    }

    @NotNull
    public static String lang(@NotNull String key, @Nullable Player player, Object... arguments) {

        String lang = optionalLang(key, player, arguments);
        return (lang == null) ? key : lang;

    }

    @NotNull
    public static String lang(@NotNull String key, @Nullable ClanPlayer clanPlayer, Object... arguments) {

        String lang = optionalLang(key, clanPlayer, arguments);
        return (lang == null) ? key : lang;

    }

    @NotNull
    public static String lang(@NotNull String key, @Nullable CommandSender sender, Object... arguments) {

        if (sender instanceof Player) {

            return lang(key, (Player) sender, arguments);

        } else {

            return lang(key, (Player) null, arguments);

        }

    }

    @NotNull
    public static String lang(@NotNull String key, @Nullable BukkitCommandIssuer issuer, Object... arguments) {

        if (issuer != null) {

            return lang(key, issuer.getIssuer(), arguments);

        }

        return lang(key, arguments);

    }

    @NotNull
    public static String lang(@NotNull String key, Object... arguments) {

        return lang(key, (Player) null, arguments);

    }

    public TeleportManager getTeleportManager() {

        return teleportManager;

    }

    @Deprecated
    public List<String> getMessages() {

        return messages;

    }

    @Deprecated
    public boolean hasUUID() {

        return this.hasUUID;

    }

    @Deprecated
    public void setUUID(boolean trueOrFalse) {

        this.hasUUID = trueOrFalse;

    }

    public TagValidator getTagValidator() {

        return tagValidator;

    }

}
