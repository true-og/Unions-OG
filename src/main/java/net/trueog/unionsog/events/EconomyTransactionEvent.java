package net.trueog.unionsog.events;

import net.trueog.unionsog.managers.PermissionsManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is fired when Shards are granted to or charged from an online
 * player's DiamondBank-OG account.
 * <p>
 * Note: Cancelling this event will revert the transaction.
 * </p>
 *
 * @see PermissionsManager#grantPlayerShards(Player, long, Cause)
 * @see PermissionsManager#chargePlayerShards(Player, long, Cause)
 */
public class EconomyTransactionEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final long amount;
    private final Cause cause;
    private final TransactionType transactionType;
    private boolean cancelled;

    public EconomyTransactionEvent(@NotNull Player affected, long amount, @NotNull Cause cause,
            @NotNull TransactionType transactionType)
    {

        this.player = affected;
        this.amount = amount;
        this.cause = cause;
        this.transactionType = transactionType;

    }

    public Player getPlayer() {

        return player;

    }

    /**
     * @return the transaction amount, in Shards
     */
    public long getAmount() {

        return amount;

    }

    @NotNull
    public Cause getCause() {

        return cause;

    }

    @SuppressWarnings("unused")
    public TransactionType getTransactionType() {

        return transactionType;

    }

    @Override
    public @NotNull HandlerList getHandlers() {

        return handlers;

    }

    public static HandlerList getHandlerList() {

        return handlers;

    }

    @Override
    public boolean isCancelled() {

        return cancelled;

    }

    /**
     * If cancelled, the transaction will be reverted.
     */
    @Override
    public void setCancelled(boolean cancelled) {

        this.cancelled = cancelled;

    }

    public enum Cause {
        CLAN_CREATION, CLAN_INVITATION, CLAN_REGROUP, CLAN_HOME_TELEPORT, CLAN_HOME_TELEPORT_SET
    }

    public enum TransactionType {
        DEPOSIT, WITHDRAW
    }

}
