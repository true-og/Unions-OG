/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.trueog.unionsog.events;

import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionPlayer;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author edson
 */
public class HomeRegroupEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final Union union;
    private final UnionPlayer cp;
    private final List<UnionPlayer> cps;
    private final Location loc;

    public HomeRegroupEvent(Union union, UnionPlayer cp, List<UnionPlayer> cps, Location loc) {

        this.union = union;
        this.cp = cp;
        this.cps = cps;
        this.loc = loc;

    }

    public Union getUnion() {

        return union;

    }

    public UnionPlayer getIssuer() {

        return cp;

    }

    public List<UnionPlayer> getPlayers() {

        return Collections.unmodifiableList(cps);

    }

    @Override
    public HandlerList getHandlers() {

        return handlers;

    }

    public static HandlerList getHandlerList() {

        return handlers;

    }

    @Override
    public boolean isCancelled() {

        return cancelled;

    }

    @Override
    public void setCancelled(boolean cancel) {

        cancelled = cancel;

    }

}
