package net.trueog.unionsog.events;

import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionPlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerHomeClearEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Union union;
    private final UnionPlayer cp;
    private boolean cancelled;

    public PlayerHomeClearEvent(Union union, UnionPlayer cp) {

        this.union = union;
        this.cp = cp;

    }

    public Union getUnion() {

        return union;

    }

    public UnionPlayer getCp() {

        return cp;

    }

    @Override
    public boolean isCancelled() {

        return cancelled;

    }

    @Override
    public void setCancelled(boolean cancel) {

        this.cancelled = cancel;

    }

    @NotNull
    @Override
    public HandlerList getHandlers() {

        return HANDLER_LIST;

    }

    public static HandlerList getHandlerList() {

        return HANDLER_LIST;

    }

}
