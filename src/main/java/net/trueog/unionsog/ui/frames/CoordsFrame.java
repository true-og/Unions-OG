package net.trueog.unionsog.ui.frames;

import com.cryptomorin.xseries.XMaterial;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.ui.InventoryDrawer;
import net.trueog.unionsog.ui.SCComponent;
import net.trueog.unionsog.ui.SCComponentImpl;
import net.trueog.unionsog.ui.SCFrame;
import net.trueog.unionsog.utils.Paginator;
import net.trueog.unionsog.utils.VanishUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static net.trueog.unionsog.UnionsOG.lang;

public class CoordsFrame extends SCFrame {

    private final List<UnionPlayer> allMembers;
    private final Paginator paginator;

    public CoordsFrame(Player viewer, SCFrame parent, Union subject) {

        super(parent, viewer);
        allMembers = VanishUtils.getNonVanished(getViewer(), subject);

        paginator = new Paginator(getSize() - 9, allMembers);

    }

    @Override
    public void createComponents() {

        for (int slot = 0; slot < 9; slot++) {

            if (slot == 2 || slot == 6 || slot == 7)
                continue;
            add(Components.getPanelComponent(slot));

        }

        add(Components.getBackComponent(getParent(), 2, getViewer()));

        add(Components.getPreviousPageComponent(6, this::previousPage, paginator, getViewer()));
        add(Components.getNextPageComponent(7, this::nextPage, paginator, getViewer()));
        int slot = 9;
        for (int i = paginator.getMinIndex(); paginator.isValidIndex(i); i++) {

            UnionPlayer cp = allMembers.get(i);
            Location cpLoc = Objects.requireNonNull(cp.toPlayer()).getLocation();
            int distance = (int) Math.ceil(cpLoc.toVector().distance(getViewer().getLocation().toVector()));

            SCComponent c = new SCComponentImpl(lang("gui.playerdetails.player.title", getViewer(), cp.getName()),
                    Arrays.asList(lang("gui.coords.player.lore.distance", getViewer(), distance),
                            lang("gui.coords.player.lore.coords", getViewer(), cpLoc.getBlockX(), cpLoc.getBlockY(),
                                    cpLoc.getBlockZ()),
                            lang("gui.coords.player.lore.world", getViewer(),
                                    Objects.requireNonNull(cpLoc.getWorld()).getName())),
                    XMaterial.PLAYER_HEAD, slot);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(cp.getUniqueId());
            Components.setOwningPlayer(c.getItem(), offlinePlayer);
            c.setListener(ClickType.LEFT,
                    () -> InventoryDrawer.open(new PlayerDetailsFrame(getViewer(), this, offlinePlayer)));
            c.setLorePermission("unionsog.member.coords");
            add(c);
            slot++;

        }

    }

    private void previousPage() {

        if (paginator.previousPage()) {

            updateFrame();

        }

    }

    private void nextPage() {

        if (paginator.nextPage()) {

            updateFrame();

        }

    }

    private void updateFrame() {

        InventoryDrawer.open(this);

    }

    @Override
    @NotNull
    public String getTitle() {

        return lang("gui.coords.title", getViewer());

    }

    @Override
    public int getSize() {

        return 6 * 9;

    }

}
