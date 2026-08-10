package net.trueog.unionsog.commands.completions;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UnionsCompletion extends AbstractSyncCompletion {

    public UnionsCompletion(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public Collection<String> getCompletions(BukkitCommandCompletionContext c) throws InvalidCommandArgument {

        List<Union> unions = unionManager.getUnions();
        if (c.hasConfig("has_home")) {

            unions.removeIf(union -> union.getHomeLocation() == null);

        }

        if (c.hasConfig("hide_own")) {

            Union union = getUnion(c.getIssuer());
            if (union != null) {

                unions.remove(union);

            }

        }

        return unions.stream().map(Union::getTag).collect(Collectors.toList());

    }

    @Override
    public @NotNull String getId() {

        return "unions";

    }

    private @Nullable Union getUnion(CommandIssuer issuer) {

        return unionManager.getUnionByPlayerUniqueId(issuer.getUniqueId());

    }

}
