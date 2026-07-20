package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.*;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.commands.ClanInput;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;

@SuppressWarnings("unused")
public class InputVerifiedCondition extends AbstractParameterCondition<ClanInput> {

    public InputVerifiedCondition(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public Class<ClanInput> getType() {

        return ClanInput.class;

    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context,
            BukkitCommandExecutionContext execContext, ClanInput value) throws InvalidCommandArgument
    {

        if (!value.getClan().isVerified() && !context.getIssuer().hasPermission("unionsog.mod.bypass")) {

            throw new ConditionFailedException(lang("other.clan.not.verified", execContext.getSender()));

        }

    }

    @Override
    public @NotNull String getId() {

        return "verified";

    }

}
