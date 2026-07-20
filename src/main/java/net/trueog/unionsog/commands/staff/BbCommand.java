package net.trueog.unionsog.commands.staff;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.trueog.unionsog.ChatBlock;
import net.trueog.unionsog.Clan;
import net.trueog.unionsog.commands.ClanInput;
import net.trueog.unionsog.managers.ClanManager;
import net.trueog.unionsog.managers.SettingsManager;
import net.trueog.unionsog.managers.StorageManager;
import org.bukkit.entity.Player;

import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.RED;

@CommandAlias("%clan")
@Conditions("%basic_conditions")
@Subcommand("%mod %bb")
public class BbCommand extends BaseCommand {

    @Dependency
    private StorageManager storage;
    @Dependency
    private ClanManager cm;

    @Dependency
    private SettingsManager settings;

    @Subcommand("%display")
    @CommandPermission("unionsog.mod.bb")
    @CommandCompletion("@clans")
    @Description("{@@command.description.mod.bb.display}")
    public void display(Player sender, @Name("clan") ClanInput input) {

        input.getClan().displayBb(sender);

    }

    @Subcommand("%clear")
    @CommandPermission("unionsog.mod.bb-clear")
    @CommandCompletion("@clans")
    @Description("{@@command.description.mod.bb.clear}")
    public void clear(Player player, @Name("clan") ClanInput input) {

        input.getClan().clearBb();
        ChatBlock.sendMessage(player, RED + lang("cleared.bb", player));

    }

    @Subcommand("%add")
    @CommandPermission("unionsog.mod.bb-add")
    @CommandCompletion("@clans @nothing")
    @Description("{@@command.description.mod.bb.post}")
    public void postMessage(Player player, @Name("clan") ClanInput input, @Name("message") String msg) {

        Clan clan = input.getClan();
        clan.addBb(lang("bulletin.board.message", player.getName(), msg));
        clan.displayBb(player);
        storage.updateClan(clan);

    }

}
