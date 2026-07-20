package net.trueog.unionsog.proxy;

import net.trueog.unionsog.Clan;
import net.trueog.unionsog.ClanPlayer;
import net.trueog.unionsog.chat.SCMessage;

public interface ProxyManager {

    String getServerName();

    boolean isOnline(String playerName);

    void sendMessage(SCMessage message);

    void sendMessage(String target, String message);

    void sendUpdate(Clan clan);

    void sendUpdate(ClanPlayer cp);

    void sendDelete(Clan clan);

    void sendDelete(ClanPlayer cp);

}
