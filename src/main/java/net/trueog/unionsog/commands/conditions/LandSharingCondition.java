package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.LAND_SHARING;

@SuppressWarnings("unused")
public class LandSharingCondition extends AbstractCommandCondition {

    public LandSharingCondition(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context) throws InvalidCommandArgument {

        if (!settingsManager.is(LAND_SHARING)) {

            throw new ConditionFailedException(lang("land.sharing.disabled", context.getIssuer()));

        }

    }

    @Override
    public @NotNull String getId() {

        return "land_sharing";

    }

}
