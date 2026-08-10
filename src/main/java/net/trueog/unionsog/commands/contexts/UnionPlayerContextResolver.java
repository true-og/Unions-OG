package net.trueog.unionsog.commands.contexts;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.UnionsOG;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class UnionPlayerContextResolver extends AbstractIssuerOnlyContextResolver<UnionPlayer> {

    public UnionPlayerContextResolver(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public UnionPlayer getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {

        Player player = Contexts.assertPlayer(context.getIssuer());
        return unionManager.getCreateUnionPlayer(player.getUniqueId());

    }

    @Override
    public Class<UnionPlayer> getType() {

        return UnionPlayer.class;

    }

}
