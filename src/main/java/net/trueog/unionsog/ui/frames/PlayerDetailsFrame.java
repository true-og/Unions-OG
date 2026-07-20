package net.trueog.unionsog.ui.frames;

import com.cryptomorin.xseries.XMaterial;
import net.trueog.unionsog.Clan;
import net.trueog.unionsog.ClanPlayer;
import net.trueog.unionsog.RankPermission;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.managers.SettingsManager;
import net.trueog.unionsog.ui.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.CLAN_CONFIRMATION_FOR_DEMOTE;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.CLAN_CONFIRMATION_FOR_PROMOTE;

public class PlayerDetailsFrame extends SCFrame {

    private final UnionsOG plugin = UnionsOG.getInstance();
    private final OfflinePlayer subject;
    private final String subjectName;
    private final Clan clan;

    public PlayerDetailsFrame(Player viewer, SCFrame parent, OfflinePlayer subject) {

        super(parent, viewer);
        this.subject = subject;
        ClanPlayer cp = plugin.getClanManager().getCreateClanPlayer(subject.getUniqueId());
        subjectName = cp.getName();
        clan = cp.getClan();

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

        if (!isSameClan()) {

            return;

        }

        addKick();
        addPromoteDemote();
        addAssignUnassign();
        addTrustUntrust();

    }

    private void addKick() {

        SCComponent kick = new SCComponentImpl(lang("gui.playerdetails.kick.title", getViewer()), null,
                XMaterial.RED_WOOL, 28);
        kick.setListener(ClickType.LEFT,
                () -> InventoryController.runSubcommand(getViewer(), "kick", true, subjectName));
        kick.setConfirmationRequired(ClickType.LEFT);
        kick.setPermission(ClickType.LEFT, RankPermission.KICK);
        add(kick);

    }

    private void addPromoteDemote() {

        SettingsManager settings = plugin.getSettingsManager();
        SCComponent promoteDemote = new SCComponentImpl(lang("gui.playerdetails.promote.demote.title", getViewer()),
                Arrays.asList(lang("gui.playerdetails.promote.lore.left.click", getViewer()),
                        lang("gui.playerdetails.demote.lore.right.click", getViewer())),
                XMaterial.GUNPOWDER, 30);
        promoteDemote.setConfirmationRequired(ClickType.LEFT);
        promoteDemote.setListener(ClickType.LEFT, () -> InventoryController.runSubcommand(getViewer(), "promote",
                !settings.is(CLAN_CONFIRMATION_FOR_PROMOTE), subjectName));
        promoteDemote.setPermission(ClickType.LEFT, "unionsog.leader.promote");
        promoteDemote.setListener(ClickType.RIGHT, () -> InventoryController.runSubcommand(getViewer(), "demote",
                !settings.is(CLAN_CONFIRMATION_FOR_DEMOTE), subjectName));
        promoteDemote.setConfirmationRequired(ClickType.RIGHT);
        add(promoteDemote);
        promoteDemote.setPermission(ClickType.RIGHT, "unionsog.leader.demote");

    }

    private void addAssignUnassign() {

        SCComponentImpl assignUnassign = new SCComponentImpl(
                lang("gui.playerdetails.assign.unassign.title", getViewer()),
                Arrays.asList(lang("gui.playerdetails.assign.lore.left.click", getViewer()),
                        lang("gui.playerdetails.unassign.lore.right.click", getViewer())),
                XMaterial.FEATHER, 32);
        assignUnassign.setConfirmationRequired(ClickType.RIGHT);
        assignUnassign.setListener(ClickType.RIGHT,
                () -> InventoryController.runSubcommand(getViewer(), "rank unassign", true, subjectName));
        assignUnassign.setPermission(ClickType.RIGHT, "unionsog.leader.rank.unassign");
        assignUnassign.setListener(ClickType.LEFT,
                () -> InventoryDrawer.open(new RanksFrame(this, getViewer(), clan, subject)));
        add(assignUnassign);
        assignUnassign.setPermission(ClickType.LEFT, "unionsog.leader.rank.assign");

    }

    private void addTrustUntrust() {

        SCComponent trustUntrust = new SCComponentImpl(lang("gui.playerdetails.trust.untrust.title", getViewer()),
                Arrays.asList(lang("gui.playerdetails.trust.lore.left.click", getViewer()),
                        lang("gui.playerdetails.untrust.lore.right.click", getViewer())),
                XMaterial.CYAN_DYE, 34);
        trustUntrust.setConfirmationRequired(ClickType.LEFT);
        trustUntrust.setListener(ClickType.LEFT,
                () -> InventoryController.runSubcommand(getViewer(), "trust", true, subjectName));
        trustUntrust.setPermission(ClickType.LEFT, "unionsog.leader.settrust");
        trustUntrust.setListener(ClickType.RIGHT,
                () -> InventoryController.runSubcommand(getViewer(), "untrust", true, subjectName));
        trustUntrust.setPermission(ClickType.RIGHT, "unionsog.leader.settrust");
        trustUntrust.setConfirmationRequired(ClickType.RIGHT);
        add(trustUntrust);

    }

    @Override
    public @NotNull String getTitle() {

        return lang("gui.playerdetails.title", getViewer(), subjectName);

    }

    @Override
    public int getSize() {

        int size = 3;
        if (isSameClan()) {

            size = 6;

        }

        return size * 9;

    }

    private boolean isSameClan() {

        return clan != null && clan.isMember(subject.getUniqueId());

    }

}
