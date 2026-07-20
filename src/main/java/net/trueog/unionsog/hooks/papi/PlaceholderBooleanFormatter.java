package net.trueog.unionsog.hooks.papi;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class PlaceholderBooleanFormatter {

    private static final String PLACEHOLDER_API = "PlaceholderAPI";

    private PlaceholderBooleanFormatter() {

    }

    public static @NotNull String trueValue() {

        return getConfiguredValue("boolean.true", "yes");

    }

    public static @NotNull String falseValue() {

        return getConfiguredValue("boolean.false", "no");

    }

    private static @NotNull String getConfiguredValue(@NotNull String path, @NotNull String fallback) {

        Plugin placeholderApi = Bukkit.getPluginManager().getPlugin(PLACEHOLDER_API);
        if (placeholderApi instanceof JavaPlugin) {

            return ((JavaPlugin) placeholderApi).getConfig().getString(path, fallback);

        }

        return fallback;

    }

}
