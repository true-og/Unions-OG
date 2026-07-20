package net.trueog.unionsog.commands.contexts;

import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class DoubleContextResolver extends DoublePrimitiveContextResolver {

    public DoubleContextResolver(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public Class<Double> getType() {

        return Double.class;

    }

}
