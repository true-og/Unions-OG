package net.trueog.unionsog.hooks.papi.resolvers;

import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.hooks.papi.PlaceholderBooleanFormatter;
import net.trueog.unionsog.hooks.papi.PlaceholderResolver;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Map;

@SuppressWarnings("unused")
public class MethodReturnResolver extends PlaceholderResolver {

    public MethodReturnResolver(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public @NotNull String getId() {

        return "method_return";

    }

    @Override
    public @NotNull String resolve(@Nullable OfflinePlayer player, @NotNull Object object, @NotNull Method method,
            @NotNull String placeholder, @NotNull Map<String, String> config)
    {

        Object result = invoke(object, method, placeholder);
        if (result == null) {

            return "";

        }

        if (result instanceof Boolean) {

            return ((Boolean) result) ? PlaceholderBooleanFormatter.trueValue()
                    : PlaceholderBooleanFormatter.falseValue();

        }

        return String.valueOf(result);

    }

}
