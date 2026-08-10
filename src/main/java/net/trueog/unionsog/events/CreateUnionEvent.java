package net.trueog.unionsog.events;

import net.trueog.unionsog.Union;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author NeT32
 */
public class CreateUnionEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Union union;

    public CreateUnionEvent(Union union) {

        this.union = union;

    }

    public Union getUnion() {

        return this.union;

    }

    @Override
    public HandlerList getHandlers() {

        return handlers;

    }

    public static HandlerList getHandlerList() {

        return handlers;

    }

}
