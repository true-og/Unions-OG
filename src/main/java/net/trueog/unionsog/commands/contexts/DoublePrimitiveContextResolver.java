package net.trueog.unionsog.commands.contexts;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.MessageKeys;
import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.commands.contexts.Contexts.validateMinMax;

@SuppressWarnings("unused")
public class DoublePrimitiveContextResolver extends AbstractInputOnlyContextResolver<Double> {

    public DoublePrimitiveContextResolver(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public Double getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {

        String number = context.popFirstArg();
        try {

            double val = Double.parseDouble(number);
            if (!Double.isFinite(val)) {

                throw new NumberFormatException();

            }

            validateMinMax(val, Double.MIN_VALUE, Double.MAX_VALUE);
            return val;

        } catch (NumberFormatException e) {

            throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);

        }

    }

    @Override
    public Class<Double> getType() {

        return double.class;

    }

}
