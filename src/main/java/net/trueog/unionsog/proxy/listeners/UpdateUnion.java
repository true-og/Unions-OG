package net.trueog.unionsog.proxy.listeners;

import net.trueog.unionsog.Union;
import net.trueog.unionsog.proxy.BungeeManager;
import org.jetbrains.annotations.Nullable;

public class UpdateUnion extends Update<Union> {

    public UpdateUnion(BungeeManager bungee) {

        super(bungee);

    }

    @Override
    public boolean isBungeeSubchannel() {

        return false;

    }

    @Override
    protected Class<Union> getType() {

        return Union.class;

    }

    @Override
    protected @Nullable Union getCurrent(Union union) {

        return getUnionManager().getUnion(union.getTag());

    }

    @Override
    protected void insert(Union union) {

        getUnionManager().importUnion(union);

    }

}
