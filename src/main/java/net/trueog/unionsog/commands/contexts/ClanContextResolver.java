package net.trueog.unionsog.commands.contexts;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.Clan;
import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class ClanContextResolver extends AbstractIssuerOnlyContextResolver<Clan> {

    public ClanContextResolver(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public Clan getContext(BukkitCommandExecutionContext c) throws InvalidCommandArgument {

        return Contexts.assertClanMember(clanManager, c.getIssuer());

    }

    @Override
    public Class<Clan> getType() {

        return Clan.class;

    }

}
