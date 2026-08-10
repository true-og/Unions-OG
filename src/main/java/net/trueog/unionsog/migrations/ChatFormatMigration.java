package net.trueog.unionsog.migrations;

import net.trueog.unionsog.managers.SettingsManager;

import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;

/**
 * @author RoinujNosde
 */
public class ChatFormatMigration extends ConfigMigration {

    public ChatFormatMigration(SettingsManager settingsManager) {

        super(settingsManager);

    }

    public void migrateUnionChat() {

        if (config.getString("clanchat.name-color") == null) {

            return;

        }

        StringBuilder sb = new StringBuilder();

        sb.append('&');
        sb.append(settings.getColored(UNIONCHAT_BRACKET_COLOR));
        sb.append(settings.getString(UNIONCHAT_BRACKET_LEFT));
        sb.append("%union%");
        sb.append("&");
        sb.append(settings.getColored(UNIONCHAT_BRACKET_COLOR));
        sb.append(settings.getString(UNIONCHAT_BRACKET_RIGHT));
        sb.append(" ");
        sb.append('&');
        sb.append(settings.getColored(UNIONCHAT_NAME_COLOR));
        sb.append(settings.getString(UNIONCHAT_PLAYER_BRACKET_LEFT));
        sb.append("%nick-color%");
        sb.append("%player%");
        sb.append('&');
        sb.append(settings.getColored(UNIONCHAT_NAME_COLOR));
        sb.append(settings.getString(UNIONCHAT_PLAYER_BRACKET_RIGHT));
        sb.append(": ");
        sb.append('&');
        sb.append(settings.getColored(UNIONCHAT_MESSAGE_COLOR));
        sb.append("%message%");

        config.set("clanchat.format", sb.toString());
        config.set("clanchat.rank", "&f[&f]");
        config.set("clanchat.rank.color", null);
        config.set("clanchat.name-color", null);
        config.set("clanchat.player-bracket", null);
        config.set("clanchat.message-color", null);
        config.set("clanchat.tag-bracket", null);

    }

    public void migrateAllyChat() {

        // Checks if the old format is still in use
        if (config.getString("allychat.tag-color") == null) {

            return;

        }

        StringBuilder sb = new StringBuilder();
        sb.append('&');
        sb.append(settings.getColored(ALLYCHAT_BRACKET_COLOR));
        sb.append(settings.getString(ALLYCHAT_BRACKET_lEFT));
        sb.append('&');
        sb.append(settings.getColored(ALLYCHAT_TAG_COLOR));
        sb.append(settings.getString(COMMANDS_ALLY));
        sb.append('&');
        sb.append(settings.getColored(ALLYCHAT_BRACKET_COLOR));
        sb.append(settings.getString(ALLYCHAT_BRACKET_RIGHT));
        sb.append(" ");
        sb.append("&4<%union%&4> ");
        sb.append('&');
        sb.append(settings.getColored(ALLYCHAT_BRACKET_COLOR));
        sb.append(settings.getString(ALLYCHAT_PLAYER_BRACKET_LEFT));
        sb.append("%nick-color%");
        sb.append("%player%");
        sb.append('&');
        sb.append(settings.getColored(ALLYCHAT_BRACKET_COLOR));
        sb.append(settings.getString(ALLYCHAT_PLAYER_BRACKET_RIGHT));
        sb.append(": ");
        sb.append('&');
        sb.append(settings.getColored(ALLYCHAT_MESSAGE_COLOR));
        sb.append("%message%");

        config.set("allychat.format", sb.toString());
        config.set("allychat.rank", "&f[&f]");
        config.set("allychat.tag-color", null);
        config.set("allychat.name-color", null);
        config.set("allychat.player-bracket", null);
        config.set("allychat.message-color", null);
        config.set("allychat.tag-bracket", null);

    }

    @Override
    public void migrate() {

        migrateUnionChat();
        migrateAllyChat();

    }

}
