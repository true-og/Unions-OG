package net.trueog.unionsog.commands.contexts;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.Clan;
import net.trueog.unionsog.Rank;
import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.RED;

@SuppressWarnings("unused")
public class RankContextResolver extends AbstractInputOnlyContextResolver<Rank> {

    public RankContextResolver(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public Rank getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {

        Clan clan = Contexts.assertClanMember(clanManager, context.getIssuer());
        String rankName = context.isLastArg() ? context.joinArgs() : context.popFirstArg();
        Rank rank = clan.getRank(rankName);
        if (rank == null) {

            throw new InvalidCommandArgument(RED + lang("rank.0.does.not.exist", context.getIssuer(), rankName), false);

        }

        return rank;

    }

    @Override
    public Class<Rank> getType() {

        return Rank.class;

    }

}
