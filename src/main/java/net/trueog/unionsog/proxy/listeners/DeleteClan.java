package net.trueog.unionsog.proxy.listeners;

import com.google.common.io.ByteArrayDataInput;
import net.trueog.unionsog.ClanPlayer;
import net.trueog.unionsog.proxy.BungeeManager;

import static net.trueog.unionsog.UnionsOG.debug;

public class DeleteClan extends MessageListener {

    public DeleteClan(BungeeManager bungee) {

        super(bungee);

    }

    @Override
    public void accept(ByteArrayDataInput data) {

        String tag = data.readUTF();
        getClanManager().removeClan(tag);
        for (ClanPlayer cp : getClanManager().getAllClanPlayers()) {

            if (tag.equals(cp.getTag())) {

                cp.setClan(null);
                cp.setJoinDate(0);
                cp.setRank(null);
                cp.setLeader(false);

            }

        }

        debug(String.format("Deleted clan %s", tag));

    }

    @Override
    public boolean isBungeeSubchannel() {

        return false;

    }

}
