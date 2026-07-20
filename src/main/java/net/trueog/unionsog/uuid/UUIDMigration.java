package net.trueog.unionsog.uuid;

import net.trueog.unionsog.ClanPlayer;
import net.trueog.unionsog.UnionsOG;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 *
 * @author NeT32
 */
public class UUIDMigration {

    private UUIDMigration() {

    }

    public static boolean canReturnUUID() {

        try {

            Bukkit.class.getDeclaredMethod("getPlayer", UUID.class);
            return true;

        } catch (NoSuchMethodException e) {

            return false;

        }

    }

    @Deprecated
    public static UUID getForcedPlayerUUID(String playerName) {

        Player player = Bukkit.getPlayerExact(playerName);

        if (player != null) {

            return player.getUniqueId();

        } else {

            for (ClanPlayer cp : UnionsOG.getInstance().getClanManager().getAllClanPlayers()) {

                if (cp.getName().equalsIgnoreCase(playerName)) {

                    return cp.getUniqueId();

                }

            }

            @SuppressWarnings("deprecation")
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            return offlinePlayer.getUniqueId();

        }

    }

}
