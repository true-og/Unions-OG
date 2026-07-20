package net.trueog.unionsog.proxy.listeners;

import net.trueog.unionsog.Clan;
import net.trueog.unionsog.proxy.BungeeManager;
import org.jetbrains.annotations.Nullable;

public class UpdateClan extends Update<Clan> {

    public UpdateClan(BungeeManager bungee) {

        super(bungee);

    }

    @Override
    public boolean isBungeeSubchannel() {

        return false;

    }

    @Override
    protected Class<Clan> getType() {

        return Clan.class;

    }

    @Override
    protected @Nullable Clan getCurrent(Clan clan) {

        return getClanManager().getClan(clan.getTag());

    }

    @Override
    protected void insert(Clan clan) {

        getClanManager().importClan(clan);

    }

}
