package net.trueog.unionsog.utils;

import net.trueog.unionsog.UnionsOG;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import static net.trueog.unionsog.UnionsOG.lang;

public class CurrencyFormat {

    /**
     * Formats a Diamond amount (at most one fractional digit, which counts Shards
     * 0-8) for display, e.g. 12.5 -> "12.5 Diamonds"
     */
    public static String format(double diamonds) {

        return format(diamonds, null);

    }

    /**
     * Formats a Diamond amount for display, in the viewer's language.
     *
     * @param viewer the recipient of the message, or null for the server language
     */
    public static String format(double diamonds, @Nullable CommandSender viewer) {

        return unit(trim(String.format(Locale.ROOT, "%.1f", diamonds)), viewer);

    }

    /**
     * Formats a Shard amount as Diamonds for display, e.g. 113 -> "12.5 Diamonds"
     */
    public static String formatShards(long shards) {

        return formatShards(shards, null);

    }

    /**
     * Formats a Shard amount as Diamonds for display, in the viewer's language.
     *
     * @param viewer the recipient of the message, or null for the server language
     */
    public static String formatShards(long shards, @Nullable CommandSender viewer) {

        return unit(trim(UnionsOG.getInstance().getPermissionsManager().shardsToDiamonds(shards)), viewer);

    }

    private static String unit(String amount, @Nullable CommandSender viewer) {

        return viewer == null ? lang("currency.diamonds", amount) : lang("currency.diamonds", viewer, amount);

    }

    private static String trim(String diamonds) {

        return diamonds.endsWith(".0") ? diamonds.substring(0, diamonds.length() - 2) : diamonds;

    }

}
