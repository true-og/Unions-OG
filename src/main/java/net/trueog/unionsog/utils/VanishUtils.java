package net.trueog.unionsog.utils;

import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.UnionsOG;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VanishUtils {

    private VanishUtils() {

    }

    public static @NotNull List<UnionPlayer> getNonVanished(@Nullable CommandSender viewer, @NotNull Union union) {

        return getNonVanished(viewer, union.getMembers());

    }

    public static @NotNull List<UnionPlayer> getNonVanished(@Nullable CommandSender viewer,
            @NotNull List<UnionPlayer> unionPlayers)
    {

        ArrayList<UnionPlayer> nonVanished = new ArrayList<>();
        for (UnionPlayer cp : unionPlayers) {

            if (!isVanished(viewer, cp)) {

                nonVanished.add(cp);

            }

        }

        return nonVanished;

    }

    public static boolean isVanished(@Nullable CommandSender viewer, @NotNull UnionPlayer cp) {

        if (isVanished(cp)) {

            return true;

        }

        Player player = cp.toPlayer();
        if (viewer instanceof Player && player != null) {

            return !((Player) viewer).canSee(player);

        }

        return false;

    }

    public static boolean isVanished(@Nullable CommandSender viewer, @NotNull Player player) {

        if (viewer instanceof Player) {

            return !((Player) viewer).canSee(player);

        }

        return checkMetadata(player);

    }

    public static boolean isVanished(@NotNull UnionPlayer cp) {

        if (!isOnline(cp)) {

            return true;

        }

        Player player = cp.toPlayer();
        return player != null && checkMetadata(player);

    }

    private static boolean checkMetadata(Player player) {

        if (player.hasMetadata("vanished") && !player.getMetadata("vanished").isEmpty()) {

            return player.getMetadata("vanished").get(0).asBoolean();

        }

        return false;

    }

    public static boolean isOnline(@NotNull UnionPlayer player) {

        return UnionsOG.getInstance().getProxyManager().isOnline(player.getName());

    }

}
