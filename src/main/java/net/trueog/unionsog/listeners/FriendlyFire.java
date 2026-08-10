package net.trueog.unionsog.listeners;

import net.trueog.unionsog.ChatBlock;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.UnionsOG;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.trueog.unionsog.managers.SettingsManager.ConfigField.GLOBAL_FRIENDLY_FIRE;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.SAFE_CIVILIANS;

public class FriendlyFire extends SCListener {

    private final Map<UUID, Long> warned = new HashMap<>();
    private static final long WARN_DELAY = 10000;

    public FriendlyFire(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof Player) || isBlacklistedWorld(event.getEntity())) {

            return;

        }

        Player victim = (Player) event.getEntity();
        Player attacker = Events.getAttacker(event);

        if (attacker == null || attacker.getUniqueId().equals(victim.getUniqueId())) {

            return;

        }

        UnionPlayer vcp = plugin.getUnionManager().getUnionPlayer(victim);

        Union victimUnion = vcp == null ? null : vcp.getUnion();
        Union attackerUnion = plugin.getUnionManager().getUnionByPlayerUniqueId(attacker.getUniqueId());

        process(event, attacker, vcp, victimUnion, attackerUnion);

    }

    private void process(EntityDamageEvent event, Player attacker, @Nullable UnionPlayer vcp,
            @Nullable Union victimUnion, @Nullable Union attackerUnion)
    {

        if (vcp == null || victimUnion == null || attackerUnion == null) {

            if (plugin.getSettingsManager().is(SAFE_CIVILIANS)) {

                ChatBlock.sendMessageKey(attacker, "cannot.attack.civilians");
                event.setCancelled(true);

            }

            return;

        }

        if (vcp.isFriendlyFire() || victimUnion.isFriendlyFire()
                || plugin.getSettingsManager().is(GLOBAL_FRIENDLY_FIRE))
        {

            return;

        }

        if (victimUnion.equals(attackerUnion)) {

            warn(attacker, "cannot.attack.union.member");
            event.setCancelled(true);
            return;

        }

        if (victimUnion.isAlly(attackerUnion.getTag())) {

            warn(attacker, "cannot.attack.ally");
            event.setCancelled(true);

        }

    }

    private void warn(Player attacker, String messageKey) {

        long timestamp = warned.getOrDefault(attacker.getUniqueId(), 0L);
        long currentTimeMillis = System.currentTimeMillis();

        if (timestamp + WARN_DELAY <= currentTimeMillis) {

            ChatBlock.sendMessageKey(attacker, messageKey);
            warned.put(attacker.getUniqueId(), currentTimeMillis);

        }

    }

}
