package net.trueog.unionsog.ui.frames.staff;

import com.cryptomorin.xseries.XMaterial;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.ui.*;
import net.trueog.unionsog.ui.frames.Components;
import net.trueog.unionsog.ui.frames.RosterFrame;
import net.trueog.unionsog.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static net.trueog.unionsog.UnionsOG.lang;

public class UnionDetailsFrame extends SCFrame {

    private final Union union;

    public UnionDetailsFrame(@Nullable SCFrame parent, @NotNull Player viewer, @NotNull Union union) {

        super(parent, viewer);
        this.union = union;

    }

    @Override
    public void createComponents() {

        for (int slot = 0; slot < 9; slot++) {

            if (slot == 4)
                continue;
            add(Components.getPanelComponent(slot));

        }

        add(Components.getBackComponent(getParent(), 4, getViewer()));
        add(Components.getUnionComponent(this, getViewer(), union, 13, false));

        addRoster();
        addHome();
        // TODO: start - restore addBank() with UnionBankZeroMigration once union bank
        // accounts exist; the bank commands it links to are unregistered until then.
        // addBank();
        // TODO: end
        addDisband();

    }

    private void addDisband() {

        SCComponent disband = new SCComponentImpl(lang("gui.uniondetails.disband.title", getViewer()),
                Collections.singletonList(lang("gui.staffuniondetails.disband.lore", getViewer())), XMaterial.BARRIER,
                34);
        disband.setListener(ClickType.LEFT,
                () -> InventoryController.runSubcommand(getViewer(), "mod disband", false, union.getTag()));
        disband.setConfirmationRequired(ClickType.LEFT);
        disband.setPermission(ClickType.LEFT, "unionsog.mod.disband");
        add(disband);

    }

    private void addHome() {

        List<String> lore = new ArrayList<>();
        lore.add(lang("gui.staffuniondetails.home.lore.teleport", getViewer()));
        lore.add(lang("gui.staffuniondetails.home.lore.set", getViewer()));

        SCComponent home = new SCComponentImpl(lang("gui.uniondetails.home.title", getViewer()), lore,
                Objects.requireNonNull(XMaterial.MAGENTA_BED.parseMaterial()), 30);
        home.setListener(ClickType.LEFT,
                () -> InventoryController.runSubcommand(getViewer(), "mod home tp", false, union.getTag()));
        home.setPermission(ClickType.LEFT, "unionsog.mod.hometp");
        home.setListener(ClickType.RIGHT,
                () -> InventoryController.runSubcommand(getViewer(), "mod home set", false, union.getTag()));
        home.setPermission(ClickType.RIGHT, "unionsog.mod.home");
        home.setConfirmationRequired(ClickType.RIGHT);
        add(home);

    }

    private void addRoster() {

        SCComponent roster = new SCComponentImpl(lang("gui.uniondetails.roster.title", getViewer()),
                Collections.singletonList(lang("gui.staffuniondetails.roster.lore", getViewer())),
                XMaterial.PLAYER_HEAD, 28);

        List<UnionPlayer> members = union.getMembers();
        if (members.size() != 0) {

            OfflinePlayer offlinePlayer = Bukkit
                    .getOfflinePlayer(members.get((int) (Math.random() * members.size())).getUniqueId());
            Components.setOwningPlayer(roster.getItem(), offlinePlayer);

        }

        roster.setListener(ClickType.LEFT, () -> InventoryDrawer.open(new RosterFrame(getViewer(), this, union, true)));
        add(roster);

    }

    private void addBank() {

        List<String> lore = Collections
                .singletonList(lang("gui.uniondetails.bank.balance.lore", getViewer(), union.getBalanceFormatted()));

        SCComponent bank = new SCComponentImpl(lang("gui.uniondetails.bank.title", getViewer()), lore,
                XMaterial.GOLD_INGOT, 40);
        add(bank);

    }

    @Override
    public @NotNull String getTitle() {

        return lang("gui.uniondetails.title", getViewer(), ChatUtils.stripColors(union.getColorTag()), union.getName());

    }

    @Override
    public int getSize() {

        return 6 * 9;

    }

}
