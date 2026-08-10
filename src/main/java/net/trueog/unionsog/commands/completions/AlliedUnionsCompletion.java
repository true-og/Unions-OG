package net.trueog.unionsog.commands.completions;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionsOG;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

@SuppressWarnings("unused")
public class AlliedUnionsCompletion extends AbstractSyncCompletion {

    public AlliedUnionsCompletion(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public Collection<String> getCompletions(BukkitCommandCompletionContext context) throws InvalidCommandArgument {

        Player player = context.getPlayer();
        if (player != null) {

            Union union = unionManager.getUnionByPlayerUniqueId(player.getUniqueId());
            if (union != null) {

                return union.getAllies();

            }

        }

        return Collections.emptyList();

    }

    @Override
    public @NotNull String getId() {

        return "allied_unions";

    }

}
