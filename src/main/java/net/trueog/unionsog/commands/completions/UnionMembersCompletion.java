package net.trueog.unionsog.commands.completions;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.UnionsOG;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class UnionMembersCompletion extends AbstractSyncCompletion {

    public UnionMembersCompletion(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public Collection<String> getCompletions(BukkitCommandCompletionContext c) throws InvalidCommandArgument {

        Player player = c.getPlayer();
        if (player != null) {

            Union union = unionManager.getUnionByPlayerUniqueId(player.getUniqueId());
            if (union != null) {

                List<String> list = union.getMembers().stream().map(UnionPlayer::getName).collect(Collectors.toList());
                if (c.hasConfig("hide_own")) {

                    list.remove(player.getName());

                }

                return list;

            }

        }

        return Collections.emptyList();

    }

    @Override
    public @NotNull String getId() {

        return "union_members";

    }

}
