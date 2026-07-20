package net.trueog.unionsog.hooks.papi.resolvers;

import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.hooks.papi.PlaceholderResolver;
import net.trueog.unionsog.utils.KDRFormat;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Map;

@SuppressWarnings("unused")
public class KdrResolver extends PlaceholderResolver {

    public KdrResolver(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public @NotNull String getId() {

        return "kdr";

    }

    @Override
    public @NotNull String resolve(@Nullable OfflinePlayer player, @NotNull Object object, @NotNull Method method,
            @NotNull String placeholder, @NotNull Map<String, String> config)
    {

        Object result = invoke(object, method, placeholder);
        if (result instanceof Number) {

            return KDRFormat.format(((Number) result).floatValue());

        }

        return "";

    }

}
