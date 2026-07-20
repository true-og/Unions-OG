package net.trueog.unionsog.proxy.listeners;

import net.trueog.unionsog.ClanPlayer;
import net.trueog.unionsog.proxy.BungeeManager;
import org.jetbrains.annotations.Nullable;

public class UpdateClanPlayer extends Update<ClanPlayer> {

    public UpdateClanPlayer(BungeeManager bungee) {

        super(bungee);

    }

    @Override
    public boolean isBungeeSubchannel() {

        return false;

    }

    @Override
    protected Class<ClanPlayer> getType() {

        return ClanPlayer.class;

    }

    @Override
    protected @Nullable ClanPlayer getCurrent(ClanPlayer clanPlayer) {

        return getClanManager().getAnyClanPlayer(clanPlayer.getUniqueId());

    }

    @Override
    protected void insert(ClanPlayer clanPlayer) {

        getClanManager().importClanPlayer(clanPlayer);

    }

}
