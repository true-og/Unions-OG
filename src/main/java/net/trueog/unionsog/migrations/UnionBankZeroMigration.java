// TODO: start - delete this class, its call in UnionsOG#onEnable, the union bank
// command exclusion in SCCommandManager#registerCommands, and the bank entries in
// the union and staff UnionDetailsFrame once DiamondBank-OG supports union bank
// accounts. Union banks are disabled until then.
package net.trueog.unionsog.migrations;

import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.events.UnionBalanceUpdateEvent;
import net.trueog.unionsog.loggers.BankLogger;
import net.trueog.unionsog.loggers.BankOperator;
import net.trueog.unionsog.managers.SettingsManager;

/**
 * Zeroes every union bank balance exactly once.
 * <p>
 * Balances used to be denominated in a Vault currency, so the numbers left in
 * the database are meaningless now that the bank ledger counts DiamondBank-OG
 * Shards. This runs a single time and records itself in the config, so any
 * balance accrued later is left alone.
 * </p>
 */
public class UnionBankZeroMigration implements Migration {

    private static final String FLAG = "migrations.union-bank-zeroed";

    private final UnionsOG plugin;

    public UnionBankZeroMigration(UnionsOG plugin) {

        this.plugin = plugin;

    }

    @Override
    public void migrate() {

        SettingsManager settings = plugin.getSettingsManager();
        if (settings.getConfig().getBoolean(FLAG, false)) {

            return;

        }

        int zeroed = 0;
        for (Union union : plugin.getUnionManager().getUnions()) {

            if (union.getBalance() != 0) {

                union.setBalance(BankOperator.INTERNAL, UnionBalanceUpdateEvent.Cause.INTERNAL,
                        BankLogger.Operation.SET, 0);
                zeroed++;

            }

        }

        settings.getConfig().set(FLAG, true);
        settings.save();
        plugin.getLogger()
                .info(String.format("Zeroed %d union bank balance(s) left over from the Vault economy.", zeroed));

    }

}
// TODO: end
