package net.trueog.unionsog.chat.handlers;

import net.trueog.unionsog.chat.ChatHandler;
import net.trueog.unionsog.chat.SCMessage;
import net.trueog.unionsog.chat.SCMessage.Source;

import static net.trueog.unionsog.managers.SettingsManager.ConfigField.PERFORMANCE_USE_BUNGEECORD;

public class ProxyChatHandler implements ChatHandler {

    @Override
    public void sendMessage(SCMessage message) {

        plugin.getProxyManager().sendMessage(message);

    }

    @Override
    public boolean canHandle(Source source) {

        return source == Source.SPIGOT && settingsManager.is(PERFORMANCE_USE_BUNGEECORD);

    }

}
