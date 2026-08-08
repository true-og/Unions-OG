package net.trueog.unionsog.commands.clan;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.trueog.unionsog.*;
import net.trueog.unionsog.events.HomeRegroupEvent;
import net.trueog.unionsog.events.PlayerHomeClearEvent;
import net.trueog.unionsog.events.PlayerHomeSetEvent;
import net.trueog.unionsog.managers.ClanManager;
import net.trueog.unionsog.managers.PermissionsManager;
import net.trueog.unionsog.managers.ProtectionManager;
import net.trueog.unionsog.managers.SettingsManager;
import net.trueog.unionsog.utils.VanishUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;
import static org.bukkit.ChatColor.*;

@CommandAlias("%clan")
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
    private ClanManager cm;

    @Subcommand("%regroup %me")
    @CommandPermission("unionsog.leader.regroup.me")
    @Conditions("rank:name=REGROUP_ME")
    @Description("{@@command.description.regroup.me}")
    public void regroupMe(Player player, ClanPlayer cp, Clan clan) {

        if (!settings.is(ALLOW_REGROUP)) {

            ChatBlock.sendMessage(player, RED + lang("insufficient.permissions", player));
            return;

        }

        processRegroup(player, cp, clan, player.getLocation());

    }

    private void processRegroup(Player player, ClanPlayer cp, Clan clan, Location location) {

        HomeRegroupEvent homeRegroupEvent = new HomeRegroupEvent(clan, cp, VanishUtils.getNonVanished(player, clan),
                location);
        plugin.getServer().getPluginManager().callEvent(homeRegroupEvent);

        if (homeRegroupEvent.isCancelled() || !cm.purchaseHomeRegroup(player)) {

            return;

        }

        plugin.getTeleportManager().teleport(player, clan, location);

    }

    @Subcommand("%regroup %home")
    @CommandPermission("unionsog.leader.regroup.home")
    @Conditions("rank:name=REGROUP_HOME")
    @Description("{@@command.description.regroup.home}")
    public void regroupHome(Player player, ClanPlayer cp, @Conditions("can_teleport") Clan clan) {

        if (!settings.is(ALLOW_REGROUP)) {

            ChatBlock.sendMessage(player, RED + lang("insufficient.permissions", player));
            return;

        }

        Location location = clan.getHomeLocation();
        processRegroup(player, cp, clan, location);

    }

    @Subcommand("%home")
    @CommandPermission("unionsog.member.home")
    @Conditions("rank:name=HOME_TP")
    @Description("{@@command.description.home.tp}")
    public void teleport(Player player, @Conditions("can_teleport") Clan clan, ClanPlayer cp) {

        Location homeLocation = clan.getHomeLocation();

        if (cm.purchaseHomeTeleport(player)) {

            plugin.getTeleportManager().addPlayer(player, homeLocation, clan.getName());

        }

    }

    @Subcommand("%home %clear")
    @CommandPermission("unionsog.leader.home-set")
    @Conditions("rank:name=HOME_SET")
    @Description("{@@command.description.home.clear}")
    public void clear(Player player, ClanPlayer cp, Clan clan) {

        if (settings.is(CLAN_HOMEBASE_CAN_BE_SET_ONLY_ONCE) && clan.getHomeLocation() != null
                && !permissions.has(player, "unionsog.mod.home"))
        {

            ChatBlock.sendMessage(player, RED + lang("home.base.only.once", player));
            return;

        }

        PlayerHomeClearEvent event = new PlayerHomeClearEvent(clan, cp);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {

            return;

        }

        clan.setHomeLocation(null);
        ChatBlock.sendMessage(player, AQUA + lang("hombase.cleared", player));

    }

    @Subcommand("%home %set")
    @Conditions("rank:name=HOME_SET")
    @CommandPermission("unionsog.leader.home-set")
    @Description("{@@command.description.home.set}")
    public void set(Player player, ClanPlayer cp, Clan clan) {

        if (settings.is(CLAN_HOMEBASE_CAN_BE_SET_ONLY_ONCE) && clan.getHomeLocation() != null
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

        PlayerHomeSetEvent homeSetEvent = new PlayerHomeSetEvent(clan, cp, player.getLocation());
        plugin.getServer().getPluginManager().callEvent(homeSetEvent);

        if (homeSetEvent.isCancelled() || !cm.purchaseHomeTeleportSet(player)) {

            return;

        }

        clan.setHomeLocation(player.getLocation());
        ChatBlock.sendMessage(player,
                AQUA + lang("hombase.set", player, YELLOW + Helper.toLocationString(player.getLocation())));

    }

}
