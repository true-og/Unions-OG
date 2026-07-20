package net.trueog.unionsog.proxy.listeners;

import com.google.common.io.ByteArrayDataInput;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.proxy.BungeeManager;

public class GetServer extends MessageListener {

    public GetServer(BungeeManager bungee) {

        super(bungee);

    }

    @Override
    public void accept(ByteArrayDataInput data) {

        String name = data.readUTF();
        bungee.setServerName(name);
        UnionsOG.debug(String.format("Server name: %s", name));

    }

    @Override
    public boolean isBungeeSubchannel() {

        return true;

    }

}
