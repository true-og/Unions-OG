package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.*;
import net.trueog.unionsog.ClanPlayer;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.commands.ClanPlayerInput;
import net.trueog.unionsog.utils.VanishUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.debug;
import static net.trueog.unionsog.UnionsOG.lang;

@SuppressWarnings("unused")
public class OnlineCondition extends AbstractParameterCondition<ClanPlayerInput> {

    public OnlineCondition(@NotNull UnionsOG plugin) {

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
        debug(String.format("OnlineCondition -> %s %s", clanPlayer.getName(), clanPlayer.getUniqueId()));
        Player player = clanPlayer.toPlayer();

        if (player != null) {

            boolean isVanished = VanishUtils.isVanished(execContext.getSender(), player);
            if (!isVanished || !context.hasConfig("ignore_vanished")) {

                return;

            }

        }

        throw new ConditionFailedException(lang("other.player.must.be.online", execContext.getSender()));

    }

    @Override
    @NotNull
    public String getId() {

        return "online";

    }

}
