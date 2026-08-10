package net.trueog.unionsog.commands.union;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.trueog.unionsog.*;
import net.trueog.unionsog.events.HomeRegroupEvent;
import net.trueog.unionsog.events.PlayerHomeClearEvent;
import net.trueog.unionsog.events.PlayerHomeSetEvent;
import net.trueog.unionsog.managers.UnionManager;
import net.trueog.unionsog.managers.PermissionsManager;
import net.trueog.unionsog.managers.ProposalManager;
import net.trueog.unionsog.managers.ProtectionManager;
import net.trueog.unionsog.managers.SettingsManager;
import net.trueog.unionsog.utils.VanishUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;
import static org.bukkit.ChatColor.*;

@CommandAlias("%union")
@Conditions("%basic_conditions")
public class HomeCommands extends BaseCommand {

    @Dependency
    private UnionsOG plugin;
    @Dependency
    private PermissionsManager permissions;
    @Dependency
    private SettingsManager settings;
    @Dependency
    private ProtectionManager protection;
    @Dependency
    private ProposalManager proposalManager;
    @Dependency
    private UnionManager cm;

    @Subcommand("%regroup %me")
    @CommandPermission("unionsog.member.regroup.me")
    @Conditions("union_member")
    @Description("{@@command.description.regroup.me}")
    public void regroupMe(Player player, UnionPlayer cp, Union union) {

        if (!settings.is(ALLOW_REGROUP)) {

            ChatBlock.sendMessage(player, RED + lang("insufficient.permissions", player));
            return;

        }

        processRegroup(player, cp, union, player.getLocation());

    }

    private void processRegroup(Player player, UnionPlayer cp, Union union, Location location) {

        HomeRegroupEvent homeRegroupEvent = new HomeRegroupEvent(union, cp, VanishUtils.getNonVanished(player, union),
                location);
        plugin.getServer().getPluginManager().callEvent(homeRegroupEvent);

        if (homeRegroupEvent.isCancelled() || !cm.purchaseHomeRegroup(player)) {

            return;

        }

        plugin.getTeleportManager().teleport(player, union, location);

    }

    @Subcommand("%regroup %home")
    @CommandPermission("unionsog.member.regroup.home")
    @Conditions("union_member")
    @Description("{@@command.description.regroup.home}")
    public void regroupHome(Player player, UnionPlayer cp, @Conditions("can_teleport") Union union) {

        if (!settings.is(ALLOW_REGROUP)) {

            ChatBlock.sendMessage(player, RED + lang("insufficient.permissions", player));
            return;

        }

        Location location = union.getHomeLocation();
        processRegroup(player, cp, union, location);

    }

    @Subcommand("%home")
    @CommandPermission("unionsog.member.home")
    @Conditions("union_member")
    @Description("{@@command.description.home.tp}")
    public void teleport(Player player, @Conditions("can_teleport") Union union, UnionPlayer cp) {

        Location homeLocation = union.getHomeLocation();

        if (cm.purchaseHomeTeleport(player)) {

            plugin.getTeleportManager().addPlayer(player, homeLocation, union.getName());

        }

    }

    @Subcommand("%home %clear")
    @CommandPermission("unionsog.member.home-set")
    @Conditions("union_member")
    @Description("{@@command.description.home.clear}")
    public void clear(Player player, UnionPlayer cp, Union union) {

        if (settings.is(UNION_HOMEBASE_CAN_BE_SET_ONLY_ONCE) && union.getHomeLocation() != null
                && !permissions.has(player, "unionsog.mod.home"))
        {

            ChatBlock.sendMessage(player, RED + lang("home.base.only.once", player));
            return;

        }

        PlayerHomeClearEvent event = new PlayerHomeClearEvent(union, cp);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {

            return;

        }

        union.setHomeLocation(null);
        ChatBlock.sendMessage(player, AQUA + lang("hombase.cleared", player));

    }

    @Subcommand("%home %set")
    @Conditions("union_member")
    @CommandPermission("unionsog.member.home-set")
    @Description("{@@command.description.home.set}")
    public void set(Player player, UnionPlayer cp, Union union) {

        if (settings.is(UNION_HOMEBASE_CAN_BE_SET_ONLY_ONCE) && union.getHomeLocation() != null
                && !permissions.has(player, "unionsog.mod.home"))
        {

            ChatBlock.sendMessage(player, RED + lang("home.base.only.once", player));
            return;

        }

        if (settings.is(LAND_SET_BASE_ONLY_IN_LAND)) {

            if (!protection.isOwner(player, player.getLocation())) {

                ChatBlock.sendMessageKey(player, "you.can.only.set.base.in.your.land");
                return;

            }

        }

        PlayerHomeSetEvent homeSetEvent = new PlayerHomeSetEvent(union, cp, player.getLocation());
        plugin.getServer().getPluginManager().callEvent(homeSetEvent);

        if (homeSetEvent.isCancelled()) {

            return;

        }

        // The home is only paid for and moved once the union votes it through.
        proposalManager.propose(cp, union, ProposalType.SET_HOME,
                ProposalManager.toLocationTarget(player.getLocation()));

    }

}
