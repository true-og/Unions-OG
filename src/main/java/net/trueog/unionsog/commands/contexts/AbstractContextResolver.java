package net.trueog.unionsog.commands.contexts;

import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.managers.UnionManager;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractContextResolver<T> {

    protected final @NotNull UnionsOG plugin;
    protected final @NotNull UnionManager unionManager;

    public AbstractContextResolver(@NotNull UnionsOG plugin) {

        this.plugin = plugin;
        unionManager = plugin.getUnionManager();

    }

    public abstract Class<T> getType();

}
