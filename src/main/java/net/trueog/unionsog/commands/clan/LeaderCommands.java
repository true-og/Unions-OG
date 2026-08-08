package net.trueog.unionsog.commands.clan;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.trueog.unionsog.*;
import net.trueog.unionsog.commands.ClanPlayerInput;
import net.trueog.unionsog.conversation.DisbandPrompt;
import net.trueog.unionsog.conversation.SCConversation;
import net.trueog.unionsog.hooks.discord.DiscordHook;
import net.trueog.unionsog.hooks.discord.exceptions.DiscordHookException;
import net.trueog.unionsog.managers.*;
import net.trueog.unionsog.utils.ChatUtils;
import org.bukkit.entity.Player;

import java.util.Objects;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;
import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.RED;

@CommandAlias("%clan")
@Conditions("%basic_conditions|leader")
public class LeaderCommands extends BaseCommand {

    @Dependency
    private UnionsOG plugin;
    @Dependency
    private StorageManager storage;
    @Dependency
    private PermissionsManager permissions;
    @Dependency
    private SettingsManager settings;
    @Dependency
    private RequestManager requestManager;
    @Dependency
    private ClanManager cm;
    @Dependency
    private ChatManager chatManager;

    @Subcommand("%demote")
    @CommandCompletion("@clan_leaders")
    @CommandPermission("unionsog.leader.demote")
    @Description("{@@command.description.demote}")
    public void demote(Player player, ClanPlayer cp, Clan clan,
            @Conditions("same_clan") @Name("leader") ClanPlayerInput other)
    {

        ClanPlayer otherCp = other.getClanPlayer();
        if (!clan.enoughLeadersOnlineToDemote(otherCp)) {

            ChatBlock.sendMessage(player, RED + lang("not.enough.leaders.online.to.vote.on.demotion", player));
            return;

        }

        if (!clan.isLeader(otherCp.getUniqueId())) {

            ChatBlock.sendMessage(player, RED + lang("player.is.not.a.leader.of.your.clan", player));
            return;

        }

        if (clan.getLeaders().size() > 2 && settings.is(CLAN_CONFIRMATION_FOR_DEMOTE)) {

            requestManager.addDemoteRequest(cp, otherCp.getName(), clan);
            ChatBlock.sendMessage(player, AQUA + lang("demotion.vote.has.been.requested.from.all.leaders", player));
            return;

        }

        clan.addBb(player.getName(), lang("demoted.back.to.member", otherCp.getName()));
        clan.demote(otherCp.getUniqueId());

    }

    @Subcommand("%promote")
    @CommandPermission("unionsog.leader.promote")
    @CommandCompletion("@clan_non_leaders")
    @Description("{@@command.description.promote}")
    public void promote(Player player, Clan clan, ClanPlayer cp,
            @Conditions("online|same_clan") @Name("member") ClanPlayerInput other)
    {

        Player otherPl = Objects.requireNonNull(other.getClanPlayer().toPlayer());
        if (!permissions.has(otherPl, "unionsog.leader.promotable")) {

            ChatBlock.sendMessage(player,
                    RED + lang("the.player.does.not.have.the.permissions.to.lead.a.clan", player));
            return;

        }

        if (otherPl.getUniqueId().equals(player.getUniqueId())) {

            ChatBlock.sendMessage(player, RED + lang("you.cannot.promote.yourself", player));
            return;

        }

        if (clan.isLeader(otherPl.getUniqueId())) {

            ChatBlock.sendMessage(player, RED + lang("the.player.is.already.a.leader", player));
            return;

        }

        if (settings.is(CLAN_CONFIRMATION_FOR_PROMOTE) && clan.getLeaders().size() > 1) {

            requestManager.requestAllLeaders(cp, ClanRequest.PROMOTE, otherPl.getName(), "asking.for.the.promotion",
                    player.getName(), otherPl.getName());
            ChatBlock.sendMessage(player, AQUA + lang("promotion.vote.has.been.requested.from.all.leaders", player));
            return;

        }

        clan.addBb(player.getName(), lang("promoted.to.leader", otherPl.getName()));
        clan.promote(otherPl.getUniqueId());

    }

    @Subcommand("%disband")
    @CommandPermission("unionsog.leader.disband")
    @Description("{@@command.description.disband}")
    public void disband(Player player, ClanPlayer cp, Clan clan) {

        if (clan.getLeaders().size() != 1) {

            requestManager.requestAllLeaders(cp, ClanRequest.DISBAND, clan.getTag(), "asking.to.disband",
                    player.getName());
            ChatBlock.sendMessage(player, AQUA + lang("clan.disband.vote.has.been.requested.from.all.leaders", player));
            return;

        }

        new SCConversation(plugin, player, new DisbandPrompt()).begin();

    }

    @Subcommand("%rename")
    @CommandPermission("unionsog.leader.rename")
    @CommandCompletion("@nothing")
    @Description("{@@command.description.rename}")
    public void rename(Player player, ClanPlayer cp, Clan clan, @Name("name") String clanName) {

        if (clanName.contains("&")) {

            ChatBlock.sendMessageKey(cp, "your.clan.name.cannot.contain.color.codes");
            return;

        }

        if (ChatUtils.stripColors(clanName).trim().equalsIgnoreCase("None")) {

            ChatBlock.sendMessage(player, RED + "Your union cannot be named \"None\".");
            return;

        }

        boolean bypass = plugin.getPermissionsManager().has(player, "unionsog.mod.bypass");
        if (!bypass) {

            if (ChatUtils.stripColors(clanName).length() > plugin.getSettingsManager().getInt(CLAN_MAX_LENGTH)) {

                ChatBlock.sendMessage(player, RED + lang("your.clan.name.cannot.be.longer.than.characters", player,
                        plugin.getSettingsManager().getInt(CLAN_MAX_LENGTH)));
                return;

            }

            if (ChatUtils.stripColors(clanName).length() <= plugin.getSettingsManager().getInt(CLAN_MIN_LENGTH)) {

                ChatBlock.sendMessage(player, RED + lang("your.clan.name.must.be.longer.than.characters", player,
                        plugin.getSettingsManager().getInt(CLAN_MIN_LENGTH)));
                return;

            }

        }

        if (clan.getLeaders().size() != 1) {

            requestManager.requestAllLeaders(cp, ClanRequest.RENAME, clanName, "asking.to.rename", cp.getName(),
                    clanName);
            ChatBlock.sendMessageKey(cp, "rename.vote.has.been.requested.from.all.leaders");
            return;

        }

        clan.setName(clanName);
        storage.updateClan(clan);

        ChatBlock.sendMessageKey(cp, "you.have.successfully.renamed.your.clan", clanName);

    }

    @Subcommand("%discord %create")
    @CommandPermission("unionsog.leader.discord.create")
    @Description("{@@command.description.discord.create}")
    public void discord(Player player, Clan clan) {

        DiscordHook discordHook = chatManager.getDiscordHook();
        if (discordHook == null) {

            ChatBlock.sendMessageKey(player, "discordhook.is.disabled");
            return;

        }

        try {

            discordHook.createChannel(clan.getTag());
            ChatBlock.sendMessageKey(player, "discord.created.successfully");

        } catch (DiscordHookException ex) {

            String messageKey = ex.getMessageKey();
            if (messageKey != null) {

                ChatBlock.sendMessage(player, RED + lang(messageKey));

            }

        }

    }

}
