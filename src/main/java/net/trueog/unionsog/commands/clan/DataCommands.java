package net.trueog.unionsog.commands.clan;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.trueog.unionsog.ChatBlock;
import net.trueog.unionsog.Clan;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.commands.data.*;
import net.trueog.unionsog.utils.VanishUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.RED;

@CommandAlias("%clan")
@Conditions("%basic_conditions")
public class DataCommands extends BaseCommand {

    @Dependency
    private UnionsOG plugin;

    @Subcommand("%vitals")
    @CommandPermission("unionsog.member.vitals")
    @Conditions("rank:name=VITALS")
    @Description("{@@command.description.vitals}")
    public void vitals(CommandSender sender, Clan clan) {

        Vitals vitals = new Vitals(plugin, sender, clan);
        vitals.send();

    }

    @Subcommand("%stats")
    @CommandPermission("unionsog.member.stats")
    @Conditions("rank:name=STATS")
    @Description("{@@command.description.stats}")
    public void stats(Player player, Clan clan) {

        ClanStats stats = new ClanStats(plugin, player, clan);
        stats.send();

    }

    @Subcommand("%profile")
    @CommandPermission("unionsog.member.profile")
    @Description("{@@command.description.profile}")
    public void profile(CommandSender sender, Clan clan) {

        ClanProfile p = new ClanProfile(plugin, sender, clan);
        p.send();

    }

    @Subcommand("%roster")
    @CommandPermission("unionsog.member.roster")
    @Description("{@@command.description.roster}")
    public void roster(Player player, Clan clan) {

        ClanRoster r = new ClanRoster(plugin, player, clan);
        r.send();

    }

    @Subcommand("%coords")
    @CommandPermission("unionsog.member.coords")
    @Conditions("rank:name=COORDS")
    @HelpSearchTags("local location")
    @Description("{@@command.description.coords}")
    public void coords(Player player, Clan clan) {

        if (VanishUtils.getNonVanished(player, clan).size() == 1) {

            ChatBlock.sendMessage(player, RED + lang("you.are.the.only.member.online", player));
            return;

        }

        ClanCoords c = new ClanCoords(plugin, player, clan);
        c.send();

    }

}
