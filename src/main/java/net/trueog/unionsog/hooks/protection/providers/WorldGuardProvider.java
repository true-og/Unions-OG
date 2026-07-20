package net.trueog.unionsog.hooks.protection.providers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.trueog.unionsog.hooks.protection.Coordinate;
import net.trueog.unionsog.hooks.protection.Land;
import net.trueog.unionsog.hooks.protection.ProtectionProvider;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class WorldGuardProvider implements ProtectionProvider {

    @Override
    public void setup() {

    }

    private @Nullable RegionManager getRegionManager(@Nullable World world) {

        if (world == null) {

            return null;

        }

        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));

    }

    private @NotNull Set<ProtectedRegion> getApplicableRegions(@NotNull RegionManager regionManager,
            @NotNull Location location)
    {

        ApplicableRegionSet regionSet = regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(location));
        return regionSet.getRegions();

    }

    @NotNull
    private Land getLand(@NotNull ProtectedRegion region) {

        return new Land(getIdPrefix() + region.getId(), region.getOwners().getUniqueIds(), getCoordinates(region));

    }

    @NotNull
    private List<Coordinate> getCoordinates(@NotNull ProtectedRegion region) {

        List<Coordinate> coordinates = new ArrayList<>();
        for (BlockVector2 point : region.getPoints()) {

            coordinates.add(new Coordinate(point.getBlockX(), point.getBlockZ()));

        }

        return coordinates;

    }

    @Override
    public @NotNull Set<Land> getLandsAt(@NotNull Location location) {

        HashSet<Land> lands = new HashSet<>();
        RegionManager regionManager = getRegionManager(location.getWorld());

        if (regionManager == null) {

            return lands;

        }

        for (ProtectedRegion region : getApplicableRegions(regionManager, location)) {

            lands.add(getLand(region));

        }

        return lands;

    }

    @Override
    public @NotNull Set<Land> getLandsOf(@NotNull OfflinePlayer player, @NotNull World world) {

        HashSet<Land> lands = new HashSet<>();
        RegionManager regionManager = getRegionManager(world);

        if (regionManager == null) {

            return lands;

        }

        for (ProtectedRegion region : regionManager.getRegions().values()) {

            if (!region.getOwners().getUniqueIds().contains(player.getUniqueId())) {

                continue;

            }

            lands.add(getLand(region));

        }

        return lands;

    }

    @Override
    public @NotNull String getIdPrefix() {

        return "wg";

    }

    @Override
    public void deleteLand(@NotNull String id, @NotNull World world) {

        RegionManager regionManager = getRegionManager(world);
        if (regionManager == null) {

            return;

        }

        regionManager.removeRegion(id.replaceFirst(getIdPrefix(), ""));

    }

    @Override
    public @Nullable Class<? extends Event> getCreateLandEvent() {

        return null;

    }

    @Override
    public @Nullable Player getPlayer(Event event) {

        return null;

    }

    @Override
    public @Nullable String getRequiredPluginName() {

        return "WorldGuard";

    }

}
