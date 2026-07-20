package net.trueog.unionsog.ui.frames;

import com.cryptomorin.xseries.XMaterial;
import net.trueog.unionsog.Clan;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.managers.SettingsManager;
import net.trueog.unionsog.ui.InventoryDrawer;
import net.trueog.unionsog.ui.SCComponent;
import net.trueog.unionsog.ui.SCComponentImpl;
import net.trueog.unionsog.ui.SCFrame;
import net.trueog.unionsog.utils.KDRFormat;
import net.trueog.unionsog.utils.Paginator;
import net.trueog.unionsog.utils.RankingNumberResolver;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.SHOW_UNVERIFIED_ON_LIST;

public class ClanListFrame extends SCFrame {

    private final List<Clan> clans;
    private final Paginator paginator;
    private final RankingNumberResolver<Clan, BigDecimal> rankingResolver;

    public ClanListFrame(SCFrame parent, Player viewer) {

        super(parent, viewer);
        UnionsOG plugin = UnionsOG.getInstance();
        SettingsManager sm = plugin.getSettingsManager();
        clans = plugin.getClanManager().getClans().stream()
                .filter(clan -> clan.isVerified() || sm.is(SHOW_UNVERIFIED_ON_LIST)).collect(Collectors.toList());
        paginator = new Paginator(getSize() - 9, clans);
        plugin.getClanManager().sortClansByKDR(clans);

        rankingResolver = new RankingNumberResolver<>(clans, c -> KDRFormat.toBigDecimal(c.getTotalKDR()), false,
                sm.getRankingType());

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

            Clan clan = clans.get(i);
            ItemStack banner = clan.getBanner() != null ? clan.getBanner() : XMaterial.BLACK_BANNER.parseItem();
            SCComponent c = new SCComponentImpl(
                    lang("gui.clanlist.clan.title", getViewer(), clan.getColorTag(), clan.getName()),
                    Arrays.asList(
                            lang("gui.clanlist.clan.lore.position", getViewer(),
                                    rankingResolver.getRankingNumber(clan)),
                            lang("gui.clanlist.clan.lore.kdr", getViewer(), KDRFormat.format(clan.getTotalKDR())),
                            lang("gui.clanlist.clan.lore.members", getViewer(), clan.getMembers().size())),
                    banner, slot);
            c.setLorePermission("unionsog.anyone.list");
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

        return lang("gui.clanlist.title", getViewer(), clans.size());

    }

    @Override
    public int getSize() {

        return 6 * 9;

    }

}
