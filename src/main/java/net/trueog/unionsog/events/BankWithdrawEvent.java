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
public class BankWithdrawEvent extends PlayerEvent implements Cancellable {

    private final Union union;
    private double amount;
    private boolean cancelled;
    private static final HandlerList HANDLER_LIST = new HandlerList();

    public BankWithdrawEvent(@NotNull Player who, @NotNull Union union, double amount) {

        super(who);
        this.union = union;
        this.amount = amount;

    }

    public @NotNull Union getUnion() {

        return union;

    }

    public double getOldBalance() {

        return union.getBalance();

    }

    public void setAmount(double amount) {

        this.amount = amount;

    }

    public double getAmount() {

        return amount;

    }

    public static HandlerList getHandlerList() {

        return HANDLER_LIST;

    }

    @Override
    public @NotNull HandlerList getHandlers() {

        return HANDLER_LIST;

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
