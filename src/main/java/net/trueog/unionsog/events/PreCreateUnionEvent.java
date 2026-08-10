package net.trueog.unionsog.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PreCreateUnionEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean cancelled;
    private final String tag;
    private final String name;

    public PreCreateUnionEvent(@NotNull Player who, @NotNull String tag, @NotNull String name) {

        super(who);
        this.tag = tag;
        this.name = name;

    }

    public static HandlerList getHandlerList() {

        return HANDLER_LIST;

    }

    @Override
    public @NotNull HandlerList getHandlers() {

        return HANDLER_LIST;

    }

    public String getTag() {

        return tag;

    }

    public String getName() {

        return name;

    }

    @Override
    public boolean isCancelled() {

        return cancelled;

    }

    @Override
    public void setCancelled(boolean cancelled) {

        this.cancelled = cancelled;

    }

}
