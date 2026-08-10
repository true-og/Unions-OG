package net.trueog.unionsog.events;

import net.trueog.unionsog.UnionPlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AddKillEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private final UnionPlayer victim;
    private final UnionPlayer attacker;

    public AddKillEvent(@NotNull UnionPlayer attacker, @NotNull UnionPlayer victim) {

        this.attacker = attacker;
        this.victim = victim;

    }

    @Override
    public boolean isCancelled() {

        return cancelled;

    }

    public UnionPlayer getAttacker() {

        return attacker;

    }

    public UnionPlayer getVictim() {

        return victim;

    }

    @Override
    public void setCancelled(boolean value) {

        cancelled = value;

    }

    @Override
    public @NotNull HandlerList getHandlers() {

        return handlers;

    }

    public static HandlerList getHandlerList() {

        return handlers;

    }

}
