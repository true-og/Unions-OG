package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.*;
import net.trueog.unionsog.Clan;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.commands.ClanPlayerInput;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;

@SuppressWarnings("unused")
public class SameClanCondition extends AbstractParameterCondition<ClanPlayerInput> {

    public SameClanCondition(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public Class<ClanPlayerInput> getType() {

        return ClanPlayerInput.class;

    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context,
            BukkitCommandExecutionContext execContext, ClanPlayerInput value) throws InvalidCommandArgument
    {

        BukkitCommandIssuer issuer = context.getIssuer();
        Clan clan = Conditions.assertClanMember(clanManager, issuer);
        if (value == null || value.getClanPlayer().getClan() == null || !value.getClanPlayer().getClan().equals(clan)) {

            throw new ConditionFailedException(lang("the.player.is.not.a.member.of.your.clan", issuer));

        }

    }

    @Override
    public @NotNull String getId() {

        return "same_clan";

    }

}
