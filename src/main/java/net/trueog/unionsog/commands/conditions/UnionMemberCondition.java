package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class UnionMemberCondition extends AbstractParameterCondition<UnionPlayer> {

    public UnionMemberCondition(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public Class<UnionPlayer> getType() {

        return UnionPlayer.class;

    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context,
            BukkitCommandExecutionContext execContext, UnionPlayer value) throws InvalidCommandArgument
    {

        Conditions.assertUnionMember(unionManager, context.getIssuer());

    }

    @Override
    public @NotNull String getId() {

        return "union_member";

    }

}
