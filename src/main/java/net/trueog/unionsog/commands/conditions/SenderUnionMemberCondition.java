package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class SenderUnionMemberCondition extends AbstractCommandCondition {

    public SenderUnionMemberCondition(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context) throws InvalidCommandArgument {

        Conditions.assertUnionMember(unionManager, context.getIssuer());

    }

    @Override
    public @NotNull String getId() {

        return "union_member";

    }

}
