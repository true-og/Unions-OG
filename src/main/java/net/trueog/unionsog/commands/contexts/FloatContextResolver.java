package net.trueog.unionsog.commands.contexts;

import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class FloatContextResolver extends FloatPrimitiveContextResolver {

    public FloatContextResolver(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public Class<Float> getType() {

        return Float.class;

    }

}
