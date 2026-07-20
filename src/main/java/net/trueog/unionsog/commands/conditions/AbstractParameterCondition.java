package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.CommandConditions;
import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractParameterCondition<T> extends AbstractCondition
        implements CommandConditions.ParameterCondition<T, BukkitCommandExecutionContext, BukkitCommandIssuer>
{

    public AbstractParameterCondition(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    public abstract Class<T> getType();

}
