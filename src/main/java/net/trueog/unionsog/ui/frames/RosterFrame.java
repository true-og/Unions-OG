package net.trueog.unionsog.ui.frames;

import com.cryptomorin.xseries.XMaterial;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.ui.InventoryDrawer;
import net.trueog.unionsog.ui.SCComponent;
import net.trueog.unionsog.ui.SCComponentImpl;
import net.trueog.unionsog.ui.SCFrame;
import net.trueog.unionsog.ui.frames.staff.PlayerDetailsFrame;
import net.trueog.unionsog.utils.ChatUtils;
import net.trueog.unionsog.utils.Paginator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static net.trueog.unionsog.UnionsOG.lang;

public class RosterFrame extends SCFrame {

    private final Union subject;
    private final boolean staff;
    private final List<UnionPlayer> allMembers;
    private final Paginator paginator;

    public RosterFrame(Player viewer, SCFrame parent, Union subject) {

        this(viewer, parent, subject, false);

    }

    public RosterFrame(Player viewer, SCFrame parent, Union subject, boolean staff) {

        super(parent, viewer);
        this.subject = subject;
        this.staff = staff;

        allMembers = subject.getMembers();
        paginator = new Paginator(getSize() - 9, allMembers.size());

    }

    @Override
    public void createComponents() {

        for (int slot = 0; slot < 9; slot++) {

            if (slot == 2 || slot == 4 || slot == 6 || slot == 7)
                continue;
            add(Components.getPanelComponent(slot));

        }

        add(Components.getBackComponent(getParent(), 2, getViewer()));

        if (!staff) {

            SCComponent invite = new SCComponentImpl(lang("gui.roster.invite.title", getViewer()),
                    Collections.singletonList(lang("gui.roster.invite.lore", getViewer())), XMaterial.LIME_WOOL, 4);
            invite.setListener(ClickType.LEFT, () -> InventoryDrawer.open(new InviteFrame(this, getViewer())));
            invite.setPermission(ClickType.LEFT, "unionsog.member.invite");
            add(invite);

        } else {

            add(Components.getPanelComponent(4));

        }

        add(Components.getPreviousPageComponent(6, this::previousPage, paginator, getViewer()));
        add(Components.getNextPageComponent(7, this::nextPage, paginator, getViewer()));

        int slot = 9;
        for (int i = paginator.getMinIndex(); paginator.isValidIndex(i); i++) {

            UnionPlayer cp = allMembers.get(i);
            SCComponent playerComponent = Components.getPlayerComponent(this, getViewer(), cp, slot, true);
            if (staff) {

                playerComponent.setListener(ClickType.LEFT, () -> InventoryDrawer
                        .open(new PlayerDetailsFrame(getViewer(), this, Bukkit.getOfflinePlayer(cp.getUniqueId()))));

            }

            add(playerComponent);
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

        return lang("gui.roster.title", getViewer(), ChatUtils.stripColors(subject.getColorTag()));

    }

    @Override
    public int getSize() {

        return 6 * 9;

    }

}
