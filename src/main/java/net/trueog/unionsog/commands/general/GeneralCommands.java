package net.trueog.unionsog.commands.general;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.CommandParameter;
import co.aikar.commands.HelpEntry;
import co.aikar.commands.annotation.*;
import net.trueog.unionsog.*;
import net.trueog.unionsog.commands.ClanInput;
import net.trueog.unionsog.commands.ClanPlayerInput;
import net.trueog.unionsog.commands.data.*;
import net.trueog.unionsog.conversation.CreateClanTagPrompt;
import net.trueog.unionsog.conversation.RequestCanceller;
import net.trueog.unionsog.conversation.ResetKdrPrompt;
import net.trueog.unionsog.conversation.SCConversation;
import net.trueog.unionsog.events.PlayerResetKdrEvent;
import net.trueog.unionsog.managers.ClanManager;
import net.trueog.unionsog.managers.RequestManager;
import net.trueog.unionsog.managers.SettingsManager;
import net.trueog.unionsog.managers.StorageManager;
import net.trueog.unionsog.ui.InventoryDrawer;
import net.trueog.unionsog.ui.frames.MainFrame;
import net.trueog.unionsog.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.conversation.CreateClanNamePrompt.NAME_KEY;
import static net.trueog.unionsog.conversation.CreateClanTagPrompt.TAG_KEY;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;
import static org.bukkit.ChatColor.*;

@CommandAlias("%clan")
@Conditions("%basic_conditions")
public class GeneralCommands extends BaseCommand {

    // TODO: start - remove with UnionBankZeroMigration once union bank accounts
    // exist.
    private static final boolean UNION_BANKS_ENABLED = false;
    // TODO: end

    @Dependency
    private UnionsOG plugin;
    @Dependency
    private ClanManager cm;
    @Dependency
    private SettingsManager settings;
    @Dependency
    private StorageManager storage;
    @Dependency
    private RequestManager requestManager;

    @Default
    @Description("{@@command.description.clan}")
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
    @Description("{@@command.description.locale}")
    @CommandCompletion("@locales")
    public void locale(ClanPlayer cp, @Values("@locales") @Name("locale") @Single String locale) {

        if (!settings.is(LANGUAGE_SELECTOR)) {

            ChatBlock.sendMessageKey(cp, "locale.is.prohibited");
            return;

        }

        cp.setLocale(Helper.forLanguageTag(locale.replace("_", "-")));
        plugin.getStorageManager().updateClanPlayer(cp);

        ChatBlock.sendMessageKey(cp, "locale.has.been.changed");

    }

    @Subcommand("%create")
    @CommandPermission("unionsog.leader.create")
    @CommandCompletion("%compl:tag %compl:name")
    @Description("{@@command.description.create}")
    public void create(Player player, @Optional @Name("tag") String tag, @Optional @Name("name") String name) {

        ClanPlayer cp = cm.getAnyClanPlayer(player.getUniqueId());

        if (cp != null && cp.getClan() != null) {

            ChatBlock.sendMessage(player, RED + lang("you.must.first.resign", player, cp.getClan().getName()));
            return;

        }

        HashMap<Object, Object> initialData = new HashMap<>();
        initialData.put(TAG_KEY, tag);
        initialData.put(NAME_KEY, name != null ? name : tag != null ? ChatUtils.stripColors(tag) : null);
        SCConversation conversation = new SCConversation(plugin, player, new CreateClanTagPrompt(), initialData);
        conversation.addConversationCanceller(
                new RequestCanceller(player, RED + lang("clan.create.request.cancelled", player)));
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
    public void lookup(CommandSender sender, @Name("player") ClanPlayerInput player) {

        Lookup l = new Lookup(plugin, sender, player.getClanPlayer().getUniqueId());
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
    @Conditions("rank:name=KILLS")
    @CommandCompletion("@players")
    @Description("{@@command.description.kills}")
    public void kills(Player sender, @Optional @Name("player") ClanPlayerInput player) {

        String name = sender.getName();
        if (player != null) {

            name = player.getClanPlayer().getName();

        }

        Kills k = new Kills(plugin, sender, name);
        k.send();

    }

    @Subcommand("%profile")
    @CommandPermission("unionsog.anyone.profile")
    @CommandCompletion("@clans:hide_own")
    @Description("{@@command.description.profile.other}")
    public void profile(CommandSender sender, @Name("clan") ClanInput clan) {

        ClanProfile p = new ClanProfile(plugin, sender, clan.getClan());
        p.send();

    }

    @Subcommand("%roster")
    @CommandCompletion("@clans:hide_own")
    @CommandPermission("unionsog.anyone.roster")
    @Description("{@@command.description.roster.other}")
    public void roster(CommandSender sender, @Name("clan") ClanInput clan) {

        ClanRoster r = new ClanRoster(plugin, sender, clan.getClan());
        r.send();

    }

    @Subcommand("%ff %allow")
    @CommandPermission("unionsog.member.ff")
    @Description("{@@command.description.ff.allow}")
    public void allowPersonalFf(Player player, ClanPlayer cp) {

        cp.setFriendlyFire(true);
        storage.updateClanPlayer(cp);
        ChatBlock.sendMessage(player, AQUA + lang("personal.friendly.fire.is.set.to.allowed", player));

    }

    @Subcommand("%ff %auto")
    @CommandPermission("unionsog.member.ff")
    @Description("{@@command.description.ff.auto}")
    public void autoPersonalFf(Player player, ClanPlayer cp) {

        cp.setFriendlyFire(false);
        storage.updateClanPlayer(cp);
        ChatBlock.sendMessage(player, AQUA + lang("friendy.fire.is.now.managed.by.your.clan", player));

    }

    @Subcommand("%resetkdr %confirm")
    @CommandPermission("unionsog.vip.resetkdr")
    @Description("{@@command.description.resetkdr}")
    public void resetKdrConfirm(Player player, ClanPlayer cp) {

        if (!settings.is(ALLOW_RESET_KDR)) {

            ChatBlock.sendMessage(player, RED + lang("disabled.command", player));
            return;

        }

        PlayerResetKdrEvent event = new PlayerResetKdrEvent(cp);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {

            cm.resetKdr(cp);
            ChatBlock.sendMessage(player, RED + lang("you.have.reseted.your.kdr", player));

        }

    }

    @Subcommand("%resetkdr")
    @CommandPermission("unionsog.vip.resetkdr")
    @Description("{@@command.description.resetkdr}")
    public void resetKdr(Player player, ClanPlayer cp) {

        if (!settings.is(ALLOW_RESET_KDR)) {

            ChatBlock.sendMessage(player, RED + lang("disabled.command", player));

        } else {

            new SCConversation(plugin, player, new ResetKdrPrompt(cm), 60).begin();

        }

    }

    @CommandAlias("%accept")
    @Description("{@@command.description.accept}")
    @Conditions("can_vote")
    public void accept(Player player, ClanPlayer cp) {

        Clan clan = cp.getClan();
        if (clan != null) {

            clan.leaderAnnounce(GREEN + lang("voted.to.accept", player.getName()));

        }

        requestManager.accept(cp);

    }

    @CommandAlias("%deny")
    @Description("{@@command.description.deny}")
    @Conditions("can_vote")
    public void deny(Player player, ClanPlayer cp) {

        Clan clan = cp.getClan();
        if (clan != null) {

            clan.leaderAnnounce(RED + lang("has.voted.to.deny", player.getName()));

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

        boolean inClan = sender instanceof Player
                && cm.getClanByPlayerUniqueId(((Player) sender).getUniqueId()) != null;
        for (HelpEntry helpEntry : help.getHelpEntries()) {

            for (@SuppressWarnings("rawtypes")
            CommandParameter parameter : helpEntry.getParameters()) {

                if (parameter.getType().equals(Clan.class) && !inClan) {

                    helpEntry.setSearchScore(0);

                }

            }

        }

        help.showHelp();

    }

    @Subcommand("%mostkilled")
    @CommandPermission("unionsog.mod.mostkilled")
    @Conditions("rank:name=MOSTKILLED")
    @Description("{@@command.description.mostkilled}")
    public void mostKilled(Player player) {

        MostKilled mk = new MostKilled(plugin, player);
        mk.send();

    }

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

        List<Clan> clans = cm.getClans();
        if (clans.isEmpty()) {

            sender.sendMessage(RED + lang("no.clans.have.been.created", sender));
            return;

        }

        clans.sort(Comparator.comparingDouble(Clan::getBalance).reversed());

        sender.sendMessage(lang("clan.list.balance.header", sender, settings.getColored(SERVER_NAME), clans.size()));
        String lineFormat = lang("clan.list.balance.line", sender);

        String leftBracket = settings.getColored(TAG_BRACKET_COLOR) + settings.getColored(TAG_BRACKET_LEFT);
        String rightBracket = settings.getColored(TAG_BRACKET_COLOR) + settings.getColored(TAG_BRACKET_RIGHT);
        for (int i = 0; i < 10 && i < clans.size(); i++) {

            Clan clan = clans.get(i);
            String name = " " + settings.getColored(PAGE_CLAN_NAME_COLOR) + clan.getName();
            String line = MessageFormat.format(lineFormat, i + 1, leftBracket, clan.getColorTag(), rightBracket, name,
                    clan.getBalanceFormatted());
            sender.sendMessage(line);

        }

    }

    @Subcommand("%list")
    @CommandPermission("unionsog.anyone.list")
    @Description("{@@command.description.list}")
    @CommandCompletion("@clan_list_type @order")
    public void list(CommandSender sender, @Optional String type, @Optional @Single String order,
            @Optional Integer page)
    {

        ClanList list = new ClanList(plugin, sender, type, order, page);
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
