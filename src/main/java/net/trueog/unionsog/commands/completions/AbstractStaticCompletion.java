package net.trueog.unionsog.commands.completions;

import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public abstract class AbstractStaticCompletion extends AbstractCompletion {

    public AbstractStaticCompletion(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @NotNull
    public abstract Collection<String> getCompletions();

}
