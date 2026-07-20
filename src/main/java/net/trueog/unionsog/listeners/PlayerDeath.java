package net.trueog.unionsog.listeners;

import net.trueog.unionsog.*;
import net.trueog.unionsog.events.AddKillEvent;
import net.trueog.unionsog.managers.PermissionsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.logging.Level;

import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;

/**
 * @author phaed
 */
public class PlayerDeath extends SCListener {

    public PlayerDeath(UnionsOG plugin) {

        super(plugin);

    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDeath(PlayerDeathEvent event) {

        Player victim = event.getEntity();
        if (isNPC(victim) || isBlacklistedWorld(victim)) {

            return;

        }

        Player attacker = Events.getAttacker(victim.getLastDamageCause());
        if (isInvalidKill(victim, attacker))
            return;

        ClanPlayer victimCp = plugin.getClanManager().getCreateClanPlayer(victim.getUniqueId());
        ClanPlayer attackerCp = plugin.getClanManager().getCreateClanPlayer(attacker.getUniqueId());

        classifyKill(victimCp, attackerCp);

        // record death for victim
        victimCp.addDeath();
        plugin.getStorageManager().updateClanPlayer(victimCp);
        plugin.getStorageManager().updateClanPlayer(attackerCp);

    }

    @EventHandler
    public void onWarDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();
        Player killer = player.getKiller();
        if (killer == null) {

            return;

        }

        Clan victimClan = plugin.getClanManager().getClanByPlayerUniqueId(player.getUniqueId());
        Clan killerClan = plugin.getClanManager().getClanByPlayerUniqueId(killer.getUniqueId());
        War war = plugin.getProtectionManager().getWar(victimClan, killerClan);
        if (war == null || victimClan == null) {

            return;

        }

        war.increaseCasualties(victimClan);

    }

    private void classifyKill(@NotNull ClanPlayer victim, @NotNull ClanPlayer attacker) {

        Clan victimClan = victim.getClan();
        Clan attackerClan = attacker.getClan();
        if (victimClan == null || attackerClan == null || !victimClan.isVerified() || !attackerClan.isVerified()) {

            addKill(Kill.Type.CIVILIAN, attacker, victim);

        } else if (attackerClan.isRival(victim.getTag())) {

            addKill(Kill.Type.RIVAL, attacker, victim);

        } else if (attackerClan.isAlly(victimClan.getTag()) || attackerClan.equals(victimClan)) {

            addKill(Kill.Type.ALLY, attacker, victim);

        } else {

            addKill(Kill.Type.NEUTRAL, attacker, victim);

        }

    }

    @Contract("_, null -> true")
    private boolean isInvalidKill(@NotNull Player victim, @Nullable Player attacker) {

        if (attacker == null || attacker.getUniqueId().equals(victim.getUniqueId())) {

            UnionsOG.debug("Attacker is not a player or victim and attacker have the same UUID");
            return true;

        }

        if (UnionsOG.getInstance().getSettingsManager().is(KILL_WEIGHTS_DENY_SAME_IP_KILLS)) {

            InetSocketAddress attackerAddress = attacker.getAddress();
            InetSocketAddress victimAddress = victim.getAddress();
            if (attackerAddress != null && victimAddress != null) {

                if (attackerAddress.getHostString().equals(victimAddress.getHostString())) {

                    plugin.getLogger().log(Level.INFO, "Blocked same IP kill calculating: {0} killed {1}. IP: {2}",
                            new Object[]
                            { attacker.getDisplayName(), victim.getDisplayName(), attackerAddress.getHostString() });
                    return true;

                }

            }

        }

        AddKillEvent addKillEvent = new AddKillEvent(
                plugin.getClanManager().getCreateClanPlayer(attacker.getUniqueId()),
                plugin.getClanManager().getCreateClanPlayer(victim.getUniqueId()));
        Bukkit.getServer().getPluginManager().callEvent(addKillEvent);
        if (addKillEvent.isCancelled()) {

            return true;

        }

        String kdrExempt = "unionsog.other.kdr-exempt";
        PermissionsManager pm = plugin.getPermissionsManager();
        return pm.has(attacker, kdrExempt) || pm.has(victim, kdrExempt);

    }

    private void addKill(Kill.Type type, ClanPlayer attacker, ClanPlayer victim) {

        if (type == null || attacker == null || victim == null) {

            return;

        }

        final Kill kill = new Kill(attacker, victim, LocalDateTime.now());
        if (plugin.getSettingsManager().is(KDR_ENABLE_KILL_DELAY) && plugin.getClanManager().isKillBeforeDelay(kill)) {

            return;

        }

        if (plugin.getSettingsManager().is(KDR_ENABLE_MAX_KILLS)) {

            plugin.getStorageManager().getKillsPerPlayer(attacker.getName(), data -> {

                final int max = plugin.getSettingsManager().getInt(KDR_MAX_KILLS_PER_VICTIM);
                Integer kills = data.get(kill.getVictim().getName());
                if (kills != null) {

                    if (kills < max) {

                        saveKill(kill, type);

                    }

                } else {

                    saveKill(kill, type);

                }

            });
            return;

        }

        saveKill(kill, type);

    }

    private void saveKill(Kill kill, Kill.Type type) {

        plugin.getClanManager().addKill(kill);
        ClanPlayer killer = kill.getKiller();
        ClanPlayer victim = kill.getVictim();
        killer.addKill(type);
        plugin.getStorageManager().insertKill(killer, victim, type.getShortname(), kill.getTime());

    }

    private boolean isNPC(Player player) {

        if (player.hasMetadata("NPC")) {

            UnionsOG.debug(String.format("%s has NPC metadata", player.getName()));
            return true;

        }

        if (Bukkit.getOfflinePlayer(player.getUniqueId()).getName() == null) {

            UnionsOG.debug(String.format("%s has a null name", player.getUniqueId()));
            return true;

        }

        return false;

    }

}
