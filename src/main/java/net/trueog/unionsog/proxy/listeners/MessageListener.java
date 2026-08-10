package net.trueog.unionsog.proxy.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.gson.Gson;
import net.trueog.unionsog.managers.UnionManager;
import net.trueog.unionsog.proxy.BungeeManager;

public abstract class MessageListener {

    protected final BungeeManager bungee;

    public MessageListener(BungeeManager bungee) {

        this.bungee = bungee;

    }

    public abstract void accept(ByteArrayDataInput data);

    public abstract boolean isBungeeSubchannel();

    protected UnionManager getUnionManager() {

        return bungee.getPlugin().getUnionManager();

    }

    protected Gson getGson() {

        return bungee.getGson();

    }

}
