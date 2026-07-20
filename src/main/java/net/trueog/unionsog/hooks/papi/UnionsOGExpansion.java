package net.trueog.unionsog.hooks.papi;

import me.clip.placeholderapi.expansion.Configurable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import net.trueog.unionsog.UnionsOG;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnionsOGExpansion extends PlaceholderExpansion implements Relational, Configurable {

    private final UnionsOG plugin;
    private final String identifier;
    private final UnionsOGPlaceholderEngine placeholders;

    public UnionsOGExpansion(UnionsOG plugin) {

        this(plugin, "simpleclans");

    }

    public UnionsOGExpansion(UnionsOG plugin, @NotNull String identifier) {

        this.plugin = plugin;
        this.identifier = identifier;
        placeholders = new UnionsOGPlaceholderEngine(plugin);

    }

    @Override
    public @NotNull String getName() {

        return plugin.getName();

    }

    @Override
    public @NotNull String getIdentifier() {

        return identifier;

    }

    @Override
    public @NotNull String getAuthor() {

        return plugin.getDescription().getAuthors().toString();

    }

    @Override
    public @NotNull String getVersion() {

        return plugin.getDescription().getVersion();

    }

    @Override
    public boolean persist() {

        return true;

    }

    @Override
    public @NotNull List<String> getPlaceholders() {

        return placeholders.getLegacyPlaceholders(identifier);

    }

    @Override
    public boolean canRegister() {

        return true;

    }

    @Override
    public Map<String, Object> getDefaults() {

        Map<String, Object> defaults = new HashMap<>();

        defaults.put("color.rival", "&c");
        defaults.put("color.ally", "&b");
        defaults.put("color.same_clan", "&a");

        return defaults;

    }

    @Override
    @Nullable
    public String onPlaceholderRequest(Player player1, Player player2, String params) {

        return placeholders.resolveRelational(player1, player2, params, getString("color.same_clan", null),
                getString("color.rival", null), getString("color.ally", null));

    }

    @Override
    public String onRequest(@Nullable OfflinePlayer player, @NotNull String params) {

        return placeholders.resolve(player, params);

    }

}
