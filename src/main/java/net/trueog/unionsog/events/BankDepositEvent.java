package net.trueog.unionsog.events;

import net.trueog.unionsog.Union;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @deprecated use {@link UnionBalanceUpdateEvent}
 */
@Deprecated
public class BankDepositEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Union union;
    private double amount;
    private boolean cancelled;

    public BankDepositEvent(@NotNull Player who, @NotNull Union union, double amount) {

        super(who);
        this.union = union;
        this.amount = amount;

    }

    public static HandlerList getHandlerList() {

        return HANDLER_LIST;

    }

    @Override
    public @NotNull HandlerList getHandlers() {

        return HANDLER_LIST;

    }

    public @NotNull Union getUnion() {

        return union;

    }

    public double getOldBalance() {

        return union.getBalance();

    }

    @Override
    public boolean isCancelled() {

        return cancelled;

    }

    @Override
    public void setCancelled(boolean cancelled) {

        this.cancelled = cancelled;

    }

    public double getAmount() {

        return amount;

    }

    public void setAmount(double amount) {

        this.amount = amount;

    }

}
