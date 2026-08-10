package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.*;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.commands.UnionInput;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;

@SuppressWarnings("unused")
public class DifferentUnionCondition extends AbstractParameterCondition<UnionInput> {

    public DifferentUnionCondition(@NotNull UnionsOG plugin) {

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

        if (execContext.getIssuer().isPlayer()) {

            Player player = execContext.getPlayer();
            UnionPlayer cp = unionManager.getAnyUnionPlayer(player.getUniqueId());
            if (cp != null && cp.getUnion() != null) {

                if (value.getUnion().equals(cp.getUnion())) {

                    throw new ConditionFailedException(lang("cannot.be.same.union", player));

                }

            }

        }

    }

    @Override
    public @NotNull String getId() {

        return "different";

    }

}
