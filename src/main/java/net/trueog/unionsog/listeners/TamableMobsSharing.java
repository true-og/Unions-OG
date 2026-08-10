package net.trueog.unionsog.listeners;

import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.managers.UnionManager;
import net.trueog.unionsog.managers.SettingsManager;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.managers.SettingsManager.ConfigField.TAMABLE_MOBS_SHARING;

public class TamableMobsSharing implements Listener {

    private final SettingsManager settings;
    private final UnionManager unionManager;

    public TamableMobsSharing(@NotNull UnionsOG plugin) {

        settings = plugin.getSettingsManager();
        unionManager = plugin.getUnionManager();

    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {

        if (settings.is(TAMABLE_MOBS_SHARING)) {

            if (event.getEntity() instanceof Wolf && event.getDamager() instanceof Player) {

                UnionPlayer cp = unionManager.getAnyUnionPlayer(event.getDamager().getUniqueId());
                if (cp == null || cp.getUnion() == null) {

                    return;

                }

                Wolf wolf = (Wolf) event.getEntity();
                AnimalTamer owner = wolf.getOwner();
                if (owner != null && cp.getUnion().isMember(owner.getUniqueId())) {

                    // Sets the wolf to friendly if the attacker is one out of his union
                    wolf.setAngry(false);

                }

            }

        }

    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {

        if (settings.is(TAMABLE_MOBS_SHARING)) {

            if (event.getEntity() instanceof Tameable && event.getTarget() instanceof Player) {

                UnionPlayer cp = unionManager.getAnyUnionPlayer(event.getTarget().getUniqueId());
                if (cp == null || cp.getUnion() == null) {

                    return;

                }

                Tameable wolf = (Tameable) event.getEntity();
                AnimalTamer owner = wolf.getOwner();
                if (owner == null) {

                    return;

                }

                if (cp.getUnion().isMember(owner.getUniqueId())) {

                    // cancels the event if the attacker is one out of his union
                    event.setCancelled(true);

                }

            }

        }

    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEntityEvent event) {

        if (settings.is(TAMABLE_MOBS_SHARING) && event.getRightClicked() instanceof Tameable) {

            Player player = event.getPlayer();
            UnionPlayer cp = unionManager.getAnyUnionPlayer(player.getUniqueId());
            if (cp == null || cp.getUnion() == null) {

                return;

            }

            Tameable tamed = (Tameable) event.getRightClicked();

            if (tamed.getOwner() != null) {

                if (tamed instanceof Wolf && !((Wolf) tamed).isSitting()) {

                    return;

                }

                if (cp.getUnion().isMember(tamed.getOwner().getUniqueId())) {

                    tamed.setOwner(player);

                }

            }

        }

    }

}
