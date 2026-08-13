package net.trueog.unionsog.commands.union;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.trueog.unionsog.ChatBlock;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.managers.StorageManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.AQUA;

@CommandAlias("%union")
@Subcommand("%toggle")
@Conditions("%basic_conditions")
public class ToggleCommand extends BaseCommand {

    @Dependency
    private StorageManager storage;

    @Subcommand("%bb")
    @CommandPermission("unionsog.member.bb-toggle")
    @Description("{@@command.description.toggle.bb}")
    public void bb(Player player, UnionPlayer cp) {

        toggle(player, "bbon", "bboff", cp.isBbEnabled(), cp::setBbEnabled);

    }

    @Subcommand("%tag")
    @CommandPermission("unionsog.member.tag-toggle")
    @Description("{@@command.description.toggle.tag}")
    public void tag(Player player, UnionPlayer cp) {

        toggle(player, "tagon", "tagoff", cp.isTagEnabled(), cp::setTagEnabled);

    }

    // TODO: start - restore these two subcommands with UnionBankZeroMigration once
    // union bank accounts exist. They only gate the union bank commands, which are
    // unregistered until then. Restoring them also needs the StorageManager
    // dependency below and an import of net.trueog.unionsog.Union.
    //
    // @Subcommand("%deposit")
    // @CommandPermission("unionsog.member.deposit-toggle")
    // @Conditions("union_member")
    // @Description("{@@command.description.toggle.deposit}")
    // public void deposit(Player player, Union union) {
    //
    // toggle(player, "depositon", "depositoff", union.isAllowDeposit(),
    // union::setAllowDeposit);
    //
    // storage.updateUnion(union);
    //
    // }
    //
    // @Subcommand("%withdraw")
    // @CommandPermission("unionsog.member.withdraw-toggle")
    // @Conditions("union_member")
    // @Description("{@@command.description.toggle.withdraw}")
    // public void withdraw(Player player, Union union) {
    //
    // toggle(player, "withdrawon", "withdrawoff", union.isAllowWithdraw(),
    // union::setAllowWithdraw);
    //
    // storage.updateUnion(union);
    //
    // }
    // TODO: end

    @Subcommand("%invite")
    @CommandPermission("unionsog.anyone.invite-toggle")
    @Description("{@@command.description.toggle.invite}")
    public void invite(Player player, UnionPlayer cp) {

        toggle(player, "inviteon", "inviteoff", cp.isInviteEnabled(), cp::setInviteEnabled);

    }

    private void toggle(CommandSender sender, String onMessageKey, String offMessageKey, boolean status,
            Consumer<Boolean> consumer)
    {

        String messageOn = AQUA + lang(onMessageKey, sender);
        String messageOff = AQUA + lang(offMessageKey, sender);

        ChatBlock.sendMessage(sender, status ? messageOff : messageOn);
        consumer.accept(!status);

    }

}
