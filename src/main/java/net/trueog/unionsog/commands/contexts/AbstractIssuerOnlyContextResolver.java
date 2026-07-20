package net.trueog.unionsog.commands.contexts;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.contexts.IssuerOnlyContextResolver;
import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractIssuerOnlyContextResolver<T> extends AbstractContextResolver<T>
        implements IssuerOnlyContextResolver<T, BukkitCommandExecutionContext>
{

    public AbstractIssuerOnlyContextResolver(@NotNull UnionsOG plugin) {

        super(plugin);

    }

}
