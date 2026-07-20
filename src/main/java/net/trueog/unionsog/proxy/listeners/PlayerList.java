package net.trueog.unionsog.proxy.listeners;

import com.google.common.io.ByteArrayDataInput;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.proxy.BungeeManager;

import java.util.Arrays;
import java.util.List;

public class PlayerList extends MessageListener {

    public PlayerList(BungeeManager bungee) {

        super(bungee);

    }

    @Override
    public void accept(ByteArrayDataInput data) {

        data.readUTF(); // target
        List<String> players = Arrays.asList(data.readUTF().split(", "));
        bungee.setOnlinePlayers(players);
        UnionsOG.debug("Updated player list");

    }

    @Override
    public boolean isBungeeSubchannel() {

        return true;

    }

}
