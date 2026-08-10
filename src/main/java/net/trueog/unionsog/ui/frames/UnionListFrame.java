package net.trueog.unionsog.ui.frames;

import com.cryptomorin.xseries.XMaterial;
import net.trueog.unionsog.Union;
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

import static net.trueog.unionsog.UnionsOG.lang;

public class UnionListFrame extends SCFrame {

    private final List<Union> unions;
    private final Paginator paginator;
    private final RankingNumberResolver<Union, BigDecimal> rankingResolver;

    public UnionListFrame(SCFrame parent, Player viewer) {

        super(parent, viewer);
        UnionsOG plugin = UnionsOG.getInstance();
        SettingsManager sm = plugin.getSettingsManager();
        unions = plugin.getUnionManager().getUnions();
        paginator = new Paginator(getSize() - 9, unions);
        plugin.getUnionManager().sortUnionsByKDR(unions);

        rankingResolver = new RankingNumberResolver<>(unions, c -> KDRFormat.toBigDecimal(c.getTotalKDR()), false,
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

            Union union = unions.get(i);
            ItemStack banner = union.getBanner() != null ? union.getBanner() : XMaterial.BLACK_BANNER.parseItem();
            SCComponent c = new SCComponentImpl(
                    lang("gui.unionlist.union.title", getViewer(), union.getColorTag(), union.getName()),
                    Arrays.asList(
                            lang("gui.unionlist.union.lore.position", getViewer(),
                                    rankingResolver.getRankingNumber(union)),
                            lang("gui.unionlist.union.lore.kdr", getViewer(), KDRFormat.format(union.getTotalKDR())),
                            lang("gui.unionlist.union.lore.members", getViewer(), union.getMembers().size())),
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

        return lang("gui.unionlist.title", getViewer(), unions.size());

    }

    @Override
    public int getSize() {

        return 6 * 9;

    }

}
