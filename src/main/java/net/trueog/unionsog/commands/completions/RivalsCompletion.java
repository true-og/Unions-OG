package net.trueog.unionsog.commands.completions;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.Clan;
import net.trueog.unionsog.UnionsOG;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

@SuppressWarnings("unused")
public class RivalsCompletion extends AbstractSyncCompletion {

    public RivalsCompletion(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public Collection<String> getCompletions(BukkitCommandCompletionContext c) throws InvalidCommandArgument {

        Player player = c.getPlayer();
        if (player != null) {

            Clan clan = clanManager.getClanByPlayerUniqueId(player.getUniqueId());
            if (clan != null) {

                return clan.getRivals();

            }

        }

        return Collections.emptyList();

    }

    @Override
    public @NotNull String getId() {

        return "rivals";

    }

}
