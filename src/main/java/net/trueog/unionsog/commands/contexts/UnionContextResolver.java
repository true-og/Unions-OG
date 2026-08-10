package net.trueog.unionsog.commands.contexts;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class UnionContextResolver extends AbstractIssuerOnlyContextResolver<Union> {

    public UnionContextResolver(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public Union getContext(BukkitCommandExecutionContext c) throws InvalidCommandArgument {

        return Contexts.assertUnionMember(unionManager, c.getIssuer());

    }

    @Override
    public Class<Union> getType() {

        return Union.class;

    }

}
