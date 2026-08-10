package net.trueog.unionsog.events;

import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionPlayer;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author NeT32
 */
public class PlayerHomeSetEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final Union union;
    private final UnionPlayer cp;
    private final Location loc;

    public PlayerHomeSetEvent(Union union, UnionPlayer cp, Location loc) {

        this.union = union;
        this.cp = cp;
        this.loc = loc;

    }

    @Override
    public boolean isCancelled() {

        return cancelled;

    }

    @Override
    public void setCancelled(boolean cancelled) {

        this.cancelled = cancelled;

    }

    public Union getUnion() {

        return this.union;

    }

    public UnionPlayer getUnionPlayer() {

        return this.cp;

    }

    public Location getLocation() {

        return this.loc;

    }

    @Override
    public HandlerList getHandlers() {

        return handlers;

    }

    public static HandlerList getHandlerList() {

        return handlers;

    }

}
