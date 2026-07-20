package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.*;
import net.trueog.unionsog.ClanPlayer;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.commands.ClanPlayerInput;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.debug;
import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.RED;

@SuppressWarnings("unused")
public class NotInClanCondition extends AbstractParameterCondition<ClanPlayerInput> {

    public NotInClanCondition(@NotNull UnionsOG plugin) {

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

        ClanPlayer clanPlayer = value.getClanPlayer();
        debug(String.format("NotInClanCondition -> %s %s", clanPlayer.getName(), clanPlayer.getUniqueId()));
        if (clanPlayer.getClan() != null) {

            throw new ConditionFailedException(
                    RED + lang("the.player.is.already.member.of.another.clan", execContext.getSender()));

        }

    }

    @Override
    public @NotNull String getId() {

        return "not_in_clan";

    }

}
