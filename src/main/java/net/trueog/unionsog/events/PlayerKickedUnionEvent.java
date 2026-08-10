package net.trueog.unionsog.events;

import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author NeT32
 */
public class PlayerKickedUnionEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Union union;
    private final UnionPlayer target;

    public PlayerKickedUnionEvent(Union union, UnionPlayer target) {

        this.union = union;
        this.target = target;

    }

    public Union getUnion() {

        return this.union;

    }

    public UnionPlayer getUnionPlayer() {

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
