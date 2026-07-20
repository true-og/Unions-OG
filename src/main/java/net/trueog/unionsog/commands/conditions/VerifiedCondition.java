package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.Clan;
import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;

@SuppressWarnings("unused")
public class VerifiedCondition extends AbstractCommandCondition {

    public VerifiedCondition(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context) throws InvalidCommandArgument {

        Clan clan = Conditions.assertClanMember(clanManager, context.getIssuer());
        if (!clan.isVerified()) {

            throw new ConditionFailedException(lang("clan.is.not.verified", context.getIssuer()));

        }

    }

    @Override
    public @NotNull String getId() {

        return "verified";

    }

}
