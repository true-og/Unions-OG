package net.trueog.unionsog.commands.completions;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class AllMembersCompletion extends AbstractSyncCompletion {

    public AllMembersCompletion(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public Collection<String> getCompletions(BukkitCommandCompletionContext context) throws InvalidCommandArgument {

        return unionManager.getAllUnionPlayers().stream().filter(cp -> cp.getUnion() != null).map(UnionPlayer::getName)
                .collect(Collectors.toList());

    }

    @Override
    public @NotNull String getId() {

        return "all_members";

    }

}
