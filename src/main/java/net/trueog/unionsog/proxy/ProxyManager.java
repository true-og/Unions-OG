package net.trueog.unionsog.proxy;

import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.chat.SCMessage;

public interface ProxyManager {

    String getServerName();

    boolean isOnline(String playerName);

    void sendMessage(SCMessage message);

    void sendMessage(String target, String message);

    void sendUpdate(Union union);

    void sendUpdate(UnionPlayer cp);

    void sendDelete(Union union);

    void sendDelete(UnionPlayer cp);

}
