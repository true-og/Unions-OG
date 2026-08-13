package net.trueog.unionsog.commands.general;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.CommandParameter;
import co.aikar.commands.HelpEntry;
import co.aikar.commands.annotation.*;
import net.trueog.unionsog.*;
import net.trueog.unionsog.commands.UnionInput;
import net.trueog.unionsog.commands.UnionPlayerInput;
import net.trueog.unionsog.commands.conditions.SettingEnabledCondition;
import net.trueog.unionsog.commands.data.*;
import net.trueog.unionsog.conversation.CreateUnionTagPrompt;
import net.trueog.unionsog.conversation.RequestCanceller;
import net.trueog.unionsog.conversation.ResetKdrPrompt;
import net.trueog.unionsog.conversation.SCConversation;
import net.trueog.unionsog.events.PlayerResetKdrEvent;
import net.trueog.unionsog.managers.UnionManager;
import net.trueog.unionsog.managers.RequestManager;
import net.trueog.unionsog.managers.SettingsManager;
import net.trueog.unionsog.managers.StorageManager;
import net.trueog.unionsog.ui.InventoryDrawer;
import net.trueog.unionsog.ui.frames.MainFrame;
import net.trueog.unionsog.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.conversation.CreateUnionNamePrompt.NAME_KEY;
import static net.trueog.unionsog.conversation.CreateUnionTagPrompt.TAG_KEY;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;
import static org.bukkit.ChatColor.*;

@CommandAlias("%union")
@Conditions("%basic_conditions")
public class GeneralCommands extends BaseCommand {

    // TODO: start - remove with UnionBankZeroMigration once union bank accounts
    // exist.
    private static final boolean UNION_BANKS_ENABLED = false;
    // TODO: end

    /** Condition ids that decide whether a command is usable at all. */
    private static final String MEMBERS_ONLY = "union_member";
    private static final String NON_MEMBERS_ONLY = "not_union_member";

    @Dependency
    private UnionsOG plugin;
    @Dependency
    private UnionManager cm;
    @Dependency
    private SettingsManager settings;
    @Dependency
    private StorageManager storage;
    @Dependency
    private RequestManager requestManager;

    @Default
    @Description("{@@command.description.union}")
    @HelpSearchTags("menu gui interface ui")
    public void main(CommandSender sender) {

        if (sender instanceof Player && settings.is(ENABLE_GUI)) {

            InventoryDrawer.open(new MainFrame((Player) sender));

        } else {

            help(sender, new CommandHelp(getCurrentCommandManager(),
                    getCurrentCommandManager().getRootCommand(getName()), getCurrentCommandIssuer()));

        }

    }

    @Subcommand("%locale")
    @CommandPermission("unionsog.anyone.locale")
    @Conditions("setting:field=LANGUAGE_SELECTOR,message=locale.is.prohibited")
    @Description("{@@command.description.locale}")
    @CommandCompletion("@locales")
    public void locale(UnionPlayer cp, @Values("@locales") @Name("locale") @Single String locale) {

        cp.setLocale(Helper.forLanguageTag(locale.replace("_", "-")));
        plugin.getStorageManager().updateUnionPlayer(cp);

        ChatBlock.sendMessageKey(cp, "locale.has.been.changed");

    }

    @Subcommand("%create")
    @CommandPermission("unionsog.member.create")
    @Conditions("not_union_member")
    @CommandCompletion("%compl:tag %compl:name")
    @Description("{@@command.description.create}")
    public void create(Player player, @Optional @Name("tag") String tag, @Optional @Name("name") String name) {

        HashMap<Object, Object> initialData = new HashMap<>();
        initialData.put(TAG_KEY, tag);
        initialData.put(NAME_KEY, name != null ? name : tag != null ? ChatUtils.stripColors(tag) : null);
        SCConversation conversation = new SCConversation(plugin, player, new CreateUnionTagPrompt(), initialData);
        conversation.addConversationCanceller(
                new RequestCanceller(player, RED + lang("union.create.request.cancelled", player)));
        conversation.begin();

    }

    @Subcommand("%leaderboard")
    @CommandPermission("unionsog.anyone.leaderboard")
    @Description("{@@command.description.leaderboard}")
    public void leaderboard(CommandSender sender) {

        Leaderboard l = new Leaderboard(plugin, sender);
        l.send();

    }

    @Subcommand("%lookup")
    @CommandCompletion("@players")
    @CommandPermission("unionsog.anyone.lookup")
    @Description("{@@command.description.lookup.other}")
    public void lookup(CommandSender sender, @Name("player") UnionPlayerInput player) {

        Lookup l = new Lookup(plugin, sender, player.getUnionPlayer().getUniqueId());
        l.send();

    }

    @Subcommand("%lookup")
    @CommandPermission("unionsog.member.lookup")
    @Description("{@@command.description.lookup}")
    public void lookup(Player sender) {

        Lookup l = new Lookup(plugin, sender, sender.getUniqueId());
        l.send();

    }

    @Subcommand("%kills")
    @CommandPermission("unionsog.member.kills")
    @Conditions("union_member")
    @CommandCompletion("@players")
    @Description("{@@command.description.kills}")
    public void kills(Player sender, @Optional @Name("player") UnionPlayerInput player) {

        String name = sender.getName();
        if (player != null) {

            name = player.getUnionPlayer().getName();

        }

        Kills k = new Kills(plugin, sender, name);
        k.send();

    }

    @Subcommand("%profile")
    @CommandPermission("unionsog.anyone.profile")
    @CommandCompletion("@unions:hide_own")
    @Description("{@@command.description.profile.other}")
    public void profile(CommandSender sender, @Name("union") UnionInput union) {

        UnionProfile p = new UnionProfile(plugin, sender, union.getUnion());
        p.send();

    }

    @Subcommand("%roster")
    @CommandCompletion("@unions:hide_own")
    @CommandPermission("unionsog.anyone.roster")
    @Description("{@@command.description.roster.other}")
    public void roster(CommandSender sender, @Name("union") UnionInput union) {

        UnionRoster r = new UnionRoster(plugin, sender, union.getUnion());
        r.send();

    }

    @Subcommand("%ff %allow")
    @CommandPermission("unionsog.member.ff")
    @Description("{@@command.description.ff.allow}")
    public void allowPersonalFf(Player player, UnionPlayer cp) {

        cp.setFriendlyFire(true);
        storage.updateUnionPlayer(cp);
        ChatBlock.sendMessage(player, AQUA + lang("personal.friendly.fire.is.set.to.allowed", player));

    }

    @Subcommand("%ff %auto")
    @CommandPermission("unionsog.member.ff")
    @Description("{@@command.description.ff.auto}")
    public void autoPersonalFf(Player player, UnionPlayer cp) {

        cp.setFriendlyFire(false);
        storage.updateUnionPlayer(cp);
        ChatBlock.sendMessage(player, AQUA + lang("friendy.fire.is.now.managed.by.your.union", player));

    }

    @Subcommand("%resetkdr %confirm")
    @CommandPermission("unionsog.vip.resetkdr")
    @Conditions("setting:field=ALLOW_RESET_KDR")
    @Description("{@@command.description.resetkdr}")
    public void resetKdrConfirm(Player player, UnionPlayer cp) {

        PlayerResetKdrEvent event = new PlayerResetKdrEvent(cp);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {

            cm.resetKdr(cp);
            ChatBlock.sendMessage(player, RED + lang("you.have.reseted.your.kdr", player));

        }

    }

    @Subcommand("%resetkdr")
    @CommandPermission("unionsog.vip.resetkdr")
    @Conditions("setting:field=ALLOW_RESET_KDR")
    @Description("{@@command.description.resetkdr}")
    public void resetKdr(Player player, UnionPlayer cp) {

        new SCConversation(plugin, player, new ResetKdrPrompt(cm), 60).begin();

    }

    @CommandAlias("%accept")
    @Description("{@@command.description.accept}")
    @Conditions("can_vote")
    public void accept(Player player, UnionPlayer cp) {

        Union union = cp.getUnion();
        if (union != null) {

            union.memberAnnounce(GREEN + lang("voted.to.accept", player.getName()));

        }

        requestManager.accept(cp);

    }

    @CommandAlias("%deny")
    @Description("{@@command.description.deny}")
    @Conditions("can_vote")
    public void deny(Player player, UnionPlayer cp) {

        Union union = cp.getUnion();
        if (union != null) {

            union.memberAnnounce(RED + lang("has.voted.to.deny", player.getName()));

        }

        requestManager.deny(cp);

    }

    @CommandAlias("%more")
    @Description("{@@command.description.more}")
    public void more(Player player) {

        ChatBlock chatBlock = storage.getChatBlock(player);

        if (chatBlock == null || chatBlock.size() <= 0) {

            ChatBlock.sendMessage(player, RED + lang("nothing.more.to.see", player));
            return;

        }

        chatBlock.sendBlock(player, settings.getInt(PAGE_SIZE));

        if (chatBlock.size() > 0) {

            ChatBlock.sendBlank(player);
            ChatBlock.sendMessage(player, settings.getColored(PAGE_HEADINGS_COLOR)
                    + lang("view.next.page", player, settings.getString(COMMANDS_MORE)));

        }

        ChatBlock.sendBlank(player);

    }

    @CatchUnknown
    @Subcommand("%help")
    @Description("{@@command.description.help}")
    public void help(CommandSender sender, CommandHelp help) {

        boolean inUnion = sender instanceof Player
                && cm.getUnionByPlayerUniqueId(((Player) sender).getUniqueId()) != null;
        for (HelpEntry helpEntry : help.getHelpEntries()) {

            // ACF hides only what the issuer lacks the permission for.
            // Union membership rules out more than that.
            if (!isRunnable(helpEntry, inUnion)) {

                helpEntry.setSearchScore(0);

            }

        }

        help.showHelp();

    }

    /**
     * Whether the issuer could actually run a help entry's command, judged by the
     * conditions the command declares rather than by a list kept in step by hand.
     *
     * @param entry   the help entry
     * @param inUnion whether the issuer is in a union
     */
    private boolean isRunnable(HelpEntry entry, boolean inUnion) {

        Set<String> conditions = new HashSet<>();
        boolean membersOnly = false;

        for (@SuppressWarnings("rawtypes")
        CommandParameter parameter : entry.getParameters()) {

            // An issuer resolved Union only exists for a member.
            membersOnly |= Union.class.equals(parameter.getType());
            addConditions(conditions, parameter.getConditions());

            Executable method = parameter.getParameter().getDeclaringExecutable();
            addConditions(conditions, declaredConditions(method));

            // ACF walks a nested command class up to its parent when it
            // validates conditions, so the enclosing classes count too.
            for (Class<?> scope = method.getDeclaringClass(); scope != null; scope = scope.getEnclosingClass()) {

                addConditions(conditions, declaredConditions(scope));

            }

        }

        if (membersOnly && !inUnion) {

            return false;

        }

        for (String condition : conditions) {

            if (!holds(condition, inUnion)) {

                return false;

            }

        }

        return true;

    }

    /**
     * Whether one declared condition lets the issuer through. Conditions that
     * depend on a target or a location cannot be judged here and are taken to hold,
     * since they do not make a command permanently unusable.
     *
     * @param condition one ACF condition, arguments included
     * @param inUnion   whether the issuer is in a union
     */
    private boolean holds(String condition, boolean inUnion) {

        int argument = condition.indexOf(':');
        String id = (argument == -1 ? condition : condition.substring(0, argument)).toLowerCase(Locale.ENGLISH);

        if (MEMBERS_ONLY.equals(id)) {

            return inUnion;

        }

        if (NON_MEMBERS_ONLY.equals(id)) {

            return !inUnion;

        }

        if (SettingEnabledCondition.ID.equals(id)) {

            return SettingEnabledCondition.isEnabled(settings, argumentOf(condition.substring(argument + 1)));

        }

        return true;

    }

    /**
     * Reads the {@code field} argument out of an ACF condition's comma separated
     * key/value list.
     */
    private static String argumentOf(String arguments) {

        for (String pair : arguments.split(",")) {

            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2 && SettingEnabledCondition.FIELD.equals(keyValue[0].trim())) {

                return keyValue[1].trim();

            }

        }

        return "";

    }

    private static @Nullable String declaredConditions(AnnotatedElement element) {

        Conditions conditions = element.getAnnotation(Conditions.class);
        return conditions != null ? conditions.value() : null;

    }

    /** Splits an ACF condition list into its conditions, arguments included. */
    private static void addConditions(Set<String> collected, @Nullable String conditions) {

        if (conditions == null || conditions.isEmpty()) {

            return;

        }

        for (String condition : conditions.split("\\|")) {

            collected.add(condition.trim());

        }

    }

    @Subcommand("%mostkilled")
    @CommandPermission("unionsog.mod.mostkilled")
    @Conditions("union_member")
    @Description("{@@command.description.mostkilled}")
    public void mostKilled(Player player) {

        MostKilled mk = new MostKilled(plugin, player);
        mk.send();

    }

    // TODO: start - drop the @Private with UnionBankZeroMigration. The ranking
    // answers "disabled" until union banks exist, so it stays out of the help.
    @Private
    // TODO: end
    @Subcommand("%list %balance")
    @CommandPermission("unionsog.anyone.list.balance")
    @Description("{@@command.description.list.balance}")
    public void listBalance(CommandSender sender) {

        // TODO: start - remove with UnionBankZeroMigration once union bank accounts
        // exist. Every union balance is zero until then, so the ranking is meaningless.
        if (!UNION_BANKS_ENABLED) {

            ChatBlock.sendMessage(sender, RED + lang("disabled.command", sender));
            return;

        }
        // TODO: end

        List<Union> unions = cm.getUnions();
        if (unions.isEmpty()) {

            sender.sendMessage(RED + lang("no.unions.have.been.created", sender));
            return;

        }

        unions.sort(Comparator.comparingDouble(Union::getBalance).reversed());

        sender.sendMessage(lang("union.list.balance.header", sender, settings.getColored(SERVER_NAME), unions.size()));
        String lineFormat = lang("union.list.balance.line", sender);

        String leftBracket = settings.getColored(TAG_BRACKET_COLOR) + settings.getColored(TAG_BRACKET_LEFT);
        String rightBracket = settings.getColored(TAG_BRACKET_COLOR) + settings.getColored(TAG_BRACKET_RIGHT);
        for (int i = 0; i < 10 && i < unions.size(); i++) {

            Union union = unions.get(i);
            String name = " " + settings.getColored(PAGE_UNION_NAME_COLOR) + union.getName();
            String line = MessageFormat.format(lineFormat, i + 1, leftBracket, union.getColorTag(), rightBracket, name,
                    union.getBalanceFormatted());
            sender.sendMessage(line);

        }

    }

    @Subcommand("%list")
    @CommandPermission("unionsog.anyone.list")
    @Description("{@@command.description.list}")
    @CommandCompletion("@union_list_type @order")
    public void list(CommandSender sender, @Optional String type, @Optional @Single String order,
            @Optional Integer page)
    {

        UnionList list = new UnionList(plugin, sender, type, order, page);
        list.send();

    }

    @Subcommand("%rivalries")
    @CommandPermission("unionsog.anyone.rivalries")
    @Description("{@@command.description.rivalries}")
    public void rivalries(CommandSender sender) {

        Rivalries rivalries = new Rivalries(plugin, sender);
        rivalries.send();

    }

    @Subcommand("%alliances")
    @CommandPermission("unionsog.anyone.alliances")
    @Description("{@@command.description.alliances}")
    public void alliances(CommandSender sender) {

        Alliances a = new Alliances(plugin, sender);
        a.send();

    }

}
