package net.trueog.unionsog.commands.union;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.trueog.unionsog.ChatBlock;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.managers.UnionManager;
import net.trueog.unionsog.managers.SettingsManager;
import net.trueog.unionsog.managers.StorageManager;
import org.bukkit.entity.Player;

import java.util.Objects;

import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.RED;

@CommandAlias("%union")
@Conditions("%basic_conditions")
public class BbCommand extends BaseCommand {

    @Dependency
    private UnionManager cm;
    @Dependency
    private StorageManager storage;
    @Dependency
    private SettingsManager settings;

    @Subcommand("%bb")
    @CommandPermission("unionsog.member.bb")
    @Conditions("union_member")
    @Description("{@@command.description.bb.display}")
    public void display(Player sender) {

        Union union = Objects.requireNonNull(cm.getUnionByPlayerUniqueId(sender.getUniqueId()));
        union.displayBb(sender);

    }

    @Subcommand("%bb %clear")
    @CommandPermission("unionsog.member.bb-clear")
    @Conditions("union_member")
    @Description("{@@command.description.bb.clear}")
    public void clear(Player player) {

        Union union = Objects.requireNonNull(cm.getUnionByPlayerUniqueId(player.getUniqueId()));
        union.clearBb();
        ChatBlock.sendMessage(player, RED + lang("cleared.bb", player));

    }

    @Subcommand("%bb %add")
    @CommandPermission("unionsog.member.bb-add")
    @Conditions("union_member")
    @Description("{@@command.description.bb.post}")
    public void postMessage(Player player, @Name("message") String msg) {

        Union union = Objects.requireNonNull(cm.getUnionByPlayerUniqueId(player.getUniqueId()));
        union.addBb(lang("bulletin.board.message", player.getName(), msg));
        union.displayBb(player);
        storage.updateUnion(union);

    }

}
