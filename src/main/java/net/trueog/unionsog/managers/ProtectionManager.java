package net.trueog.unionsog.managers;

import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.War;
import net.trueog.unionsog.events.WarEndEvent;
import net.trueog.unionsog.events.WarStartEvent;
import net.trueog.unionsog.hooks.protection.Land;
import net.trueog.unionsog.hooks.protection.ProtectionProvider;
import net.trueog.unionsog.listeners.LandProtection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static net.trueog.unionsog.UnionsOG.debug;
import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;

public class ProtectionManager {

    private final SettingsManager settingsManager;
    private final UnionManager unionManager;
    private final Logger logger;
    private final Map<War, BukkitTask> wars = new HashMap<>();
    private final List<ProtectionProvider> providers = new ArrayList<>();
    private LandProtection landProtection;
    private final UnionsOG plugin;

    public ProtectionManager() {

        plugin = UnionsOG.getInstance();
        settingsManager = plugin.getSettingsManager();
        unionManager = plugin.getUnionManager();
        logger = plugin.getLogger();
        if (!settingsManager.is(ENABLE_WAR) && !settingsManager.is(LAND_SHARING)) {

            return;

        }

        // running on next tick, so all plugins are already loaded
        Bukkit.getScheduler().runTask(plugin, this::registerProviders);
        clearWars();

    }

    public void registerListeners() {

        landProtection = new LandProtection(plugin);
        if (!settingsManager.is(ENABLE_WAR) && !settingsManager.is(LAND_SHARING)) {

            return;

        }

        landProtection.registerListeners();

    }

    /**
     * Gets the Player's lands at the given Location, if not found, returns all
     * Player's lands in the World
     *
     * @param player   the Player
     * @param location the Location
     * @return the lands
     */
    public Set<Land> getLands(@NotNull Player player, @NotNull Location location) {

        Set<Land> lands = getLandsAt(location);
        lands.removeIf(land -> !land.getOwners().contains(player.getUniqueId()));
        if (lands.isEmpty()) {

            lands = getLandsOf(player, player.getWorld());

        }

        return lands;

    }

    @NotNull
    public Set<Land> getLandsAt(@NotNull Location location) {

        Set<Land> lands = new HashSet<>();
        for (ProtectionProvider provider : providers) {

            lands.addAll(provider.getLandsAt(location));

        }

        return lands;

    }

    public boolean isOwner(@NotNull OfflinePlayer player, @NotNull Location location) {

        debug(String.format("isOwner: player %s %s -> %s", player.getName(), player.getUniqueId(), location));
        for (Land land : getLandsAt(location)) {

            debug(String.format("land -> id %s - owners %s", land.getId(), land.getOwners()));
            if (land.getOwners().contains(player.getUniqueId())) {

                return true;

            }

        }

        return false;

    }

    @NotNull
    public Set<Land> getLandsOf(@NotNull OfflinePlayer player, @NotNull World world) {

        Set<Land> lands = new HashSet<>();
        for (ProtectionProvider provider : providers) {

            lands.addAll(provider.getLandsOf(player, world));

        }

        return lands;

    }

    public boolean can(@NotNull Action action, @NotNull Location location, @NotNull Player player) {

        return can(action, location, player, null);

    }

    public boolean can(@NotNull Action action, @NotNull Location location, @NotNull Player player,
            @Nullable Player other)
    {

        for (Land land : getLandsAt(location)) {

            for (UUID owner : land.getOwners()) {

                if (owner == null) {

                    continue;

                }

                Player involved;
                if (other != null && player.getUniqueId().equals(owner)) {

                    involved = other;

                } else {

                    involved = player;

                }

                if (isWarringAndAllowed(action, owner, involved)
                        || isSameUnionAndAllowed(action, owner, involved, land.getId()))
                {

                    return true;

                }

            }

        }

        return false;

    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean addWar(@NotNull UnionPlayer requester, Union requestUnion, Union targetUnion) {

        War war = new War(requestUnion, targetUnion);

        if (wars.containsKey(war)) {

            return false;

        }

        WarStartEvent event = new WarStartEvent(war);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {

            return false;

        }

        requestUnion.addWarringUnion(requester, targetUnion);
        targetUnion.addWarringUnion(requester, requestUnion);

        wars.put(war, scheduleTask(war, settingsManager.getMinutes(WAR_NORMAL_EXPIRATION_TIME)));
        return true;

    }

    @Nullable
    private BukkitTask scheduleTask(@NotNull War war, int expirationTime) {

        BukkitTask timeoutTask = null;
        if (expirationTime > 0) {

            timeoutTask = Bukkit.getScheduler().runTaskLater(plugin, new WarTimeoutTask(war), expirationTime);

        }

        return timeoutTask;

    }

    public void setWarExpirationTime(@NotNull Union union, int expirationTime) {

        if (expirationTime < 1) {

            return;

        }

        for (Map.Entry<War, BukkitTask> entry : wars.entrySet()) {

            War war = entry.getKey();
            if (!war.getUnions().contains(union)) {

                continue;

            }

            BukkitTask task = entry.getValue();
            if (task != null && !task.isCancelled()) {

                task.cancel();

            }

            entry.setValue(scheduleTask(war, expirationTime));

        }

    }

    public void removeWar(@Nullable War war, @NotNull WarEndEvent.Reason reason) {

        if (war == null) {

            return;

        }

        wars.remove(war);
        Union union1 = war.getUnions().get(0);
        Union union2 = war.getUnions().get(1);
        union1.removeWarringUnion(union2);
        union2.removeWarringUnion(union1);

        WarEndEvent event = new WarEndEvent(war, reason);
        Bukkit.getPluginManager().callEvent(event);

    }

    public @Nullable War getWar(@Nullable Union union1, @Nullable Union union2) {

        if (union1 == null || union2 == null) {

            return null;

        }

        for (War war : wars.keySet()) {

            List<Union> unions = war.getUnions();
            if (unions.contains(union1) && unions.contains(union2)) {

                return war;

            }

        }

        return null;

    }

    private void clearWars() {

        if (!settingsManager.is(ENABLE_WAR)) {

            return;

        }

        for (Union union : unionManager.getUnions()) {

            for (Union warringUnion : union.getWarringUnions()) {

                union.removeWarringUnion(warringUnion);

            }

        }

    }

    private boolean isSameUnionAndAllowed(Action action, UUID owner, Player involved, String landId) {

        if (!settingsManager.is(LAND_SHARING)) {

            return false;

        }

        UnionPlayer cp = unionManager.getCreateUnionPlayer(owner);
        Union involvedUnion = unionManager.getUnionByPlayerUniqueId(involved.getUniqueId());
        if (cp.getUnion() == null || !cp.getUnion().equals(involvedUnion)) {

            return false;

        }

        return cp.isAllowed(action, landId);

    }

    private boolean isWarringAndAllowed(@NotNull Action action, @NotNull UUID owner, @NotNull Player involved) {

        if (!settingsManager.isActionAllowedInWar(action) || !settingsManager.is(ENABLE_WAR)) {

            return false;

        }

        Union ownerUnion = unionManager.getUnionByPlayerUniqueId(owner);
        Union involvedUnion = unionManager.getUnionByPlayerUniqueId(involved.getUniqueId());
        if (ownerUnion == null || involvedUnion == null) {

            return false;

        }

        return ownerUnion.isWarring(involvedUnion);

    }

    private void registerProviders() {

        for (String className : settingsManager.getStringList(LAND_PROTECTION_PROVIDERS)) {

            Object instance = null;
            try {

                Class<?> clazz = getProviderClass(className);
                instance = clazz.getConstructor().newInstance();

            } catch (ClassNotFoundException ex) {

                logger.log(Level.WARNING, String.format("Provider %s not found!", className), ex);

            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                    | IllegalAccessException ex)
            {

                logger.log(Level.WARNING, String.format("Error instantiating provider %s", className), ex);

            }

            if (instance instanceof ProtectionProvider) {

                registerProvider((ProtectionProvider) instance);

            } else if (instance != null) {

                logger.warning(String.format("%s is not an instance of ProtectionProvider", className));

            }

        }

    }

    public void registerProvider(@NotNull ProtectionProvider provider) {

        String requiredPlugin = provider.getRequiredPluginName();
        String providerName = provider.getClass().getSimpleName();
        if (requiredPlugin != null && Bukkit.getPluginManager().getPlugin(requiredPlugin) == null) {

            debug(String.format("Required plugin %s for the provider %s was not found!", requiredPlugin, providerName));
            return;

        }

        try {

            provider.setup();

        } catch (LinkageError | Exception throwable) {

            logger.log(Level.WARNING, String.format("Error registering provider %s", providerName));
            if (settingsManager.is(DEBUG)) {

                throwable.printStackTrace();

            }

            return;

        }

        providers.add(provider);
        landProtection.registerCreateLandEvent(provider, provider.getCreateLandEvent());
        logger.info(String.format("Registered %s successfully", providerName));

    }

    @NotNull
    private Class<?> getProviderClass(String className) throws ClassNotFoundException {

        if ("WorldGuard6Provider".equals(className)
                || "net.trueog.unionsog.hooks.protection.providers.WorldGuard6Provider".equals(className))
        {

            className = "WorldGuardProvider";

        }

        try {

            return Class.forName("net.trueog.unionsog.hooks.protection.providers." + className);

        } catch (ClassNotFoundException ignored) {

        }

        return Class.forName(className);

    }

    public enum Action {
        BREAK, INTERACT, INTERACT_ENTITY, PLACE, DAMAGE, CONTAINER
    }

    private class WarTimeoutTask implements Runnable {

        private final War war;

        private WarTimeoutTask(War war) {

            this.war = war;

        }

        @Override
        public void run() {

            removeWar(war, WarEndEvent.Reason.EXPIRATION);
            Union union1 = war.getUnions().get(0);
            Union union2 = war.getUnions().get(1);
            union1.addBb(union1.getColorTag(), lang("war.expired", union2.getTag()));
            union2.addBb(union2.getColorTag(), lang("war.expired", union1.getTag()));

        }

    }

}
