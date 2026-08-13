package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.RED;

/**
 * Fails when the issuer is already in a union, for the commands only a player
 * without one can run. The mirror of {@link SenderUnionMemberCondition}, and
 * what keeps those commands out of a member's help.
 */
@SuppressWarnings("unused")
public class SenderNotUnionMemberCondition extends AbstractCommandCondition {

    public SenderNotUnionMemberCondition(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context) throws InvalidCommandArgument {

        BukkitCommandIssuer issuer = context.getIssuer();
        Conditions.assertPlayer(issuer);

        Union union = unionManager.getUnionByPlayerUniqueId(issuer.getUniqueId());
        if (union != null) {

            throw new ConditionFailedException(RED + lang("you.must.first.resign", issuer, union.getName()));

        }

    }

    @Override
    public @NotNull String getId() {

        return "not_union_member";

    }

}
