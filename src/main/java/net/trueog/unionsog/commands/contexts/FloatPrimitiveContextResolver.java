package net.trueog.unionsog.commands.contexts;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.MessageKeys;
import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.commands.contexts.Contexts.validateMinMax;

@SuppressWarnings("unused")
public class FloatPrimitiveContextResolver extends AbstractInputOnlyContextResolver<Float> {

    public FloatPrimitiveContextResolver(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public Float getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {

        String number = context.popFirstArg();
        try {

            float val = Float.parseFloat(number);
            if (!Float.isFinite(val)) {

                throw new NumberFormatException();

            }

            validateMinMax(val, Float.MIN_VALUE, Float.MAX_VALUE);
            return val;

        } catch (NumberFormatException e) {

            throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);

        }

    }

    @Override
    public Class<Float> getType() {

        return float.class;

    }

}
