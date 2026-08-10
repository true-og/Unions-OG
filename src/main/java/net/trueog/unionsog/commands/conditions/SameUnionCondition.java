package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.*;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.commands.UnionPlayerInput;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;

@SuppressWarnings("unused")
public class SameUnionCondition extends AbstractParameterCondition<UnionPlayerInput> {

    public SameUnionCondition(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public Class<UnionPlayerInput> getType() {

        return UnionPlayerInput.class;

    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context,
            BukkitCommandExecutionContext execContext, UnionPlayerInput value) throws InvalidCommandArgument
    {

        BukkitCommandIssuer issuer = context.getIssuer();
        Union union = Conditions.assertUnionMember(unionManager, issuer);
        if (value == null || value.getUnionPlayer().getUnion() == null
                || !value.getUnionPlayer().getUnion().equals(union))
        {

            throw new ConditionFailedException(lang("the.player.is.not.a.member.of.your.union", issuer));

        }

    }

    @Override
    public @NotNull String getId() {

        return "same_union";

    }

}
