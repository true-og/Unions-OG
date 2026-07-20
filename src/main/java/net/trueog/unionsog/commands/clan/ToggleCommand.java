package net.trueog.unionsog.commands.clan;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.trueog.unionsog.ChatBlock;
import net.trueog.unionsog.ClanPlayer;
import net.trueog.unionsog.managers.StorageManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.AQUA;

@CommandAlias("%clan")
@Subcommand("%toggle")
@Conditions("%basic_conditions")
public class ToggleCommand extends BaseCommand {

    @Dependency
    private StorageManager storage;

    @Conditions("verified")
    public class Verified extends BaseCommand {

        @Subcommand("%bb")
        @CommandPermission("unionsog.member.bb-toggle")
        @Description("{@@command.description.toggle.bb}")
        public void bb(Player player, ClanPlayer cp) {

            toggle(player, "bbon", "bboff", cp.isBbEnabled(), cp::setBbEnabled);

        }

        @Subcommand("%tag")
        @CommandPermission("unionsog.member.tag-toggle")
        @Description("{@@command.description.toggle.tag}")
        public void tag(Player player, ClanPlayer cp) {

            toggle(player, "tagon", "tagoff", cp.isTagEnabled(), cp::setTagEnabled);

        }

        // TODO: start - restore these two subcommands with UnionBankZeroMigration once
        // union bank accounts exist. They only gate the union bank commands, which are
        // unregistered until then. Restoring them also needs the StorageManager
        // dependency below and an import of net.trueog.unionsog.Clan.
        //
        // @Subcommand("%deposit")
        // @CommandPermission("unionsog.leader.deposit-toggle")
        // @Conditions("leader")
        // @Description("{@@command.description.toggle.deposit}")
        // public void deposit(Player player, Clan clan) {
        //
        // toggle(player, "depositon", "depositoff", clan.isAllowDeposit(),
        // clan::setAllowDeposit);
        //
        // storage.updateClan(clan);
        //
        // }
        //
        // @Subcommand("%withdraw")
        // @CommandPermission("unionsog.leader.withdraw-toggle")
        // @Conditions("leader")
        // @Description("{@@command.description.toggle.withdraw}")
        // public void withdraw(Player player, Clan clan) {
        //
        // toggle(player, "withdrawon", "withdrawoff", clan.isAllowWithdraw(),
        // clan::setAllowWithdraw);
        //
        // storage.updateClan(clan);
        //
        // }
        // TODO: end

    }

    @Subcommand("%invite")
    @CommandPermission("unionsog.anyone.invite-toggle")
    @Description("{@@command.description.toggle.invite}")
    public void invite(Player player, ClanPlayer cp) {

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
