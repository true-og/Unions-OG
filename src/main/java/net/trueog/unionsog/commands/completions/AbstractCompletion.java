package net.trueog.unionsog.commands.completions;

import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.managers.UnionManager;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractCompletion implements IdentifiableCompletion {

    protected final UnionsOG plugin;
    protected final UnionManager unionManager;

    public AbstractCompletion(@NotNull UnionsOG plugin) {

        this.plugin = plugin;
        this.unionManager = plugin.getUnionManager();

    }

}
