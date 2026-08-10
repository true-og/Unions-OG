package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.commands.UnionInput;
import org.jetbrains.annotations.NotNull;

public class TeleportInputCondition extends AbstractParameterCondition<UnionInput> {

    public TeleportInputCondition(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public Class<UnionInput> getType() {

        return UnionInput.class;

    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context,
            BukkitCommandExecutionContext execContext, UnionInput value) throws InvalidCommandArgument
    {

        new TeleportCondition(plugin).validateCondition(context, execContext, value.getUnion());

    }

    @Override
    public @NotNull String getId() {

        return "can_teleport";

    }

}
