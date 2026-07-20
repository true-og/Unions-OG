package net.trueog.unionsog.commands.contexts;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.contexts.ContextResolver;
import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractInputOnlyContextResolver<T> extends AbstractContextResolver<T>
        implements ContextResolver<T, BukkitCommandExecutionContext>
{

    public AbstractInputOnlyContextResolver(@NotNull UnionsOG plugin) {

        super(plugin);

    }

}
