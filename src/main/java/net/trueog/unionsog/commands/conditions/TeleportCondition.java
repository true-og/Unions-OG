package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.*;
import net.trueog.unionsog.Clan;
import net.trueog.unionsog.Flags;
import net.trueog.unionsog.UnionsOG;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;

public class TeleportCondition extends AbstractParameterCondition<Clan> {

    public TeleportCondition(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public Class<Clan> getType() {

        return Clan.class;

    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context,
            BukkitCommandExecutionContext execContext, Clan value) throws InvalidCommandArgument
    {

        Player player = execContext.getPlayer();
        if (value.getHomeLocation() == null) {

            throw new ConditionFailedException(lang("hombase.not.set", player));

        }

        Flags flags = new Flags(value.getFlags());
        String homeServer = flags.getString("homeServer", "");
        if (!homeServer.isEmpty() && !plugin.getProxyManager().getServerName().equals(homeServer)) {

            throw new ConditionFailedException(lang("home.set.in.different.server"));

        }

    }

    @Override
    public @NotNull String getId() {

        return "can_teleport";

    }

}
