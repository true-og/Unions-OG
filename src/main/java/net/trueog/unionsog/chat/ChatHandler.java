package net.trueog.unionsog.chat;

import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.managers.ChatManager;
import net.trueog.unionsog.managers.PermissionsManager;
import net.trueog.unionsog.managers.SettingsManager;

public interface ChatHandler {

    UnionsOG plugin = UnionsOG.getInstance();
    SettingsManager settingsManager = plugin.getSettingsManager();
    ChatManager chatManager = plugin.getChatManager();
    PermissionsManager permissionsManager = plugin.getPermissionsManager();

    void sendMessage(SCMessage message);

    boolean canHandle(SCMessage.Source source);

}
