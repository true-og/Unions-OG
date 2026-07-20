package net.trueog.unionsog.commands.completions;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.Clan;
import net.trueog.unionsog.UnionsOG;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class WarringClansCompletion extends AbstractSyncCompletion {

    public WarringClansCompletion(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public Collection<String> getCompletions(BukkitCommandCompletionContext c) throws InvalidCommandArgument {

        if (c.getIssuer().isPlayer()) {

            Player player = c.getPlayer();
            Clan clan = clanManager.getClanByPlayerUniqueId(player.getUniqueId());
            if (clan != null) {

                return clan.getWarringClans().stream().map(Clan::getTag).collect(Collectors.toList());

            }

        }

        return Collections.emptyList();

    }

    @Override
    public @NotNull String getId() {

        return "warring_clans";

    }

}
