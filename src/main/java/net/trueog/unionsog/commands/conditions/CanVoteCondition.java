package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.UnionsOG;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.RED;

@SuppressWarnings("unused")
public class CanVoteCondition extends AbstractCommandCondition {

    public CanVoteCondition(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context) throws InvalidCommandArgument {

        Player player = Conditions.assertPlayer(context.getIssuer());
        UnionPlayer cp = unionManager.getCreateUnionPlayer(player.getUniqueId());
        Union union = cp.getUnion();
        if (union != null) {

            if (!requestManager.hasRequest(union.getTag())) {

                throw new ConditionFailedException(lang("nothing.to.vote", player));

            }

            if (cp.getVote() != null) {

                throw new ConditionFailedException(RED + lang("you.have.already.voted", player));

            }

        } else {

            if (!requestManager.hasRequest(player.getName().toLowerCase())) {

                throw new ConditionFailedException(lang("nothing.to.vote", player));

            }

        }

    }

    @Override
    public @NotNull String getId() {

        return "can_vote";

    }

}
