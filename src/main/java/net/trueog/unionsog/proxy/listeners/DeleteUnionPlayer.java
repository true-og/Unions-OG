package net.trueog.unionsog.proxy.listeners;

import com.google.common.io.ByteArrayDataInput;
import net.trueog.unionsog.proxy.BungeeManager;

import java.util.UUID;

import static net.trueog.unionsog.UnionsOG.debug;

public class DeleteUnionPlayer extends MessageListener {

    public DeleteUnionPlayer(BungeeManager bungee) {

        super(bungee);

    }

    @Override
    public void accept(ByteArrayDataInput data) {

        UUID uuid = UUID.fromString(data.readUTF());
        getUnionManager().deleteUnionPlayerFromMemory(uuid);
        debug(String.format("Deleted cp %s", uuid));

    }

    @Override
    public boolean isBungeeSubchannel() {

        return false;

    }

}
