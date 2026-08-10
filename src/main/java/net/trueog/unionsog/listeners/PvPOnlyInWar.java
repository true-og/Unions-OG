package net.trueog.unionsog.listeners;

import net.trueog.unionsog.ChatBlock;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionsOG;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.Nullable;

import static net.trueog.unionsog.managers.SettingsManager.ConfigField.PVP_ONLY_WHILE_IN_WAR;

public class PvPOnlyInWar extends SCListener {

    public PvPOnlyInWar(UnionsOG plugin) {

        super(plugin);

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof Player) || isBlacklistedWorld(event.getEntity())) {

            return;

        }

        Player victim = (Player) event.getEntity();
        Player attacker = Events.getAttacker(event);

        if (attacker == null || victim.getUniqueId().equals(attacker.getUniqueId())) {

            return;

        }

        Union attackerUnion = plugin.getUnionManager().getUnionByPlayerUniqueId(attacker.getUniqueId());
        Union victimUnion = plugin.getUnionManager().getUnionByPlayerUniqueId(victim.getUniqueId());

        if (plugin.getSettingsManager().is(PVP_ONLY_WHILE_IN_WAR)) {

            process(event, attacker, victim, attackerUnion, victimUnion);

        }

    }

    private void process(EntityDamageEvent event, Player attacker, Player victim, @Nullable Union attackerUnion,
            @Nullable Union victimUnion)
    {

        if (attackerUnion == null || victimUnion == null) {

            ChatBlock.sendMessageKey(attacker, "must.be.in.union.to.pvp", victim.getName());
            event.setCancelled(true);
            return;

        }

        if (plugin.getPermissionsManager().has(victim, "unionsog.mod.nopvpinwar")) {

            event.setCancelled(true);
            return;

        }

        if (!attackerUnion.isWarring(victimUnion)) {

            ChatBlock.sendMessageKey(attacker, "unions.not.at.war.pvp.denied", victimUnion.getName());
            event.setCancelled(true);

        }

    }

}
