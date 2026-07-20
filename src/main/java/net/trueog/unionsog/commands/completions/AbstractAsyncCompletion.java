package net.trueog.unionsog.commands.completions;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions.AsyncCommandCompletionHandler;
import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractAsyncCompletion extends AbstractCompletion
        implements AsyncCommandCompletionHandler<BukkitCommandCompletionContext>
{

    public AbstractAsyncCompletion(@NotNull UnionsOG plugin) {

        super(plugin);

    }

}
