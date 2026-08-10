package net.trueog.unionsog.ui.frames.staff;

import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.ui.InventoryController;
import net.trueog.unionsog.ui.InventoryDrawer;
import net.trueog.unionsog.ui.SCComponent;
import net.trueog.unionsog.ui.SCFrame;
import net.trueog.unionsog.ui.frames.Components;
import net.trueog.unionsog.utils.Paginator;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.trueog.unionsog.UnionsOG.lang;

public class UnionListFrame extends SCFrame {

    private final Type type;
    private final @Nullable OfflinePlayer toPlace;
    private List<Union> unions;
    private final Paginator paginator;

    public UnionListFrame(@Nullable SCFrame parent, @NotNull Player viewer, @NotNull Type type,
            @Nullable OfflinePlayer toPlace)
    {

        super(parent, viewer);
        this.type = type;
        this.toPlace = toPlace;
        UnionsOG plugin = UnionsOG.getInstance();
        unions = plugin.getUnionManager().getUnions();
        paginator = new Paginator(getSize() - 9, unions);
        plugin.getUnionManager().sortUnionsByName(unions, true);

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

            Union union = unions.get(i);
            SCComponent c = Components.getUnionComponent(this, getViewer(), union, slot, false);
            if (type != Type.PLACE) {

                c.setListener(ClickType.LEFT,
                        () -> InventoryDrawer.open(new UnionDetailsFrame(this, getViewer(), union)));

            } else {

                if (toPlace != null) {

                    c.setListener(ClickType.LEFT, () -> InventoryController.runSubcommand(getViewer(), "mod place",
                            false, toPlace.getName(), union.getTag()));
                    c.setConfirmationRequired(ClickType.LEFT);
                    c.setPermission(ClickType.LEFT, "unionsog.mod.place");

                }

            }

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
    public @NotNull String getTitle() {

        if (type == Type.PLACE && toPlace != null) {

            return lang("gui.staff.unionlist.toplace.title", getViewer(), toPlace.getName());

        }

        return lang("gui.unionlist.title", getViewer(), unions.size());

    }

    @Override
    public int getSize() {

        return 6 * 9;

    }

    public enum Type {
        ALL, PLACE
    }

}
