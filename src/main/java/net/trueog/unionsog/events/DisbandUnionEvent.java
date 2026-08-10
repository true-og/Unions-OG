package net.trueog.unionsog.events;

import net.trueog.unionsog.Union;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author NeT32
 */
public class DisbandUnionEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final CommandSender sender;
    private final Union union;

    public DisbandUnionEvent(CommandSender sender, Union union) {

        if (sender == null) {

            sender = Bukkit.getConsoleSender();

        }

        this.sender = sender;
        this.union = union;

    }

    public Union getUnion() {

        return this.union;

    }

    public CommandSender getSender() {

        return sender;

    }

    @Override
    public HandlerList getHandlers() {

        return handlers;

    }

    public static HandlerList getHandlerList() {

        return handlers;

    }

}
