package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.Clan;
import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.RED;

@SuppressWarnings("unused")
public class RivableCondition extends AbstractCommandCondition {

    public RivableCondition(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context) throws InvalidCommandArgument {

        Clan clan = Conditions.assertClanMember(clanManager, context.getIssuer());
        if (clan.isUnrivable()) {

            throw new ConditionFailedException(RED + lang("your.clan.cannot.create.rivals", context.getIssuer()));

        }

    }

    @Override
    public @NotNull String getId() {

        return "rivable";

    }

}
