package net.trueog.unionsog.commands.staff;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.trueog.unionsog.ChatBlock;
import net.trueog.unionsog.Clan;
import net.trueog.unionsog.EconomyResponse;
import net.trueog.unionsog.commands.ClanInput;
import net.trueog.unionsog.events.ClanBalanceUpdateEvent;
import net.trueog.unionsog.loggers.BankLogger;
import net.trueog.unionsog.loggers.BankOperator;
import net.trueog.unionsog.managers.PermissionsManager;
import net.trueog.unionsog.utils.CurrencyFormat;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.md_5.bungee.api.ChatColor.RED;
import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.AQUA;

@CommandAlias("%clan")
@Conditions("%basic_conditions")
@Subcommand("%admin %bank")
public class BankCommand extends BaseCommand {

    @Dependency
    private PermissionsManager permissions;

    @Subcommand("%status")
    @CommandPermission("unionsog.admin.bank.status")
    @CommandCompletion("@clans")
    @Description("{@@command.description.bank.admin.status}")
    public void status(CommandSender sender, @Name("clan") ClanInput clanInput) {

        Clan clan = clanInput.getClan();
        ChatBlock.sendMessage(sender,
                AQUA + lang("clan.admin.balance", sender, clan.getName(), clan.getBalanceFormatted()));

    }

    @Subcommand("%take")
    @CommandPermission("unionsog.admin.bank.take")
    @CommandCompletion("@clans")
    @Description("{@@command.description.bank.admin.take}")
    public void take(CommandSender sender, @Name("clan") ClanInput clanInput, @Name("amount") double amount) {

        Clan clan = clanInput.getClan();
        long shards = permissions.diamondsToShards(Math.abs(amount));
        if (shards < 0) {

            ChatBlock.sendMessage(sender, RED + lang("invalid.diamond.amount", sender));
            return;

        }

        BankOperator operator = new BankOperator(sender,
                sender instanceof Player ? permissions.playerGetShards((Player) sender) : 0);

        EconomyResponse economyResponse = clan.withdraw(operator, ClanBalanceUpdateEvent.Cause.COMMAND, shards);
        switch (economyResponse) {

            case SUCCESS:
                ChatBlock.sendMessage(sender, AQUA
                        + lang("clan.admin.take", sender, CurrencyFormat.formatShards(shards, sender), clan.getName()));
                clan.addBb(sender.getName(),
                        lang("bb.clan.take", sender, CurrencyFormat.formatShards(shards), sender.getName()));
                break;
            case NOT_ENOUGH_BALANCE:
                sender.sendMessage(RED + lang("clan.admin.bank.not.enough.money", sender, clan.getName()));
                break;

        }

    }

    @Subcommand("%give")
    @CommandPermission("unionsog.admin.bank.give")
    @CommandCompletion("@clans")
    @Description("{@@command.description.bank.admin.give}")
    public void give(CommandSender sender, @Name("clan") ClanInput clanInput, @Name("amount") double amount) {

        Clan clan = clanInput.getClan();
        long shards = permissions.diamondsToShards(Math.abs(amount));
        if (shards < 0) {

            ChatBlock.sendMessage(sender, RED + lang("invalid.diamond.amount", sender));
            return;

        }

        BankOperator operator = new BankOperator(sender,
                sender instanceof Player ? permissions.playerGetShards((Player) sender) : 0);

        EconomyResponse economyResponse = clan.deposit(operator, ClanBalanceUpdateEvent.Cause.COMMAND, shards);
        if (economyResponse == EconomyResponse.SUCCESS) {

            ChatBlock.sendMessage(sender, AQUA
                    + lang("clan.admin.give", sender, CurrencyFormat.formatShards(shards, sender), clan.getName()));
            clan.addBb(sender.getName(),
                    lang("bb.clan.give", sender, CurrencyFormat.formatShards(shards), sender.getName()));

        }

    }

    @Subcommand("%set")
    @CommandPermission("unionsog.admin.bank.set")
    @CommandCompletion("@clans")
    @Description("{@@command.description.bank.admin.set}")
    public void set(CommandSender sender, @Name("clan") ClanInput clanInput, @Name("amount") double amount) {

        Clan clan = clanInput.getClan();
        long shards = permissions.diamondsToShards(Math.abs(amount));
        if (shards < 0) {

            ChatBlock.sendMessage(sender, RED + lang("invalid.diamond.amount", sender));
            return;

        }

        BankOperator operator = new BankOperator(sender,
                sender instanceof Player ? permissions.playerGetShards((Player) sender) : 0);

        EconomyResponse response = clan.setBalance(operator, ClanBalanceUpdateEvent.Cause.COMMAND,
                BankLogger.Operation.SET, shards);
        if (response == EconomyResponse.SUCCESS) {

            ChatBlock.sendMessage(sender,
                    AQUA + lang("clan.admin.set", sender, clan.getName(), CurrencyFormat.formatShards(shards, sender)));
            clan.addBb(sender.getName(),
                    lang("bb.clan.set", sender, CurrencyFormat.formatShards(shards), sender.getName()));

        }

    }

}
