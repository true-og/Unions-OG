package net.trueog.unionsog.events;

import net.trueog.unionsog.Request;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author NeT32
 */
public class RequestFinishedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Request RequestProcess;

    public RequestFinishedEvent(Request RequestProcess) {

        this.RequestProcess = RequestProcess;

    }

    public Request getRequest() {

        return this.RequestProcess;

    }

    @Override
    public HandlerList getHandlers() {

        return handlers;

    }

    public static HandlerList getHandlerList() {

        return handlers;

    }

}
