package net.trueog.unionsog.loggers;

import net.trueog.unionsog.Union;
import net.trueog.unionsog.EconomyResponse;
import net.trueog.unionsog.events.UnionBalanceUpdateEvent;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Class responsible for logging bank related transactions.
 *
 * @since 2.15.3
 */
public class BankLog {

    private final DecimalFormat decimalFormat = new DecimalFormat("##.##");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd - HH:mm");

    private final BankOperator operator;
    private final Union union;
    private final EconomyResponse economyResponse;
    private final BankLogger.Operation operation;
    private final UnionBalanceUpdateEvent.Cause cause;
    private final double amount;

    public BankLog(@NotNull BankOperator operator, @NotNull Union union, @NotNull EconomyResponse economyResponse,
            @NotNull BankLogger.Operation operation, UnionBalanceUpdateEvent.Cause cause, double amount)
    {

        this.operator = operator;
        this.union = union;
        this.economyResponse = economyResponse;
        this.operation = operation;
        this.cause = cause;
        this.amount = amount;

    }

    public static List<String> getHeader() {

        return Arrays.asList("Date", "Sender", "Union Name", "Response", "Operation", "Cause", "Sender Balance",
                "Amount", "Union Balance");

    }

    public List<String> getValues() {

        List<String> values = new ArrayList<>();
        values.add(dateFormat.format(new Date()));
        values.add(operator.getName());
        values.add(union.getName());
        values.add(economyResponse.name());
        values.add(operation.name());
        values.add(cause.name());
        values.add(decimalFormat.format(operator.getBalance()));
        values.add(decimalFormat.format(amount));
        values.add(decimalFormat.format(union.getBalance()));

        return values;

    }

    public BankOperator getOperator() {

        return operator;

    }

    public Union getUnion() {

        return union;

    }

    public EconomyResponse getEconomyResponse() {

        return economyResponse;

    }

    public BankLogger.Operation getOperation() {

        return operation;

    }

    public UnionBalanceUpdateEvent.Cause getCause() {

        return cause;

    }

    public double getAmount() {

        return amount;

    }

}
