package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.*;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.commands.UnionPlayerInput;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.RED;

@SuppressWarnings("unused")
public class NotBannedInputCondition extends AbstractParameterCondition<UnionPlayerInput> {

    public NotBannedInputCondition(@NotNull UnionsOG plugin) {

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

        UUID uniqueId = value.getUnionPlayer().getUniqueId();
        if (settingsManager.isBanned(uniqueId)) {

            throw new ConditionFailedException(
                    RED + lang("this.player.is.banned.from.using.union.commands", execContext.getSender()));

        }

    }

    @Override
    public @NotNull String getId() {

        return "not_banned";

    }

}
