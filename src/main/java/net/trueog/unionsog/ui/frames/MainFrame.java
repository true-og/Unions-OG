package net.trueog.unionsog.ui.frames;

import com.cryptomorin.xseries.XMaterial;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.ui.*;
import net.trueog.unionsog.ui.frames.staff.StaffFrame;
import net.trueog.unionsog.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;

public class MainFrame extends SCFrame {

    private final UnionsOG plugin = UnionsOG.getInstance();
    private int nextSlot;

    public MainFrame(Player viewer) {

        super(null, viewer);

    }

    @Override
    public void createComponents() {

        nextSlot = 0;
        add(Components.getPlayerComponent(this, getViewer(), getViewer(), nextSlot++, false));
        add(Components.getClanComponent(this, getViewer(),
                plugin.getClanManager().getCreateClanPlayer(getViewer().getUniqueId()).getClan(), nextSlot++, true));
        addUnionBanking();
        addClanList();
        addLeaderboard();
        addResetKdr();
        addStaff();
        addLanguageSelector();
        addOtherCommands();

    }

    private void addUnionBanking() {

        SCComponent unionBanking = new SCComponentImpl(ChatUtils.parseColors("&6Union Banking"),
                Arrays.asList(ChatUtils.parseColors("&7This feature is not finished yet."),
                        ChatUtils.parseColors("&eUnion Banking will be available soon.")),
                XMaterial.DIAMOND_PICKAXE, nextSlot++);
        unionBanking.setListener(ClickType.LEFT, () -> getViewer().sendMessage(
                ChatUtils.parseColors("&eUnion Banking is not finished yet and will be available soon.")));
        add(unionBanking);

    }

    private void addOtherCommands() {

        SCComponent otherCommands = new SCComponentImpl(lang("gui.main.other.commands.title", getViewer()),
                Collections.singletonList(lang("gui.main.other.commands.lore", getViewer())), XMaterial.BOOK, nextSlot++);
        otherCommands.setListener(ClickType.LEFT, () -> InventoryController.runSubcommand(getViewer(), "help", false));
        add(otherCommands);

    }

    private void addStaff() {

        if (plugin.getPermissionsManager().has(getViewer(), "unionsog.mod.staffgui")) {

            SCComponent staff = new SCComponentImpl.Builder(XMaterial.COMMAND_BLOCK).withSlot(nextSlot++)
                    .withDisplayName(lang("gui.main.staff.title", getViewer()))
                    .withLore(Collections.singletonList(lang("gui.main.staff.lore", getViewer()))).build();
            staff.setPermission(ClickType.LEFT, "unionsog.mod.staffgui");
            staff.setListener(ClickType.LEFT, () -> InventoryDrawer.open(new StaffFrame(this, getViewer())));
            add(staff);

        }

    }

    private void addLeaderboard() {

        SCComponent leaderboard = new SCComponentImpl(lang("gui.main.leaderboard.title", getViewer()),
                Collections.singletonList(lang("gui.main.leaderboard.lore", getViewer())), XMaterial.PAINTING,
                nextSlot++);
        leaderboard.setListener(ClickType.LEFT, () -> InventoryDrawer.open(new LeaderboardFrame(getViewer(), this)));
        leaderboard.setPermission(ClickType.LEFT, "unionsog.anyone.leaderboard");
        add(leaderboard);

    }

    private void addClanList() {

        SCComponent clanList = new SCComponentImpl(lang("gui.main.clan.list.title", getViewer()),
                Collections.singletonList(lang("gui.main.clan.list.lore", getViewer())), XMaterial.PURPLE_BANNER,
                nextSlot++);
        clanList.setListener(ClickType.LEFT, () -> InventoryDrawer.open(new ClanListFrame(this, getViewer())));
        clanList.setPermission(ClickType.LEFT, "unionsog.anyone.list");
        add(clanList);

    }

    private void addLanguageSelector() {

        if (plugin.getSettingsManager().is(LANGUAGE_SELECTOR)) {

            SCComponent language = new SCComponentImpl.Builder(XMaterial.MAP)
                    .withDisplayName(lang("gui.main.languageselector.title", getViewer())).withSlot(nextSlot++)
                    .withLore(Arrays.asList(lang("gui.main.languageselector.lore.left.click", getViewer()),
                            lang("gui.main.languageselector.lore.right.click", getViewer())))
                    .build();
            language.setListener(ClickType.LEFT,
                    () -> InventoryDrawer.open(new LanguageSelectorFrame(this, getViewer())));
            language.setListener(ClickType.RIGHT, () -> {

                getViewer().sendMessage(
                        lang("click.to.help.translating", getViewer(), "https://crowdin.com/project/simpleclans"));
                getViewer().closeInventory();

            });
            add(language);

        }

    }

    public void addResetKdr() {

        if (!plugin.getPermissionsManager().has(getViewer(), "unionsog.vip.resetkdr")) {

            return;

        }

        List<String> resetKdrLore = Collections.singletonList(lang("gui.main.reset.kdr.lore", getViewer()));

        SCComponent resetKdr = new SCComponentImpl(lang("gui.main.reset.kdr.title", getViewer()), resetKdrLore,
                XMaterial.ANVIL, nextSlot++);
        resetKdr.setListener(ClickType.LEFT, () -> InventoryController.runSubcommand(getViewer(), "resetkdr", false));
        resetKdr.setConfirmationRequired(ClickType.LEFT);
        resetKdr.setPermission(ClickType.LEFT, "unionsog.vip.resetkdr");

        add(resetKdr);

    }

    @Override
    public @NotNull String getTitle() {

        return lang("gui.main.title", getViewer(), plugin.getSettingsManager().getColored(SERVER_NAME));

    }

    @Override
    public int getSize() {

        return 9;

    }

}
