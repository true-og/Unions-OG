package net.trueog.unionsog.commands.staff;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.trueog.unionsog.ChatBlock;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.commands.UnionInput;
import net.trueog.unionsog.managers.UnionManager;
import net.trueog.unionsog.managers.SettingsManager;
import net.trueog.unionsog.managers.StorageManager;
import org.bukkit.entity.Player;

import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.RED;

@CommandAlias("%union")
@Conditions("%basic_conditions")
@Subcommand("%mod %bb")
public class BbCommand extends BaseCommand {

    @Dependency
    private StorageManager storage;
    @Dependency
    private UnionManager cm;

    @Dependency
    private SettingsManager settings;

    @Subcommand("%display")
    @CommandPermission("unionsog.mod.bb")
    @CommandCompletion("@unions")
    @Description("{@@command.description.mod.bb.display}")
    public void display(Player sender, @Name("union") UnionInput input) {

        input.getUnion().displayBb(sender);

    }

    @Subcommand("%clear")
    @CommandPermission("unionsog.mod.bb-clear")
    @CommandCompletion("@unions")
    @Description("{@@command.description.mod.bb.clear}")
    public void clear(Player player, @Name("union") UnionInput input) {

        input.getUnion().clearBb();
        ChatBlock.sendMessage(player, RED + lang("cleared.bb", player));

    }

    @Subcommand("%add")
    @CommandPermission("unionsog.mod.bb-add")
    @CommandCompletion("@unions @nothing")
    @Description("{@@command.description.mod.bb.post}")
    public void postMessage(Player player, @Name("union") UnionInput input, @Name("message") String msg) {

        Union union = input.getUnion();
        union.addBb(lang("bulletin.board.message", player.getName(), msg));
        union.displayBb(player);
        storage.updateUnion(union);

    }

}
