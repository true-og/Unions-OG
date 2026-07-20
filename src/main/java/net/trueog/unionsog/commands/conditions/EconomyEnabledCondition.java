package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;

@SuppressWarnings("unused")
public class EconomyEnabledCondition extends AbstractCommandCondition {

    public EconomyEnabledCondition(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context) throws InvalidCommandArgument {

        if (!permissionsManager.hasEconomy()) {

            throw new ConditionFailedException(lang("economy.disabled", context.getIssuer()));

        }

    }

    @Override
    public @NotNull String getId() {

        return "economy";

    }

}
