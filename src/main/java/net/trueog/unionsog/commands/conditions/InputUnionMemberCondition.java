package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.*;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.commands.UnionPlayerInput;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.RED;

public class InputUnionMemberCondition extends AbstractParameterCondition<UnionPlayerInput> {

    public InputUnionMemberCondition(@NotNull UnionsOG plugin) {

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

        if (value.getUnionPlayer().getUnion() == null) {

            throw new ConditionFailedException(RED + lang("player.not.a.member.of.any.union", execContext.getSender()));

        }

    }

    @Override
    public @NotNull String getId() {

        return "union_member";

    }

}
