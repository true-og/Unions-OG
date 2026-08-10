package net.trueog.unionsog.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitLocales;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.PaperCommandManager;
import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.Helper;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.commands.completions.AbstractAsyncCompletion;
import net.trueog.unionsog.commands.completions.AbstractCompletion;
import net.trueog.unionsog.commands.completions.AbstractStaticCompletion;
import net.trueog.unionsog.commands.completions.AbstractSyncCompletion;
import net.trueog.unionsog.commands.conditions.AbstractCommandCondition;
import net.trueog.unionsog.commands.conditions.AbstractCondition;
import net.trueog.unionsog.commands.conditions.AbstractParameterCondition;
import net.trueog.unionsog.commands.contexts.AbstractContextResolver;
import net.trueog.unionsog.commands.contexts.AbstractInputOnlyContextResolver;
import net.trueog.unionsog.commands.contexts.AbstractIssuerOnlyContextResolver;
import net.trueog.unionsog.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.UnionsOG.optionalLang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;

public class SCCommandManager extends PaperCommandManager {

    private final UnionsOG plugin;
    private static final List<String> SUBCOMMANDS;
    private static final List<String> COMPLETIONS;

    public SCCommandManager(@NotNull UnionsOG plugin) {

        super(plugin);
        this.plugin = plugin;
        configure();

    }

    private void configure() {

        enableUnstableAPI("help");
        registerDependencies();
        addReplacements();
        registerContexts();
        registerCommands();
        registerConditions();
        registerCompletions();
        defaultHelpPerPage = plugin.getSettingsManager().getInt(HELP_SIZE);

    }

    private void registerDependencies() {

        registerDependency(UnionManager.class, plugin.getUnionManager());
        registerDependency(SettingsManager.class, plugin.getSettingsManager());
        registerDependency(StorageManager.class, plugin.getStorageManager());
        registerDependency(PermissionsManager.class, plugin.getPermissionsManager());
        registerDependency(RequestManager.class, plugin.getRequestManager());
        registerDependency(ProtectionManager.class, plugin.getProtectionManager());
        registerDependency(ChatManager.class, plugin.getChatManager());

    }

    private void registerCompletions() {

        Set<Class<? extends AbstractCompletion>> completions = Helper
                .getSubTypesOf("net.trueog.unionsog.commands.completions", AbstractCompletion.class);
        plugin.getLogger().info(String.format("Registering %d command completions...", completions.size()));
        for (Class<? extends AbstractCompletion> c : completions) {

            if (Modifier.isAbstract(c.getModifiers())) {

                continue;

            }

            try {

                AbstractCompletion obj = c.getConstructor(UnionsOG.class).newInstance(plugin);
                if (obj instanceof AbstractStaticCompletion) {

                    getCommandCompletions().registerStaticCompletion(obj.getId(),
                            ((AbstractStaticCompletion) obj).getCompletions());

                }

                if (obj instanceof AbstractAsyncCompletion) {

                    getCommandCompletions().registerAsyncCompletion(obj.getId(), (AbstractAsyncCompletion) obj);

                }

                if (obj instanceof AbstractSyncCompletion) {

                    getCommandCompletions().registerCompletion(obj.getId(), ((AbstractSyncCompletion) obj));

                }

            } catch (Exception ex) {

                plugin.getLogger().log(Level.SEVERE, "Error registering completion", ex);

            }

        }

    }

    @SuppressWarnings("unchecked")
    private void registerConditions() {

        Set<Class<? extends AbstractCondition>> conditions = Helper
                .getSubTypesOf("net.trueog.unionsog.commands.conditions", AbstractCondition.class);
        plugin.getLogger().info(String.format("Registering %d command conditions...", conditions.size()));
        for (Class<? extends AbstractCondition> c : conditions) {

            if (Modifier.isAbstract(c.getModifiers())) {

                continue;

            }

            try {

                AbstractCondition obj = c.getConstructor(UnionsOG.class).newInstance(plugin);
                if (obj instanceof AbstractParameterCondition) {

                    @SuppressWarnings("rawtypes")
                    AbstractParameterCondition condition = ((AbstractParameterCondition<?>) obj);
                    getCommandConditions().addCondition(condition.getType(), condition.getId(), condition);

                }

                if (obj instanceof AbstractCommandCondition) {

                    AbstractCommandCondition condition = ((AbstractCommandCondition) obj);
                    getCommandConditions().addCondition(condition.getId(), condition);

                }

            } catch (Exception ex) {

                plugin.getLogger().log(Level.SEVERE, "Error registering condition", ex);

            }

        }

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void registerContexts() {

        Set<Class<? extends AbstractContextResolver>> resolvers = Helper
                .getSubTypesOf("net.trueog.unionsog.commands.contexts", AbstractContextResolver.class);
        plugin.getLogger().info(String.format("Registering %d command contexts...", resolvers.size()));
        for (Class<? extends AbstractContextResolver> cr : resolvers) {

            if (Modifier.isAbstract(cr.getModifiers())) {

                continue;

            }

            try {

                AbstractContextResolver obj = cr.getConstructor(UnionsOG.class).newInstance(plugin);
                if (obj instanceof AbstractIssuerOnlyContextResolver) {

                    getCommandContexts().registerIssuerOnlyContext(obj.getType(),
                            ((AbstractIssuerOnlyContextResolver) obj));

                }

                if (obj instanceof AbstractInputOnlyContextResolver) {

                    getCommandContexts().registerContext(obj.getType(), ((AbstractInputOnlyContextResolver<?>) obj));

                }

            } catch (Exception ex) {

                plugin.getLogger().log(Level.SEVERE, "Error registering context", ex);

            }

        }

    }

    private void registerCommands() {

        boolean forceCommandPriority = plugin.getSettingsManager().is(COMMANDS_FORCE_PRIORITY);
        Set<Class<? extends BaseCommand>> commands = Helper.getSubTypesOf("net.trueog.unionsog.commands",
                BaseCommand.class);
        plugin.getLogger().info(String.format("Registering %d base commands...", commands.size()));
        for (Class<? extends BaseCommand> c : commands) {

            // ACF already registers nested classes
            if (c.isMemberClass() || Modifier.isStatic(c.getModifiers())) {

                continue;

            }

            // TODO: start - remove with UnionBankZeroMigration once union bank accounts
            // exist. Union banks hold no real value yet, so their commands stay
            // unregistered.
            if (c == net.trueog.unionsog.commands.union.BankCommand.class
                    || c == net.trueog.unionsog.commands.staff.BankCommand.class)
            {

                continue;

            }
            // TODO: end

            try {

                BaseCommand baseCommand = c.getConstructor().newInstance();
                registerCommand(baseCommand, forceCommandPriority);

            } catch (Exception ex) {

                plugin.getLogger().log(Level.SEVERE, "Error registering command", ex);

            }

        }

    }

    private void addReplacements() {

        SettingsManager sm = plugin.getSettingsManager();
        getCommandReplacements().addReplacements("basic_conditions", "not_blacklisted|not_banned", "union",
                getUnionCommandAliases(sm), "deny", sm.getString(COMMANDS_DENY) + "|deny", "more",
                sm.getString(COMMANDS_MORE), "ally_chat", sm.getString(COMMANDS_ALLY), "accept",
                sm.getString(COMMANDS_ACCEPT) + "|accept", "union_chat", sm.getString(COMMANDS_UNION_CHAT));

        SUBCOMMANDS.forEach(s -> processReplacement(s, "", ".command", true));
        COMPLETIONS.forEach(s -> processReplacement(s, "compl:", ".completion", false));

    }

    private String getUnionCommandAliases(SettingsManager sm) {

        Set<String> aliases = new LinkedHashSet<>();
        aliases.add(sm.getString(COMMANDS_UNION));
        aliases.add("union");
        aliases.add("unions");
        // clan and clans are not aliases; GroupAliasCommand redirects them.
        return String.join("|", aliases);

    }

    @Override
    public BukkitLocales getLocales() {

        if (this.locales == null) {

            this.locales = new BukkitLocales(this) {

                @Nullable
                private Player getPlayer(CommandIssuer issuer) {

                    if (issuer != null) {

                        return Bukkit.getPlayer(issuer.getUniqueId());

                    }

                    return null;

                }

                @Override
                @Nullable
                public String getOptionalMessage(CommandIssuer issuer, MessageKey key) {

                    return optionalLang(key.getKey(), getPlayer(issuer));

                }

                @Override
                @NotNull
                public String getMessage(CommandIssuer issuer, MessageKeyProvider key) {

                    return lang(key.getMessageKey().getKey(), getPlayer(issuer));

                }

            };

            // this.locales.loadLanguages();
        }

        return this.locales;

    }

    private void processReplacement(String key, String prefix, String suffix, boolean hasFallback) {

        String replacement = optionalLang(key + suffix, (UnionPlayer) null);
        if (replacement == null) {

            replacement = key;

        }

        replacement = replacement.replace(" ", "");
        if (hasFallback) {

            replacement = replacement + "|" + key;

        }

        getCommandReplacements().addReplacement(prefix + key, replacement);

    }

    static {

        SUBCOMMANDS = Arrays.asList("setbanner", "resetkdr", "place", "home", "war", "regroup", "mostkilled", "kills",
                "globalff", "reload", "unban", "ban", "disband", "resign", "ff", "unionff", "vote", "yes", "no",
                "purge", "bank", "kick", "invite", "toggle", "modtag", "bb", "display", "clear", "rival", "ally", "add",
                "remove", "stats", "coords", "vitals", "rivalries", "alliances", "leaderboard", "allow", "block",
                "auto", "check", "delete", "me", "tag", "deposit", "withdraw", "set", "status", "tp", "all", "everyone",
                "lookup", "roster", "profile", "list", "create", "description", "start", "end", "admin", "help", "mod",
                "land", "break", "interact", "place_block", "damage", "interact_entity", "container", "permanent",
                "take", "give", "join", "leave", "mute", "confirm", "balance", "discord", "rename", "locale", "color");

        COMPLETIONS = Arrays.asList("tag", "name");

    }

}
