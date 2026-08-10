package net.trueog.unionsog.commands.union;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.trueog.unionsog.ChatBlock;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.EconomyResponse;
import net.trueog.unionsog.events.BankDepositEvent;
import net.trueog.unionsog.events.BankWithdrawEvent;
import net.trueog.unionsog.loggers.BankLogger;
import net.trueog.unionsog.loggers.BankOperator;
import net.trueog.unionsog.managers.PermissionsManager;
import net.trueog.unionsog.utils.CurrencyFormat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.events.UnionBalanceUpdateEvent.Cause.COMMAND;
import static net.trueog.unionsog.events.UnionBalanceUpdateEvent.Cause.REVERT;
import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.RED;

@CommandAlias("%union")
@Subcommand("%bank")
@Conditions("%basic_conditions|economy")
public class BankCommand extends BaseCommand {

    @Dependency
    private PermissionsManager permissions;

    @Subcommand("%status")
    @CommandPermission("unionsog.member.bank")
    @Conditions("union_member")
    @Description("{@@command.description.bank.status}")
    public void bankStatus(Player player, Union union) {

        player.sendMessage(AQUA + lang("union.balance", player, union.getBalanceFormatted()));

    }

    @Subcommand("%withdraw %all")
    @CommandPermission("unionsog.member.bank")
    @Conditions("union_member")
    @Description("{@@command.description.bank.withdraw.all}")
    public void bankWithdraw(Player player, Union union) {

        processWithdraw(player, union, (long) union.getBalance());

    }

    @Subcommand("%withdraw")
    @CommandPermission("unionsog.member.bank")
    @Conditions("union_member")
    @Description("{@@command.description.bank.withdraw.amount}")
    public void bankWithdraw(Player player, Union union, double amount) {

        long shards = permissions.diamondsToShards(Math.abs(amount));
        if (shards < 0) {

            ChatBlock.sendMessage(player, RED + lang("invalid.diamond.amount", player));
            return;

        }

        processWithdraw(player, union, shards);

    }

    private void processWithdraw(Player player, Union union, long shards) {

        if (!union.isAllowWithdraw()) {

            String message = getCurrentCommandManager().getCommandReplacements()
                    .replace(lang("withdraw.not.allowed", player));
            ChatBlock.sendMessage(player, RED + message);
            return;

        }

        /*
         * TODO: Remove at UnionsOG 3.0
         */
        BankWithdrawEvent event = new BankWithdrawEvent(player, union, shards);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {

            return;

        }

        /*
         * ——————————————————————————————————
         */
        BankOperator operator = new BankOperator(player, permissions.playerGetShards(player));
        switch (union.withdraw(operator, COMMAND, shards)) {

            case SUCCESS:
                if (permissions.grantPlayerShards(player, shards, null)) {

                    player.sendMessage(
                            AQUA + lang("player.union.withdraw", player, CurrencyFormat.formatShards(shards, player)));
                    union.addBb(player.getName(),
                            lang("bb.union.withdraw", CurrencyFormat.formatShards(shards), player.getName()));

                } else {

                    union.setBalance(operator, REVERT, BankLogger.Operation.WITHDRAW, union.getBalance() + shards);

                }
                break;
            case NOT_ENOUGH_BALANCE:
                player.sendMessage(lang("union.bank.not.enough.money", player));

        }

    }

    @Subcommand("%deposit %all")
    @CommandPermission("unionsog.member.bank")
    @Conditions("union_member")
    @Description("{@@command.description.bank.deposit.all}")
    public void bankDeposit(Player player, Union union) {

        processDeposit(player, union, permissions.playerGetShards(player));

    }

    @Subcommand("%deposit")
    @CommandPermission("unionsog.member.bank")
    @Conditions("union_member")
    @Description("{@@command.description.bank.deposit.amount}")
    public void bankDeposit(Player player, Union union, double amount) {

        long shards = permissions.diamondsToShards(Math.abs(amount));
        if (shards < 0) {

            ChatBlock.sendMessage(player, RED + lang("invalid.diamond.amount", player));
            return;

        }

        processDeposit(player, union, shards);

    }

    private void processDeposit(Player player, Union union, long shards) {

        if (!union.isAllowDeposit()) {

            String message = getCurrentCommandManager().getCommandReplacements()
                    .replace(lang("deposit.not.allowed", player));
            ChatBlock.sendMessage(player, RED + message);
            return;

        }

        /*
         * TODO: Remove at UnionsOG 3.0
         */
        BankDepositEvent event = new BankDepositEvent(player, union, shards);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {

            return;

        }
        /*
         * ——————————————————————————————————
         */

        if (!permissions.playerHasShards(player, shards)) {

            player.sendMessage(
                    AQUA + lang("not.sufficient.money", player, CurrencyFormat.formatShards(shards, player)));
            return;

        }

        BankOperator operator = new BankOperator(player, permissions.playerGetShards(player));
        EconomyResponse response = union.deposit(operator, COMMAND, shards);
        if (response == EconomyResponse.SUCCESS) {

            if (permissions.chargePlayerShards(player, shards, null)) {

                player.sendMessage(
                        AQUA + lang("player.union.deposit", player, CurrencyFormat.formatShards(shards, player)));
                union.addBb(player.getName(),
                        lang("bb.union.deposit", CurrencyFormat.formatShards(shards), player.getName()));

            } else {

                // Reverts the deposit if the DiamondBank-OG charge failed
                union.setBalance(operator, REVERT, BankLogger.Operation.DEPOSIT, union.getBalance() - shards);

            }

        }

    }

}
