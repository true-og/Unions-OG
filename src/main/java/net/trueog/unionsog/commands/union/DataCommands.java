package net.trueog.unionsog.commands.union;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.trueog.unionsog.ChatBlock;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.commands.data.*;
import net.trueog.unionsog.utils.VanishUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.RED;

@CommandAlias("%union")
@Conditions("%basic_conditions")
public class DataCommands extends BaseCommand {

    @Dependency
    private UnionsOG plugin;

    @Subcommand("%vitals")
    @CommandPermission("unionsog.member.vitals")
    @Conditions("union_member")
    @Description("{@@command.description.vitals}")
    public void vitals(CommandSender sender, Union union) {

        Vitals vitals = new Vitals(plugin, sender, union);
        vitals.send();

    }

    @Subcommand("%stats")
    @CommandPermission("unionsog.member.stats")
    @Conditions("union_member")
    @Description("{@@command.description.stats}")
    public void stats(Player player, Union union) {

        UnionStats stats = new UnionStats(plugin, player, union);
        stats.send();

    }

    @Subcommand("%profile")
    @CommandPermission("unionsog.member.profile")
    @Description("{@@command.description.profile}")
    public void profile(CommandSender sender, Union union) {

        UnionProfile p = new UnionProfile(plugin, sender, union);
        p.send();

    }

    @Subcommand("%roster")
    @CommandPermission("unionsog.member.roster")
    @Description("{@@command.description.roster}")
    public void roster(Player player, Union union) {

        UnionRoster r = new UnionRoster(plugin, player, union);
        r.send();

    }

    @Subcommand("%coords")
    @CommandPermission("unionsog.member.coords")
    @Conditions("union_member")
    @HelpSearchTags("local location")
    @Description("{@@command.description.coords}")
    public void coords(Player player, Union union) {

        if (VanishUtils.getNonVanished(player, union).size() == 1) {

            ChatBlock.sendMessage(player, RED + lang("you.are.the.only.member.online", player));
            return;

        }

        UnionCoords c = new UnionCoords(plugin, player, union);
        c.send();

    }

}
