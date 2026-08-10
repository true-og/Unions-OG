package net.trueog.unionsog.ui;

import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.events.ComponentClickEvent;
import net.trueog.unionsog.managers.PermissionsManager;
import net.trueog.unionsog.ui.frames.ConfirmationFrame;
import net.trueog.unionsog.ui.frames.WarningFrame;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.COMMANDS_UNION;

/**
 *
 * @author RoinujNosde
 *
 */
public class InventoryController implements Listener {

    private static final Map<UUID, SCFrame> frames = new HashMap<>();

    @EventHandler(ignoreCancelled = true)
    public void onClose(InventoryCloseEvent event) {

        HumanEntity entity = event.getPlayer();
        if (!(entity instanceof Player)) {

            return;

        }

        frames.remove(entity.getUniqueId());

    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(InventoryClickEvent event) {

        HumanEntity entity = event.getWhoClicked();
        if (!(entity instanceof Player)) {

            return;

        }

        SCFrame frame = frames.get(entity.getUniqueId());
        if (frame == null) {

            return;

        }

        event.setCancelled(true);

        if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) {

            return;

        }

        SCComponent component = frame.getComponent(event.getSlot());
        if (component == null) {

            return;

        }

        ClickType click = event.getClick();
        Runnable listener = component.getListener(click);
        if (listener == null) {

            return;

        }

        String permission = component.getPermission(click);
        if (permission != null) {

            if (!hasPermission((Player) entity, permission)) {

                InventoryDrawer.open(new WarningFrame(frame, (Player) entity, permission));
                return;

            }

        }

        if (component.isConfirmationRequired(click)) {

            listener = () -> InventoryDrawer
                    .open(new ConfirmationFrame(frame, frame.getViewer(), component.getListener(click)));

        }

        Runnable finalListener = listener;
        Bukkit.getScheduler().runTask(UnionsOG.getInstance(), () -> {

            ItemStack currentItem = event.getCurrentItem();
            if (currentItem == null)
                return;

            ComponentClickEvent componentClickEvent = new ComponentClickEvent(((Player) entity), frame, component);
            Bukkit.getPluginManager().callEvent(componentClickEvent);
            if (componentClickEvent.isCancelled()) {

                return;

            }

            ItemMeta itemMeta = currentItem.getItemMeta();
            Objects.requireNonNull(itemMeta).setLore(Collections.singletonList(lang("gui.loading", (Player) entity)));
            currentItem.setItemMeta(itemMeta);

            finalListener.run();

        });

    }

    /**
     * Checks if the player has the permission
     *
     * @param player     the Player
     * @param permission the permission
     * @return true if they have permission
     *
     * @author RoinujNosde
     */
    private boolean hasPermission(@NotNull Player player, @NotNull String permission) {

        PermissionsManager pm = UnionsOG.getInstance().getPermissionsManager();

        return pm.has(player, permission);

    }

    /**
     * Registers the frame in the InventoryController
     * 
     * @param frame the frame
     *              <p>
     *              author: RoinujNosde
     */
    public static void register(@NotNull SCFrame frame) {

        frames.put(frame.getViewer().getUniqueId(), frame);

    }

    /**
     * Checks if the Player is registered
     *
     * @param player the Player
     * @return if they are registered
     */
    public static boolean isRegistered(@NotNull Player player) {

        return frames.containsKey(player.getUniqueId());

    }

    /**
     * Runs a subcommand for the Player
     * 
     * @param player     the Player
     * @param subcommand the subcommand
     * @param update     whether to update the inventory instead of closing
     *                   <p>
     *                   author: RoinujNosde
     *                   </p>
     */
    public static void runSubcommand(@NotNull Player player, @NotNull String subcommand, boolean update,
            String... args)
    {

        UnionsOG plugin = UnionsOG.getInstance();
        String baseCommand = plugin.getSettingsManager().getString(COMMANDS_UNION);
        String finalCommand = String.format("%s %s ", baseCommand, subcommand) + String.join(" ", args);
        new BukkitRunnable() {

            @Override
            public void run() {

                player.performCommand(finalCommand);
                if (!update) {

                    player.closeInventory();

                } else {

                    SCFrame currentFrame = frames.get(player.getUniqueId());
                    if (currentFrame instanceof ConfirmationFrame) {

                        currentFrame = currentFrame.getParent();

                    }

                    InventoryDrawer.open(currentFrame);

                }

            }

        }.runTask(plugin);

    }

}
