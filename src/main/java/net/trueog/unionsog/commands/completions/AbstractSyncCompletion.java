package net.trueog.unionsog.commands.completions;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions.CommandCompletionHandler;
import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractSyncCompletion extends AbstractCompletion
        implements CommandCompletionHandler<BukkitCommandCompletionContext>
{

    public AbstractSyncCompletion(@NotNull UnionsOG plugin) {

        super(plugin);

    }

}
