package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.UNION_MIN_SIZE_TO_SET_RIVAL;
import static org.bukkit.ChatColor.RED;

@SuppressWarnings("unused")
public class MinimumToRivalCondition extends AbstractCommandCondition {

    public MinimumToRivalCondition(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context) throws InvalidCommandArgument {

        BukkitCommandIssuer issuer = context.getIssuer();
        Union union = Conditions.assertUnionMember(unionManager, issuer);
        if (union.getSize() < settingsManager.getInt(UNION_MIN_SIZE_TO_SET_RIVAL)) {

            throw new ConditionFailedException(
                    RED + lang("min.players.rivalries", issuer, settingsManager.getInt(UNION_MIN_SIZE_TO_SET_RIVAL)));

        }

    }

    @Override
    public @NotNull String getId() {

        return "minimum_to_rival";

    }

}
