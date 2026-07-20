package net.trueog.unionsog.hooks.papi.resolvers;

import net.trueog.unionsog.Clan;
import net.trueog.unionsog.ClanPlayer;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.hooks.papi.PlaceholderResolver;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class RankingPositionResolver extends PlaceholderResolver {

    public RankingPositionResolver(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public @NotNull String getId() {

        return "ranking_position";

    }

    @Override
    public @NotNull String resolve(@Nullable OfflinePlayer player, @NotNull Object object, @NotNull Method method,
            @NotNull String placeholder, @NotNull Map<String, String> config)
    {

        if (object instanceof Clan) {

            List<Clan> clans = plugin.getClanManager().getClans();
            plugin.getClanManager().sortClansByKDR(clans);

            return String.valueOf(clans.indexOf(object) + 1);

        }

        if (object instanceof ClanPlayer) {

            List<ClanPlayer> clanPlayers = plugin.getClanManager().getAllClanPlayers();
            plugin.getClanManager().sortClanPlayersByKDR(clanPlayers);

            return String.valueOf(clanPlayers.indexOf(object) + 1);

        }

        return "";

    }

}
