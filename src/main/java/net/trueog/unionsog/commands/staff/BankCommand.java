package net.trueog.unionsog.commands.staff;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.trueog.unionsog.ChatBlock;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.EconomyResponse;
import net.trueog.unionsog.commands.UnionInput;
import net.trueog.unionsog.events.UnionBalanceUpdateEvent;
import net.trueog.unionsog.loggers.BankLogger;
import net.trueog.unionsog.loggers.BankOperator;
import net.trueog.unionsog.managers.PermissionsManager;
import net.trueog.unionsog.utils.CurrencyFormat;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.md_5.bungee.api.ChatColor.RED;
import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.AQUA;

@CommandAlias("%union")
@Conditions("%basic_conditions")
@Subcommand("%admin %bank")
public class BankCommand extends BaseCommand {

    @Dependency
    private PermissionsManager permissions;

    @Subcommand("%status")
    @CommandPermission("unionsog.admin.bank.status")
    @CommandCompletion("@unions")
    @Description("{@@command.description.bank.admin.status}")
    public void status(CommandSender sender, @Name("union") UnionInput unionInput) {

        Union union = unionInput.getUnion();
        ChatBlock.sendMessage(sender,
                AQUA + lang("union.admin.balance", sender, union.getName(), union.getBalanceFormatted()));

    }

    @Subcommand("%take")
    @CommandPermission("unionsog.admin.bank.take")
    @CommandCompletion("@unions")
    @Description("{@@command.description.bank.admin.take}")
    public void take(CommandSender sender, @Name("union") UnionInput unionInput, @Name("amount") double amount) {

        Union union = unionInput.getUnion();
        long shards = permissions.diamondsToShards(Math.abs(amount));
        if (shards < 0) {

            ChatBlock.sendMessage(sender, RED + lang("invalid.diamond.amount", sender));
            return;

        }

        BankOperator operator = new BankOperator(sender,
                sender instanceof Player ? permissions.playerGetShards((Player) sender) : 0);

        EconomyResponse economyResponse = union.withdraw(operator, UnionBalanceUpdateEvent.Cause.COMMAND, shards);
        switch (economyResponse) {

            case SUCCESS:
                ChatBlock.sendMessage(sender, AQUA + lang("union.admin.take", sender,
                        CurrencyFormat.formatShards(shards, sender), union.getName()));
                union.addBb(sender.getName(),
                        lang("bb.union.take", sender, CurrencyFormat.formatShards(shards), sender.getName()));
                break;
            case NOT_ENOUGH_BALANCE:
                sender.sendMessage(RED + lang("union.admin.bank.not.enough.money", sender, union.getName()));
                break;

        }

    }

    @Subcommand("%give")
    @CommandPermission("unionsog.admin.bank.give")
    @CommandCompletion("@unions")
    @Description("{@@command.description.bank.admin.give}")
    public void give(CommandSender sender, @Name("union") UnionInput unionInput, @Name("amount") double amount) {

        Union union = unionInput.getUnion();
        long shards = permissions.diamondsToShards(Math.abs(amount));
        if (shards < 0) {

            ChatBlock.sendMessage(sender, RED + lang("invalid.diamond.amount", sender));
            return;

        }

        BankOperator operator = new BankOperator(sender,
                sender instanceof Player ? permissions.playerGetShards((Player) sender) : 0);

        EconomyResponse economyResponse = union.deposit(operator, UnionBalanceUpdateEvent.Cause.COMMAND, shards);
        if (economyResponse == EconomyResponse.SUCCESS) {

            ChatBlock.sendMessage(sender, AQUA
                    + lang("union.admin.give", sender, CurrencyFormat.formatShards(shards, sender), union.getName()));
            union.addBb(sender.getName(),
                    lang("bb.union.give", sender, CurrencyFormat.formatShards(shards), sender.getName()));

        }

    }

    @Subcommand("%set")
    @CommandPermission("unionsog.admin.bank.set")
    @CommandCompletion("@unions")
    @Description("{@@command.description.bank.admin.set}")
    public void set(CommandSender sender, @Name("union") UnionInput unionInput, @Name("amount") double amount) {

        Union union = unionInput.getUnion();
        long shards = permissions.diamondsToShards(Math.abs(amount));
        if (shards < 0) {

            ChatBlock.sendMessage(sender, RED + lang("invalid.diamond.amount", sender));
            return;

        }

        BankOperator operator = new BankOperator(sender,
                sender instanceof Player ? permissions.playerGetShards((Player) sender) : 0);

        EconomyResponse response = union.setBalance(operator, UnionBalanceUpdateEvent.Cause.COMMAND,
                BankLogger.Operation.SET, shards);
        if (response == EconomyResponse.SUCCESS) {

            ChatBlock.sendMessage(sender, AQUA
                    + lang("union.admin.set", sender, union.getName(), CurrencyFormat.formatShards(shards, sender)));
            union.addBb(sender.getName(),
                    lang("bb.union.set", sender, CurrencyFormat.formatShards(shards), sender.getName()));

        }

    }

}
