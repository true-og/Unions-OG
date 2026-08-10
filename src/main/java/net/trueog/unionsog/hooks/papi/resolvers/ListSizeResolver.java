package net.trueog.unionsog.hooks.papi.resolvers;

import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.hooks.papi.PlaceholderResolver;
import net.trueog.unionsog.utils.VanishUtils;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class ListSizeResolver extends PlaceholderResolver {

    public ListSizeResolver(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public @NotNull String getId() {

        return "list_size";

    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull String resolve(@Nullable OfflinePlayer player, @NotNull Object object, @NotNull Method method,
            @NotNull String placeholder, @NotNull Map<String, String> config)
    {

        Object result = invoke(object, method, placeholder);
        String size = "";
        if (result instanceof List) {

            size = String.valueOf(((List<?>) result).size());
            if (config.containsKey("filter_vanished")) {

                size = String.valueOf(VanishUtils
                        .getNonVanished(player != null ? player.getPlayer() : null, (List<UnionPlayer>) result).size());

            }

        }

        return size;

    }

}
