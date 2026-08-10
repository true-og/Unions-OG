package net.trueog.unionsog.ui.frames;

import com.cryptomorin.xseries.XMaterial;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.ui.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;

public class PlayerDetailsFrame extends SCFrame {

    private final UnionsOG plugin = UnionsOG.getInstance();
    private final OfflinePlayer subject;
    private final String subjectName;
    private final Union union;

    public PlayerDetailsFrame(Player viewer, SCFrame parent, OfflinePlayer subject) {

        super(parent, viewer);
        this.subject = subject;
        UnionPlayer cp = plugin.getUnionManager().getCreateUnionPlayer(subject.getUniqueId());
        subjectName = cp.getName();
        union = cp.getUnion();

    }

    @Override
    public void createComponents() {

        for (int slot = 0; slot < 9; slot++) {

            if (slot == 4)
                continue;
            add(Components.getPanelComponent(slot));

        }

        add(Components.getBackComponent(getParent(), 4, getViewer()));
        add(Components.getPlayerComponent(this, getViewer(), subject, 13, false));

        if (!isSameUnion()) {

            return;

        }

        addKick();

    }

    private void addKick() {

        SCComponent kick = new SCComponentImpl(lang("gui.playerdetails.kick.title", getViewer()), null,
                XMaterial.RED_WOOL, 28);
        kick.setListener(ClickType.LEFT,
                () -> InventoryController.runSubcommand(getViewer(), "kick", true, subjectName));
        kick.setConfirmationRequired(ClickType.LEFT);
        kick.setPermission(ClickType.LEFT, "unionsog.member.kick");
        add(kick);

    }

    @Override
    public @NotNull String getTitle() {

        return lang("gui.playerdetails.title", getViewer(), subjectName);

    }

    @Override
    public int getSize() {

        int size = 3;
        if (isSameUnion()) {

            size = 6;

        }

        return size * 9;

    }

    private boolean isSameUnion() {

        return union != null && union.isMember(subject.getUniqueId());

    }

}
