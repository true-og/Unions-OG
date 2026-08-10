package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.*;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.commands.UnionPlayerInput;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.debug;
import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.RED;

@SuppressWarnings("unused")
public class NotInUnionCondition extends AbstractParameterCondition<UnionPlayerInput> {

    public NotInUnionCondition(@NotNull UnionsOG plugin) {

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

        UnionPlayer unionPlayer = value.getUnionPlayer();
        debug(String.format("NotInClanCondition -> %s %s", unionPlayer.getName(), unionPlayer.getUniqueId()));
        if (unionPlayer.getUnion() != null) {

            throw new ConditionFailedException(
                    RED + lang("the.player.is.already.member.of.another.union", execContext.getSender()));

        }

    }

    @Override
    public @NotNull String getId() {

        return "not_in_union";

    }

}
