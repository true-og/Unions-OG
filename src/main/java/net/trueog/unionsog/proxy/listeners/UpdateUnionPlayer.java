package net.trueog.unionsog.proxy.listeners;

import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.proxy.BungeeManager;
import org.jetbrains.annotations.Nullable;

public class UpdateUnionPlayer extends Update<UnionPlayer> {

    public UpdateUnionPlayer(BungeeManager bungee) {

        super(bungee);

    }

    @Override
    public boolean isBungeeSubchannel() {

        return false;

    }

    @Override
    protected Class<UnionPlayer> getType() {

        return UnionPlayer.class;

    }

    @Override
    protected @Nullable UnionPlayer getCurrent(UnionPlayer unionPlayer) {

        return getUnionManager().getAnyUnionPlayer(unionPlayer.getUniqueId());

    }

    @Override
    protected void insert(UnionPlayer unionPlayer) {

        getUnionManager().importUnionPlayer(unionPlayer);

    }

}
