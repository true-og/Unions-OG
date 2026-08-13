package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.managers.SettingsManager;
import net.trueog.unionsog.managers.SettingsManager.ConfigField;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.RED;

/**
 * Fails when the config switch a command depends on is turned off.
 * <p>
 * Declaring the switch instead of checking it in the command body keeps the
 * refusal in one place and lets the help leave out what the server has turned
 * off. Takes the {@link ConfigField} to read as {@code field} and, optionally,
 * the message key to refuse with as {@code message}.
 * </p>
 */
@SuppressWarnings("unused")
public class SettingEnabledCondition extends AbstractCommandCondition {

    /** The condition id, as written in {@code @Conditions}. */
    public static final String ID = "setting";
    /** The condition argument naming the {@link ConfigField} to read. */
    public static final String FIELD = "field";

    private static final String MESSAGE = "message";
    private static final String DEFAULT_MESSAGE = "disabled.command";

    public SettingEnabledCondition(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context) throws InvalidCommandArgument {

        if (isEnabled(settingsManager, context.getConfigValue(FIELD, ""))) {

            return;

        }

        throw new ConditionFailedException(
                RED + lang(context.getConfigValue(MESSAGE, DEFAULT_MESSAGE), context.getIssuer()));

    }

    /**
     * Reads the switch a condition names, so that the help can ask the same
     * question without running the command.
     *
     * @param settings the settings to read
     * @param field    the {@link ConfigField} name the condition carries
     * @return whether the switch is on
     */
    public static boolean isEnabled(@NotNull SettingsManager settings, @NotNull String field) {

        try {

            return settings.is(ConfigField.valueOf(field));

        } catch (IllegalArgumentException | ClassCastException ex) {

            // A misspelt field must not quietly hide a working command.
            return true;

        }

    }

    @Override
    public @NotNull String getId() {

        return ID;

    }

}
