package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.*;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.commands.UnionPlayerInput;
import net.trueog.unionsog.utils.VanishUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.debug;
import static net.trueog.unionsog.UnionsOG.lang;

@SuppressWarnings("unused")
public class OnlineCondition extends AbstractParameterCondition<UnionPlayerInput> {

    public OnlineCondition(@NotNull UnionsOG plugin) {

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
        debug(String.format("OnlineCondition -> %s %s", unionPlayer.getName(), unionPlayer.getUniqueId()));
        Player player = unionPlayer.toPlayer();

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
