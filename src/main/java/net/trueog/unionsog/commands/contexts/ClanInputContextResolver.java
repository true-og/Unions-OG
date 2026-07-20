package net.trueog.unionsog.commands.contexts;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.Clan;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.commands.ClanInput;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.RED;

@SuppressWarnings("unused")
public class ClanInputContextResolver extends AbstractInputOnlyContextResolver<ClanInput> {

    public ClanInputContextResolver(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public ClanInput getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {

        String arg = context.popFirstArg();
        Clan clan = clanManager.getClan(arg);
        if (clan == null) {

            throw new InvalidCommandArgument(RED + lang("the.clan.does.not.exist", context.getSender()), false);

        }

        return new ClanInput(clan);

    }

    @Override
    public Class<ClanInput> getType() {

        return ClanInput.class;

    }

}
