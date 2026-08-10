package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.*;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.commands.UnionInput;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.RED;

@SuppressWarnings("unused")
public class AlliedUnionCondition extends AbstractParameterCondition<UnionInput> {

    public AlliedUnionCondition(@NotNull UnionsOG plugin) {

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

        BukkitCommandIssuer issuer = context.getIssuer();
        Union union = Conditions.assertUnionMember(unionManager, issuer);
        if (!union.isAlly(value.getUnion().getTag())) {

            throw new ConditionFailedException(RED + lang("your.unions.are.not.allies", issuer));

        }

    }

    @Override
    public @NotNull String getId() {

        return "allied_union";

    }

}
