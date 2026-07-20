package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField;

public class CanChatCondition extends AbstractCommandCondition {

    public CanChatCondition(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context) throws InvalidCommandArgument {

        String type = context.getConfigValue("type", (String) null).toUpperCase();
        ConfigField chatEnabled = ConfigField.valueOf(type + "CHAT_ENABLE");
        if (!settingsManager.is(chatEnabled)) {

            throw new ConditionFailedException(lang(type.toLowerCase() + ".chat.disabled", context.getIssuer()));

        }

    }

    @Override
    public @NotNull String getId() {

        return "can_chat";

    }

}
