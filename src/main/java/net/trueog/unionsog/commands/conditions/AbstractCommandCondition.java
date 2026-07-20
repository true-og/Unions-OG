package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.CommandConditions;
import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractCommandCondition extends AbstractCondition
        implements CommandConditions.Condition<BukkitCommandIssuer>
{

    public AbstractCommandCondition(@NotNull UnionsOG plugin) {

        super(plugin);

    }

}
