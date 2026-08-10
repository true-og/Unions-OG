package net.trueog.unionsog.proxy.listeners;

import com.google.common.io.ByteArrayDataInput;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.proxy.BungeeManager;

import static net.trueog.unionsog.UnionsOG.debug;

public class DeleteUnion extends MessageListener {

    public DeleteUnion(BungeeManager bungee) {

        super(bungee);

    }

    @Override
    public void accept(ByteArrayDataInput data) {

        String tag = data.readUTF();
        getUnionManager().removeUnion(tag);
        for (UnionPlayer cp : getUnionManager().getAllUnionPlayers()) {

            if (tag.equals(cp.getTag())) {

                cp.setUnion(null);
                cp.setJoinDate(0);

            }

        }

        debug(String.format("Deleted clan %s", tag));

    }

    @Override
    public boolean isBungeeSubchannel() {

        return false;

    }

}
