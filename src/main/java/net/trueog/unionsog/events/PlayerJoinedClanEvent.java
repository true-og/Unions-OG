package net.trueog.unionsog.events;

import net.trueog.unionsog.Clan;
import net.trueog.unionsog.ClanPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author NeT32
 */
public class PlayerJoinedClanEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Clan clan;
    private final ClanPlayer target;

    public PlayerJoinedClanEvent(Clan clan, ClanPlayer target) {

        this.clan = clan;
        this.target = target;

    }

    public Clan getClan() {

        return this.clan;

    }

    public ClanPlayer getClanPlayer() {

        return this.target;

    }

    @Override
    public HandlerList getHandlers() {

        return handlers;

    }

    public static HandlerList getHandlerList() {

        return handlers;

    }

}
