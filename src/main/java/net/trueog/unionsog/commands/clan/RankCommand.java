package net.trueog.unionsog.commands.clan;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.trueog.unionsog.*;
import net.trueog.unionsog.commands.ClanPlayerInput;
import net.trueog.unionsog.conversation.CreateRankNamePrompt;
import net.trueog.unionsog.conversation.RequestCanceller;
import net.trueog.unionsog.conversation.SCConversation;
import net.trueog.unionsog.events.DeleteRankEvent;
import net.trueog.unionsog.events.PlayerRankUpdateEvent;
import net.trueog.unionsog.managers.PermissionsManager;
import net.trueog.unionsog.managers.StorageManager;
import net.trueog.unionsog.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.RED;

@CommandAlias("%clan")
@Subcommand("%rank")
@Conditions("%basic_conditions|leader")
public class RankCommand extends BaseCommand {

    @Dependency
    private UnionsOG plugin;
    @Dependency
    private StorageManager storage;
    @Dependency
    private PermissionsManager permissions;

    @Subcommand("%assign")
    @CommandPermission("unionsog.leader.rank.assign")
    @CommandCompletion("@clan_members @ranks")
    @Description("{@@command.description.rank.assign}")
    public void assign(ClanPlayer player, Clan clan, @Name("member") @Conditions("same_clan") ClanPlayerInput member,
            @Name("rank") Rank rank)
    {

        ClanPlayer memberInput = member.getClanPlayer();

        PlayerRankUpdateEvent event = new PlayerRankUpdateEvent(player, memberInput, clan,
                clan.getRank(memberInput.getRankId()), rank);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        if (memberInput.getRankId().equals(rank.getName())) {

            ChatBlock.sendMessage(player, lang("player.already.has.that.rank", player));
            return;

        }

        memberInput.setRank(rank.getName());
        storage.updateClanPlayer(memberInput);
        ChatBlock.sendMessage(player, AQUA + lang("player.rank.changed", player));

    }

    @Subcommand("%unassign")
    @CommandPermission("unionsog.leader.rank.unassign")
    @CommandCompletion("@clan_members")
    @Description("{@@command.description.rank.unassign}")
    public void unassign(ClanPlayer player, Clan clan, @Conditions("same_clan") @Name("member") ClanPlayerInput cp) {

        ClanPlayer memberInput = cp.getClanPlayer();

        PlayerRankUpdateEvent event = new PlayerRankUpdateEvent(player, memberInput, clan,
                clan.getRank(memberInput.getRankId()), null);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        memberInput.setRank(null);
        storage.updateClanPlayer(memberInput);
        ChatBlock.sendMessage(player, AQUA + lang("player.unassigned.from.rank", player));

    }

    @Subcommand("%create")
    @CommandPermission("unionsog.leader.rank.create")
    @Description("{@@command.description.rank.create}")
    public void create(Player player, Clan clan) {

        SCConversation conversation = new SCConversation(plugin, player, new CreateRankNamePrompt(), 60);
        conversation.addConversationCanceller(
                new RequestCanceller(player, RED + lang("rank.create.request.cancelled", player)));
        conversation.getContext().setSessionData("clan", clan);
        conversation.begin();

    }

    @Subcommand("%delete")
    @CommandPermission("unionsog.leader.rank.delete")
    @CommandCompletion("@ranks")
    @Description("{@@command.description.rank.delete}")
    public void delete(Player player, Clan clan, @Name("rank") Rank rank) {

        DeleteRankEvent event = new DeleteRankEvent(player, clan, rank);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {

            clan.deleteRank(rank.getName());
            storage.updateClan(clan, true);
            ChatBlock.sendMessage(player, AQUA + lang("rank.0.deleted", player, rank.getDisplayName()));

        }

    }

    @Subcommand("%list")
    @CommandPermission("unionsog.leader.rank.list")
    @Description("{@@command.description.rank.list}")
    public void list(Player player, Clan clan) {

        List<Rank> ranks = clan.getRanks();

        if (ranks.isEmpty()) {

            ChatBlock.sendMessage(player, RED + lang("no.ranks", player));
            return;

        }

        ranks.sort(Comparator.reverseOrder());
        ChatBlock.sendMessage(player, AQUA + lang("clans.ranks", player));
        int count = 1;
        for (Rank rank : ranks) {

            ChatBlock.sendMessage(player, AQUA + lang("ranks.list.item", player, count,
                    ChatUtils.parseColors(rank.getDisplayName()) + AQUA, rank.getName()));
            count++;

        }

    }

    @Subcommand("%setdisplayname")
    @CommandPermission("unionsog.leader.rank.setdisplayname")
    @CommandCompletion("@ranks @nothing")
    @Description("{@@command.description.rank.setdisplayname}")
    public void setDisplayName(Player player, Clan clan, @Name("rank") Rank rank,
            @Name("displayname") String displayName)
    {

        if (displayName.contains("&") && !permissions.has(player, "unionsog.leader.coloredrank")) {

            ChatBlock.sendMessage(player, RED + lang("you.cannot.set.colored.ranks", player));
            return;

        }

        rank.setDisplayName(displayName);
        storage.updateClan(clan, true);
        ChatBlock.sendMessage(player, ChatColor.AQUA + lang("rank.displayname.updated", player));

    }

    @Subcommand("%setdefault")
    @CommandPermission("unionsog.leader.rank.setdefault")
    @CommandCompletion("@ranks")
    @Description("{@@command.description.rank.setdefault}")
    public void setDefault(Player player, Clan clan, @Name("rank") Rank rank) {

        clan.setDefaultRank(rank.getName());
        ChatBlock.sendMessage(player, AQUA + lang("rank.setdefault", player, rank.getDisplayName()));

    }

    @Subcommand("%removedefault")
    @CommandPermission("unionsog.leader.rank.removedefault")
    @Description("{@@command.description.rank.removedefault}")
    public void removeDefault(Player player, Clan clan) {

        clan.setDefaultRank(null);
        ChatBlock.sendMessage(player, AQUA + lang("rank.removedefault", player));

    }

    @Subcommand("%permissions")
    public class PermissionsCommand extends BaseCommand {

        private final String validPermissionsToMessage = String.join(",", Helper.fromPermissionArray());

        @Default
        @CommandPermission("unionsog.leader.rank.permissions.available")
        @Description("{@@command.description.rank.permissions.available}")
        public void availablePermissions(Player player) {

            ChatBlock.sendMessage(player, AQUA + lang("available.rank.permissions", player));
            ChatBlock.sendMessage(player, AQUA + validPermissionsToMessage);

        }

        @Default
        @CommandPermission("unionsog.leader.rank.permissions.list")
        @CommandCompletion("@ranks")
        @Description("{@@command.description.rank.permissions.rank}")
        public void list(Player player, @Name("rank") Rank rank) {

            Set<String> permissions = rank.getPermissions();
            if (permissions.isEmpty()) {

                ChatBlock.sendMessage(player, RED + lang("rank.no.permissions", player));
                return;

            }

            ChatBlock.sendMessage(player, AQUA + lang("rank.0.permissions", player, rank.getDisplayName()));
            ChatBlock.sendMessage(player, AQUA + String.join(",", permissions));

        }

        @Subcommand("%add")
        @CommandPermission("unionsog.leader.rank.permissions.add")
        @CommandCompletion("@ranks @rank_permissions")
        @Description("{@@command.description.rank.permissions.add}")
        public void add(Player player, Clan clan, @Name("rank") Rank rank,
                @Values("@rank_permissions") @Name("permission") String permission)
        {

            Set<String> permissions = rank.getPermissions();
            permissions.add(permission);
            ChatBlock.sendMessage(player,
                    AQUA + lang("permission.0.added.to.rank.1", player, permission, rank.getDisplayName()));
            storage.updateClan(clan, true);

        }

        @Subcommand("%remove")
        @CommandCompletion("@ranks @rank_permissions")
        @CommandPermission("unionsog.leader.rank.permissions.remove")
        @Description("{@@command.description.rank.permissions.remove}")
        public void remove(Player player, Clan clan, @Name("rank") Rank rank,
                @Values("@rank_permissions") @Name("permission") String permission)
        {

            Set<String> permissions = rank.getPermissions();
            permissions.remove(permission);
            ChatBlock.sendMessage(player,
                    AQUA + lang("permission.0.removed.from.rank.1", player, permission, rank.getDisplayName()));
            storage.updateClan(clan, true);

        }

    }

}
