package net.trueog.unionsog.ui.frames;

import com.cryptomorin.xseries.XMaterial;
import net.trueog.unionsog.ui.SCComponent;
import net.trueog.unionsog.ui.SCComponentImpl;
import net.trueog.unionsog.ui.SCFrame;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static net.trueog.unionsog.UnionsOG.lang;

public class WarningFrame extends SCFrame {

    private final String permission;

    public WarningFrame(@NotNull SCFrame parent, @NotNull Player viewer, @NotNull String permission) {

        super(parent, viewer);
        this.permission = permission;

    }

    @Override
    public void createComponents() {

        for (int slot = 0; slot < 9; slot++) {

            if (slot == 4)
                continue;
            add(Components.getPanelComponent(slot));

        }

        add(Components.getBackComponent(getParent(), 4, getViewer()));

        addNoPermissionComponent(permission, 22);

    }

    private void addNoPermissionComponent(String permission, int slot) {

        List<String> lore = Collections
                .singletonList(lang("gui.warning.no.permission.plugin.lore", getViewer(), permission));

        SCComponent perm = new SCComponentImpl(lang("gui.warning.no.permission.title", getViewer()), lore,
                XMaterial.BARRIER, slot);
        add(perm);

    }

    @Override
    public @NotNull String getTitle() {

        return lang("gui.warning.title", getViewer());

    }

    @Override
    public int getSize() {

        return 6 * 9;

    }

}
