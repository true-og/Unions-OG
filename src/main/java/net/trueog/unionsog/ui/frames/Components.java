package net.trueog.unionsog.ui.frames;

import com.cryptomorin.xseries.XMaterial;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.Helper;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.ui.*;
import net.trueog.unionsog.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;

public class Components {

    private Components() {

    }

    public static SCComponent getPlayerComponent(SCFrame frame, Player viewer, OfflinePlayer subject, int slot,
            boolean openDetails)
    {

        UnionPlayer cp = UnionsOG.getInstance().getUnionManager().getCreateUnionPlayer(subject.getUniqueId());

        return getPlayerComponent(frame, viewer, cp, slot, openDetails);

    }

    public static SCComponent getPlayerComponent(SCFrame frame, Player viewer, UnionPlayer cp, int slot,
            boolean openDetails)
    {

        String status = getPlayerStatus(viewer, cp);
        SCComponent c = new SCComponentImpl(lang("gui.playerdetails.player.title", viewer, cp.getName()), Arrays.asList(
                cp.getUnion() == null ? lang("gui.playerdetails.player.lore.nounion", viewer)
                        : lang("gui.playerdetails.player.lore.union", viewer, cp.getUnion().getColorTag(),
                                cp.getUnion().getName()),
                lang("gui.playerdetails.player.lore.status", viewer, status),
                lang("gui.playerdetails.player.lore.kdr", viewer, new DecimalFormat("#.#").format(cp.getKDR())),
                lang("gui.playerdetails.player.lore.kill.totals", viewer, cp.getRivalKills(), cp.getNeutralKills(),
                        cp.getCivilianKills()),
                lang("gui.playerdetails.player.lore.deaths", viewer, cp.getDeaths()),
                lang("gui.playerdetails.player.lore.join.date", viewer, cp.getJoinDateString()),
                lang("gui.playerdetails.player.lore.last.seen", viewer, cp.getLastSeenString(viewer)),
                lang("gui.playerdetails.player.lore.past.unions", viewer,
                        cp.getPastUnionsString(lang("gui.playerdetails.player.lore.past.unions.separator", viewer))),
                lang("gui.playerdetails.player.lore.inactive", viewer, cp.getInactiveDays())), XMaterial.PLAYER_HEAD,
                slot);
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(cp.getUniqueId());
        setOwningPlayer(c.getItem(), offlinePlayer);
        if (viewer.getUniqueId().equals(cp.getUniqueId())) {

            c.setLorePermission("unionsog.member.lookup");

        } else {

            c.setLorePermission("unionsog.anyone.lookup");

        }

        if (openDetails) {

            c.setListener(ClickType.LEFT,
                    () -> InventoryDrawer.open(new PlayerDetailsFrame(viewer, frame, offlinePlayer)));

        }

        return c;

    }

    @NotNull
    private static String getPlayerStatus(Player viewer, UnionPlayer cp) {

        if (cp.getUnion() == null) {

            return lang("free.agent", viewer);

        }

        if (cp.isTrusted()) {

            return lang("trusted", viewer);

        }

        return lang("untrusted", viewer);

    }

    public static SCComponent getUnionComponent(@NotNull SCFrame frame, @NotNull Player viewer, @Nullable Union union,
            int slot, boolean openDetails)
    {

        UnionsOG pl = UnionsOG.getInstance();
        String name;
        List<String> lore;
        if (union != null) {

            name = lang("gui.uniondetails.union.title", viewer, union.getColorTag(), union.getName());
            lore = Arrays.asList(
                    lang("gui.uniondetails.union.lore.description", viewer,
                            union.getDescription() != null && !union.getDescription().isEmpty() ? union.getDescription()
                                    : lang("no.description", viewer)),
                    lang("gui.uniondetails.union.lore.status", viewer, Helper.getFormattedUnionStatus(union, viewer)),
                    lang("gui.uniondetails.union.lore.online.members", viewer,
                            VanishUtils.getNonVanished(viewer, union).size(), union.getMembers().size()),
                    lang("gui.uniondetails.union.lore.kdr", viewer, KDRFormat.format(union.getTotalKDR())),
                    lang("gui.uniondetails.union.lore.kill.totals", viewer, union.getTotalRival(),
                            union.getTotalNeutral(), union.getTotalCivilian()),
                    lang("gui.uniondetails.union.lore.deaths", viewer, union.getTotalDeaths()),
                    lang("gui.uniondetails.union.lore.allies", viewer,
                            union.getAllies().isEmpty() ? lang("none", viewer)
                                    : union.getAllyString(lang("gui.uniondetails.union.lore.allies.separator", viewer),
                                            viewer)),
                    lang("gui.uniondetails.union.lore.rivals", viewer,
                            union.getRivals().isEmpty() ? lang("none", viewer)
                                    : union.getRivalString(lang("gui.uniondetails.union.lore.rivals.separator", viewer),
                                            viewer)),
                    lang("gui.uniondetails.union.lore.founded", viewer, union.getFoundedString()),
                    lang("gui.uniondetails.union.lore.inactive", viewer, union.getInactiveDays()));

        } else {

            name = lang("gui.uniondetails.free.agent.title", viewer);
            double price = pl.getSettingsManager().is(ECONOMY_PURCHASE_UNION_CREATE)
                    ? pl.getSettingsManager().getDouble(ECONOMY_CREATION_PRICE)
                    : 0;
            lore = new ArrayList<>();
            if (price != 0) {

                lore.add(lang("gui.uniondetails.free.agent.create.union.price.lore", frame.getViewer(),
                        CurrencyFormat.format(price, viewer)));

            }

            lore.add(lang("gui.uniondetails.free.agent.create.union.lore", frame.getViewer()));

        }

        ItemStack item;
        if (union != null && union.getBanner() != null) {

            item = union.getBanner();

        } else {

            item = XMaterial.GREEN_BANNER.parseItem();

        }

        SCComponent c = new SCComponentImpl.Builder(item).withLore(lore).withDisplayName(name).withSlot(slot).build();
        if (openDetails && union != null) {

            c.setListener(ClickType.LEFT, () -> InventoryDrawer.open(new UnionDetailsFrame(frame, viewer, union)));

        }

        if (union == null) {

            c.setPermission(ClickType.LEFT, "unionsog.member.create");
            c.setListener(ClickType.LEFT, () -> InventoryController.runSubcommand(viewer, "create", false));

        }

        if (union != null && union.isMember(viewer)) {

            c.setLorePermission("unionsog.member.profile");

        } else {

            c.setLorePermission("unionsog.anyone.profile");

        }

        return c;

    }

    public static SCComponent getBackComponent(@Nullable SCFrame parent, int slot, Player viewer) {

        SCComponent back = new SCComponentImpl(lang("gui.back.title", viewer), null, XMaterial.ARROW, slot);
        back.setListener(ClickType.LEFT, () -> InventoryDrawer.open(parent));
        return back;

    }

    public static SCComponent getPanelComponent(int slot) {

        return new SCComponentImpl(" ", null, XMaterial.GRAY_STAINED_GLASS_PANE, slot);

    }

    public static @NotNull SCComponent getPreviousPageComponent(int slot, @Nullable Runnable listener,
            @NotNull Paginator paginator, @NotNull Player viewer)
    {

        if (!paginator.hasPreviousPage()) {

            return getPanelComponent(slot);

        }

        SCComponent c = new SCComponentImpl(lang("gui.previous.page.title", viewer), null, XMaterial.STONE_BUTTON,
                slot);
        setOneTimeUseListener(c, listener);
        return c;

    }

    public static @NotNull SCComponent getNextPageComponent(int slot, @Nullable Runnable listener,
            @NotNull Paginator paginator, @NotNull Player viewer)
    {

        if (!paginator.hasNextPage()) {

            return getPanelComponent(slot);

        }

        SCComponent c = new SCComponentImpl(lang("gui.next.page.title", viewer), null, XMaterial.STONE_BUTTON, slot);
        setOneTimeUseListener(c, listener);
        return c;

    }

    private static void setOneTimeUseListener(SCComponent c, @Nullable Runnable listener) {

        c.setListener(ClickType.LEFT, () -> {

            if (listener != null) {

                listener.run();

            }

            c.setListener(ClickType.LEFT, null);

        });

    }

    @SuppressWarnings("deprecation")
    public static void setOwningPlayer(@NotNull ItemStack item, @NotNull OfflinePlayer player) {

        SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
        if (itemMeta == null || player.getName() == null) {

            return;

        }

        try {

            itemMeta.setOwningPlayer(player);

        } catch (NoSuchMethodError e) {

            itemMeta.setOwner(player.getName());

        }

        item.setItemMeta(itemMeta);

    }

}
