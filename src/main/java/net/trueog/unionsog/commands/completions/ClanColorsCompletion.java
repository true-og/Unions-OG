package net.trueog.unionsog.commands.completions;

import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

@SuppressWarnings("unused")
public class ClanColorsCompletion extends AbstractStaticCompletion {

    public ClanColorsCompletion(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public @NotNull Collection<String> getCompletions() {

        return Arrays.asList("black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple", "gold", "gray",
                "dark_gray", "blue", "green", "aqua", "red", "light_purple", "yellow", "white");

    }

    @Override
    public @NotNull String getId() {

        return "clan_colors";

    }

}
