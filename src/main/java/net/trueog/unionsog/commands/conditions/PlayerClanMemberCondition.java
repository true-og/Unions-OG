package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.UnionsOG;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.debug;

@SuppressWarnings("unused")
public class PlayerClanMemberCondition extends AbstractParameterCondition<Player> {

    public PlayerClanMemberCondition(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public Class<Player> getType() {

        return Player.class;

    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context,
            BukkitCommandExecutionContext execContext, Player value) throws InvalidCommandArgument
    {

        debug(String.format("PlayerClanMemberCondition -> %s %s", value.getName(), value.getUniqueId()));
        Conditions.assertClanMember(clanManager, context.getIssuer());

    }

    @Override
    public @NotNull String getId() {

        return "clan_member";

    }

}
