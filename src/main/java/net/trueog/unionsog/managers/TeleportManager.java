package net.trueog.unionsog.managers;

import io.papermc.lib.PaperLib;
import net.trueog.unionsog.*;
import net.trueog.unionsog.events.UnionPlayerTeleportEvent;
import net.trueog.unionsog.utils.VanishUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;
import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.RED;

/**
 * Class responsible for managing teleports and its queue
 */
public final class TeleportManager {

    private final UnionsOG plugin;
    private final HashMap<String, TeleportState> waitingPlayers = new HashMap<>();

    public TeleportManager() {

        plugin = UnionsOG.getInstance();
        startCounter();

    }

    /**
     * Add player to teleport waiting queue
     *
     * @param player      the Player
     * @param destination the destination
     * @param unionName   the Union name
     */
    public void addPlayer(Player player, Location destination, String unionName) {

        PermissionsManager pm = plugin.getPermissionsManager();

        int secs = UnionsOG.getInstance().getSettingsManager().getInt(UNION_TELEPORT_DELAY);
        if (pm.has(player, "unionsog.mod.bypass") || pm.has(player, "unionsog.vip.teleport-delay")) {

            secs = 0;

        }

        waitingPlayers.put(player.getUniqueId().toString(), new TeleportState(player, destination, unionName, secs));

        if (secs > 0) {

            ChatBlock.sendMessage(player, AQUA + lang("waiting.for.teleport.stand.still.for.0.seconds", player, secs));

        }

    }

    /**
     * Teleports all online and non-vanished members of this {@link Union} to the
     * specified {@link Location}
     *
     * @param requester the Player requesting the teleport
     * @param union     the Union
     * @param location  the Location
     */
    /**
     * Regroups a {@link Union} at the specified {@link Location}.
     * <p>
     * Every other member is asked first and is only moved once they agree, since
     * being teleported away is theirs to refuse. The requester asked for it, so
     * they go without being asked.
     * </p>
     *
     * @param requester the Player requesting the teleport
     * @param union     the Union
     * @param location  the Location
     */
    public void teleport(@NotNull Player requester, @NotNull Union union, @NotNull Location location) {

        UnionPlayer asker = plugin.getUnionManager().getUnionPlayer(requester);
        int asked = 0;

        for (UnionPlayer cp : VanishUtils.getNonVanished(requester, union)) {

            if (cp.equals(asker)) {

                queueRegroup(requester, union, location);
                continue;

            }

            if (asker != null && plugin.getRequestManager().addRegroupRequest(asker, cp, union, location)) {

                asked++;

            }

        }

        ChatBlock.sendMessage(requester, AQUA + lang("regroup.asked.0.members", requester, asked));

    }

    /**
     * Teleports all online and non-vanished members of this {@link Union} to the
     * specified {@link Location}
     *
     * @param union    the Union
     * @param location the Location
     */
    public void teleport(Union union, Location location) {

        teleport(union, location, VanishUtils.getNonVanished(null, union));

    }

    public void teleportToHome(@NotNull Player player, @NotNull Location destination, @NotNull String unionName) {

        PaperLib.teleportAsync(player, getSafe(destination), PlayerTeleportEvent.TeleportCause.COMMAND)
                .thenAccept(result ->
                {

                    if (result) {

                        ChatBlock.sendMessage(player, AQUA + lang("now.at.homebase", player, unionName));
                        celebrateArrival(player);

                    } else {

                        plugin.getLogger().log(Level.WARNING, "An error occurred while teleporting a player");

                    }

                });

    }

    public void teleportToHome(@NotNull Player player, @NotNull Union union) {

        if (union.getHomeLocation() == null) {

            return;

        }

        teleportToHome(player, union.getHomeLocation(), union.getName());

    }

    /**
     * Marks an arrival at a union home with a short burst of particles.
     * <p>
     * Spawned through the world rather than the player so that everyone in range
     * sees it, the arriving member included. Both particles date back to 1.8 and
     * carry no particle data, which is what ViaVersion needs to translate them
     * instead of dropping them, so legacy clients see the burst too.
     * </p>
     *
     * @param player the arriving member
     */
    private void celebrateArrival(@NotNull Player player) {

        World world = player.getWorld();
        Location centre = player.getLocation().add(0, 1, 0);

        world.spawnParticle(Particle.VILLAGER_HAPPY, centre, 40, 0.4, 0.6, 0.4, 0);
        world.spawnParticle(Particle.FIREWORKS_SPARK, centre, 20, 0.3, 0.4, 0.3, 0.04);

    }

    private boolean isSameBlock(Location loc, Location loc2) {

        return loc.getBlockX() == loc2.getBlockX() && loc.getBlockY() == loc2.getBlockY()
                && loc.getBlockZ() == loc2.getBlockZ();

    }

    /**
     * Converts the specified {@link Location} to a safe one, i.e. where there is no
     * risk of suffocation
     *
     * @param location the Location
     * @return the safe Location
     */
    public @NotNull Location getSafe(@NotNull Location location) {

        int counter = 0;
        while (counter < 256) { // max world height

            counter++;
            Block bottom = location.getBlock();
            Block top = location.add(0, 1, 0).getBlock();
            if (!isAir(bottom)) {

                continue;

            }

            if (!isAir(top)) {

                location.add(0, 1, 0); // skips checking the same block again
                continue;

            }

            location.subtract(0, 1, 0); // remove what was added above
            return location;

        }

        // noinspection ConstantConditions
        location.setY(location.getWorld().getHighestBlockYAt(location) + 1);
        return location;

    }

    private void dropItems(Player player) {

        if (plugin.getPermissionsManager().has(player, "unionsog.mod.keep-items")) {

            return;

        }

        List<Material> itemsList = plugin.getSettingsManager().getItemList();
        PlayerInventory inv = player.getInventory();
        boolean dropOnHome = plugin.getSettingsManager().is(DROP_ITEMS_ON_UNION_HOME);
        boolean keepOnHome = plugin.getSettingsManager().is(KEEP_ITEMS_ON_UNION_HOME);
        ItemStack[] contents = inv.getContents();
        for (ItemStack item : contents) {

            if (item == null) {

                continue;

            }

            if ((dropOnHome && itemsList.contains(item.getType()))
                    || (keepOnHome && !itemsList.contains(item.getType())))
            {

                player.getWorld().dropItemNaturally(player.getLocation(), item);
                inv.remove(item);

            }

        }

    }

    private void startCounter() {

        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {

            waitingPlayers.values().removeIf(ts -> ts.getPlayer() == null);
            for (Iterator<TeleportState> iter = waitingPlayers.values().iterator(); iter.hasNext();) {

                TeleportState state = iter.next();
                Player player = state.getPlayer();
                if (state.isProcessing() || player == null) {

                    continue;

                }

                state.setProcessing(true);

                if (!isSameBlock(player.getLocation(), state.getLocation())) {

                    ChatBlock.sendMessage(player, RED + lang("you.moved.teleport.cancelled", player));
                    iter.remove();
                    continue;

                }

                if (state.isTeleportTime()) {

                    teleport(state);
                    iter.remove();

                } else {

                    ChatBlock.sendMessage(player, AQUA + "" + state.getCounter());

                }

                state.setProcessing(false);

            }

        }, 0, 20L);

    }

    private void teleport(TeleportState state) {

        Player player = state.getPlayer();
        if (player == null) {

            return;

        }

        UnionPlayer cp = plugin.getUnionManager().getCreateUnionPlayer(player.getUniqueId());
        UnionPlayerTeleportEvent event = new UnionPlayerTeleportEvent(cp, state.getLocation(), state.getDestination());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {

            return;

        }

        Location loc = state.getDestination();
        sendTeleportBlocks(player, loc);
        dropItems(player);
        loc.clone().add(.5, .5, .5);
        teleportToHome(player, loc, state.getUnionName());

    }

    @SuppressWarnings("deprecation")
    private void sendTeleportBlocks(Player player, Location loc) {

        int x = loc.getBlockX();
        int z = loc.getBlockZ();

        if (plugin.getSettingsManager().is(TELEPORT_BLOCKS)) {

            player.sendBlockChange(new Location(loc.getWorld(), x + 1, loc.getBlockY() - 1, z + 1), Material.GLASS,
                    (byte) 0);
            player.sendBlockChange(new Location(loc.getWorld(), x - 1, loc.getBlockY() - 1, z - 1), Material.GLASS,
                    (byte) 0);
            player.sendBlockChange(new Location(loc.getWorld(), x + 1, loc.getBlockY() - 1, z - 1), Material.GLASS,
                    (byte) 0);
            player.sendBlockChange(new Location(loc.getWorld(), x - 1, loc.getBlockY() - 1, z + 1), Material.GLASS,
                    (byte) 0);

        }

    }

    private void teleport(Union union, Location location, List<UnionPlayer> members) {

        for (UnionPlayer cp : members) {

            Player player = cp.toPlayer();
            if (player == null) {

                continue;

            }

            queueRegroup(player, union, location);

        }

    }

    /**
     * Queues one member's regroup teleport, spread a block off the destination so
     * that a union does not land in a single square.
     *
     * @param player   the member to move
     * @param union    the Union
     * @param location the destination
     */
    public void queueRegroup(@NotNull Player player, @NotNull Union union, @NotNull Location location) {

        int x = location.getBlockX();
        int z = location.getBlockZ();
        sendTeleportBlocks(player, location);

        Random r = new Random();
        int xx = r.nextInt(2) - 1;
        int zz = r.nextInt(2) - 1;
        if (xx == 0 && zz == 0) {

            xx = 1;

        }

        x = x + xx;
        z = z + zz;

        addPlayer(player, new Location(location.getWorld(), x + .5, location.getBlockY(), z + .5, location.getYaw(),
                location.getPitch()), union.getName());

    }

    /**
     * Checks if all passed blocks are some kind of AIR
     *
     * @param blocks blocks to test
     * @return true if all blocks are AIR
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isAir(@NotNull Block... blocks) {

        for (Block b : blocks) {

            if (!b.getType().name().contains("AIR")) {

                return false;

            }

        }

        return true;

    }

}