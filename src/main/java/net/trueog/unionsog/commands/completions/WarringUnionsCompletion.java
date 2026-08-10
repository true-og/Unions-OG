package net.trueog.unionsog.commands.completions;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionsOG;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class WarringUnionsCompletion extends AbstractSyncCompletion {

    public WarringUnionsCompletion(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public Collection<String> getCompletions(BukkitCommandCompletionContext c) throws InvalidCommandArgument {

        if (c.getIssuer().isPlayer()) {

            Player player = c.getPlayer();
            Union union = unionManager.getUnionByPlayerUniqueId(player.getUniqueId());
            if (union != null) {

                return union.getWarringUnions().stream().map(Union::getTag).collect(Collectors.toList());

            }

        }

        return Collections.emptyList();

    }

    @Override
    public @NotNull String getId() {

        return "warring_unions";

    }

}
