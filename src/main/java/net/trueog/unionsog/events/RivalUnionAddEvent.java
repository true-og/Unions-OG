package net.trueog.unionsog.events;

import net.trueog.unionsog.Union;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author NeT32
 */
public class RivalUnionAddEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Union unionFirst;
    private final Union unionSecond;

    public RivalUnionAddEvent(Union unionFirst, Union unionSecond) {

        this.unionFirst = unionFirst;
        this.unionSecond = unionSecond;

    }

    public Union getUnionFirst() {

        return this.unionFirst;

    }

    public Union getUnionSecond() {

        return this.unionSecond;

    }

    @Override
    public HandlerList getHandlers() {

        return handlers;

    }

    public static HandlerList getHandlerList() {

        return handlers;

    }

}
