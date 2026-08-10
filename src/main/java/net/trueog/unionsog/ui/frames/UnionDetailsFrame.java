package net.trueog.unionsog.ui.frames;

import com.cryptomorin.xseries.XMaterial;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.UnionPlayer.Channel;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.managers.SettingsManager;
import net.trueog.unionsog.ui.*;
import net.trueog.unionsog.utils.ChatUtils;
import net.trueog.unionsog.utils.CurrencyFormat;
import net.trueog.unionsog.utils.VanishUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;

public class UnionDetailsFrame extends SCFrame {

    private final Union union;
    private final UnionPlayer cp;
    private final UnionsOG plugin;
    private final SettingsManager settings;

    public UnionDetailsFrame(@Nullable SCFrame parent, @NotNull Player viewer, @NotNull Union union) {

        super(parent, viewer);
        this.union = union;
        plugin = UnionsOG.getInstance();
        settings = plugin.getSettingsManager();
        cp = plugin.getUnionManager().getUnionPlayer(getViewer());

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
        addCoords();
        addAllies();
        addRivals();
        addHome();
        addRegroup();
        addFf();
        // TODO: start - restore addBank() with UnionBankZeroMigration once union bank
        // accounts exist; the bank commands it links to are unregistered until then.
        // addBank();
        // TODO: end
        addResign();
        addDisband();
        addChat();

    }

    private void addChat() {

        Channel cpChannel = cp.getChannel();
        boolean unionEnabled = Channel.UNION.equals(cpChannel);
        boolean allyEnabled = Channel.ALLY.equals(cpChannel);

        SCComponent chat = createChatComponent(unionEnabled, allyEnabled);
        chat.setListener(ClickType.LEFT, () -> {

            if (unionEnabled) {

                cp.setChannel(Channel.NONE);

            } else {

                cp.setChannel(Channel.UNION);

            }

            updateFrame();

        });
        chat.setPermission(ClickType.LEFT, "unionsog.member.chat");
        chat.setListener(ClickType.RIGHT, () -> {

            if (allyEnabled) {

                cp.setChannel(Channel.NONE);

            } else {

                cp.setChannel(Channel.ALLY);

            }

            updateFrame();

        });
        chat.setPermission(ClickType.RIGHT, "unionsog.member.ally");
        add(chat);

    }

    @NotNull
    private SCComponent createChatComponent(boolean unionEnabled, boolean allyEnabled) {

        String joined = lang("chat.joined", getViewer());
        String notJoined = lang("chat.not.joined", getViewer());

        String unionStatus = unionEnabled ? joined : notJoined;
        String allyStatus = allyEnabled ? joined : notJoined;

        String chatCommand = settings.is(UNIONCHAT_TAG_BASED) ? union.getTag()
                : settings.getString(COMMANDS_UNION_CHAT);
        String joinArg = lang("join", getViewer());
        String leaveArg = lang("leave", getViewer());
        return new SCComponentImpl(lang("gui.uniondetails.chat.title", getViewer()),
                Arrays.asList(lang("gui.uniondetails.chat.union.chat.lore", getViewer(), chatCommand),
                        lang("gui.uniondetails.chat.union.join.leave.lore", getViewer(), chatCommand, joinArg,
                                leaveArg),
                        lang("gui.uniondetails.chat.ally.chat.lore", getViewer(), settings.getString(COMMANDS_ALLY)),
                        lang("gui.uniondetails.chat.ally.join.leave.lore", getViewer(),
                                settings.getString(COMMANDS_ALLY), joinArg, leaveArg),
                        lang("gui.uniondetails.chat.union.status.lore", getViewer(), unionStatus),
                        lang("gui.uniondetails.chat.ally.status.lore", getViewer(), allyStatus),
                        lang("gui.uniondetails.chat.union.toggle.lore", getViewer()),
                        lang("gui.uniondetails.chat.ally.toggle.lore", getViewer())),
                XMaterial.KNOWLEDGE_BOOK, 43);

    }

    private void addDisband() {

        SCComponent disband = new SCComponentImpl(lang("gui.uniondetails.disband.title", getViewer()),
                Collections.singletonList(lang("gui.uniondetails.disband.lore", getViewer())), XMaterial.BARRIER, 50);
        disband.setListener(ClickType.DROP, () -> InventoryController.runSubcommand(getViewer(), "disband", false));
        disband.setPermission(ClickType.DROP, "unionsog.member.disband");
        disband.setConfirmationRequired(ClickType.LEFT);
        add(disband);

    }

    private void addResign() {

        SCComponent resign = new SCComponentImpl(lang("gui.uniondetails.resign.title", getViewer()),
                Collections.singletonList(lang("gui.uniondetails.resign.lore", getViewer())), XMaterial.IRON_DOOR, 48);
        resign.setListener(ClickType.LEFT, () -> InventoryController.runSubcommand(getViewer(), "resign", false));
        resign.setConfirmationRequired(ClickType.LEFT);
        resign.setPermission(ClickType.LEFT, "unionsog.member.resign");
        add(resign);

    }

    private void addBank() {

        String withdrawStatus = union.isAllowWithdraw() ? lang("allowed", getViewer()) : lang("blocked", getViewer());
        String depositStatus = union.isAllowDeposit() ? lang("allowed", getViewer()) : lang("blocked", getViewer());
        SCComponent bank = new SCComponentImpl(lang("gui.uniondetails.bank.title", getViewer()),
                Arrays.asList(lang("gui.uniondetails.bank.balance.lore", getViewer(), union.getBalanceFormatted()),
                        lang("gui.uniondetails.bank.withdraw.status.lore", getViewer(), withdrawStatus),
                        lang("gui.uniondetails.bank.deposit.status.lore", getViewer(), depositStatus),
                        lang("gui.uniondetails.bank.withdraw.toggle.lore", getViewer()),
                        lang("gui.uniondetails.bank.deposit.toggle.lore", getViewer())),
                XMaterial.GOLD_INGOT, 34);
        bank.setLorePermission("unionsog.member.bank");
        bank.setListener(ClickType.DROP, () -> InventoryController.runSubcommand(getViewer(), "toggle withdraw", true));
        bank.setConfirmationRequired(ClickType.DROP);
        bank.setPermission(ClickType.DROP, "unionsog.member.withdraw-toggle");
        bank.setListener(ClickType.RIGHT, () -> InventoryController.runSubcommand(getViewer(), "toggle deposit", true));
        bank.setPermission(ClickType.RIGHT, "unionsog.member.deposit-toggle");

        add(bank);

    }

    private void addFf() {

        String personalFf = cp.isFriendlyFire() ? lang("allowed", getViewer()) : lang("auto", getViewer());
        String unionFf = union.isFriendlyFire() ? lang("allowed", getViewer()) : lang("blocked", getViewer());
        SCComponent ff = new SCComponentImpl(lang("gui.uniondetails.ff.title", getViewer()),
                Arrays.asList(lang("gui.uniondetails.ff.personal.lore", getViewer(), personalFf),
                        lang("gui.uniondetails.ff.union.lore", getViewer(), unionFf),
                        lang("gui.uniondetails.ff.personal.toggle.lore", getViewer()),
                        lang("gui.uniondetails.ff.union.toggle.lore", getViewer())),
                XMaterial.GOLDEN_SWORD, 32);

        ff.setListener(ClickType.LEFT, this::togglePersonalFf);
        ff.setPermission(ClickType.LEFT, "unionsog.member.ff");
        ff.setListener(ClickType.RIGHT, this::toggleUnionFf);
        ff.setPermission(ClickType.RIGHT, "unionsog.member.union-ff");
        add(ff);

    }

    private void toggleUnionFf() {

        String arg;
        if (union.isFriendlyFire()) {

            arg = "block";

        } else {

            arg = "allow";

        }

        InventoryController.runSubcommand(getViewer(), "unionff", true, arg);

    }

    private void togglePersonalFf() {

        String arg;
        if (cp.isFriendlyFire()) {

            arg = "auto";

        } else {

            arg = "allow";

        }

        InventoryController.runSubcommand(getViewer(), "ff", true, arg);

    }

    private void addRegroup() {

        double price = 0;
        if (settings.is(ECONOMY_PURCHASE_HOME_REGROUP)) {

            price = settings.getDouble(ECONOMY_REGROUP_PRICE);
            if (!settings.is(ECONOMY_UNIQUE_TAX_ON_REGROUP)) {

                price = price * VanishUtils.getNonVanished(getViewer(), union).size();

            }

        }

        List<String> lore = new ArrayList<>();
        if (price != 0)
            lore.add(lang("gui.uniondetails.regroup.lore.price", getViewer(),
                    CurrencyFormat.format(price, getViewer())));
        lore.add(lang("gui.uniondetails.regroup.lore.home", getViewer()));
        lore.add(lang("gui.uniondetails.regroup.lore.me", getViewer()));

        SCComponent regroup = new SCComponentImpl(lang("gui.uniondetails.regroup.title", getViewer()), lore,
                XMaterial.BEACON, 30);
        regroup.setListener(ClickType.LEFT,
                () -> InventoryController.runSubcommand(getViewer(), "regroup home", false));
        regroup.setConfirmationRequired(ClickType.LEFT);
        regroup.setPermission(ClickType.LEFT, "unionsog.member.regroup.home");
        regroup.setListener(ClickType.RIGHT, () -> InventoryController.runSubcommand(getViewer(), "regroup me", false));
        regroup.setConfirmationRequired(ClickType.RIGHT);
        regroup.setPermission(ClickType.RIGHT, "unionsog.member.regroup.me");
        add(regroup);

    }

    private void addHome() {

        double homePrice = settings.is(ECONOMY_PURCHASE_HOME_TELEPORT) ? settings.getDouble(ECONOMY_HOME_TELEPORT_PRICE)
                : 0;
        double setPrice = settings.is(ECONOMY_PURCHASE_HOME_TELEPORT_SET)
                ? settings.getDouble(ECONOMY_HOME_TELEPORT_SET_PRICE)
                : 0;

        List<String> lore = new ArrayList<>();
        if (homePrice != 0)
            lore.add(lang("gui.uniondetails.home.lore.teleport.price", getViewer(),
                    CurrencyFormat.format(homePrice, getViewer())));
        lore.add(lang("gui.uniondetails.home.lore.teleport", getViewer()));
        if (setPrice != 0)
            lore.add(lang("gui.uniondetails.home.lore.set.price", getViewer(),
                    CurrencyFormat.format(setPrice, getViewer())));
        lore.add(lang("gui.uniondetails.home.lore.set", getViewer()));
        lore.add(lang("gui.uniondetails.home.lore.clear", getViewer()));

        SCComponent home = new SCComponentImpl(lang("gui.uniondetails.home.title", getViewer()), lore,
                Objects.requireNonNull(XMaterial.MAGENTA_BED.parseMaterial()), 28);
        home.setListener(ClickType.LEFT, () -> InventoryController.runSubcommand(getViewer(), "home", false));
        home.setPermission(ClickType.LEFT, "unionsog.member.home");
        home.setListener(ClickType.RIGHT, () -> InventoryController.runSubcommand(getViewer(), "home set", false));
        home.setPermission(ClickType.RIGHT, "unionsog.member.home-set");
        home.setConfirmationRequired(ClickType.RIGHT);
        home.setListener(ClickType.DROP, () -> InventoryController.runSubcommand(getViewer(), "home clear", false));
        home.setPermission(ClickType.DROP, "unionsog.member.home-set");
        home.setConfirmationRequired(ClickType.DROP);
        add(home);

    }

    private void addRoster() {

        SCComponent roster = new SCComponentImpl(lang("gui.uniondetails.roster.title", getViewer()),
                Collections.singletonList(lang("gui.uniondetails.roster.lore", getViewer())), XMaterial.PLAYER_HEAD,
                19);
        List<UnionPlayer> members = union.getMembers();
        if (members.size() != 0) {

            OfflinePlayer offlinePlayer = Bukkit
                    .getOfflinePlayer(members.get((int) (Math.random() * members.size())).getUniqueId());
            Components.setOwningPlayer(roster.getItem(), offlinePlayer);

        }

        roster.setListener(ClickType.LEFT, () -> InventoryDrawer.open(new RosterFrame(getViewer(), this, union)));
        roster.setPermission(ClickType.LEFT, "unionsog.member.roster");
        add(roster);

    }

    private void addCoords() {

        SCComponent coords = new SCComponentImpl(lang("gui.uniondetails.coords.title", getViewer()),
                Collections.singletonList(lang("gui.uniondetails.coords.lore", getViewer())), XMaterial.COMPASS, 21);
        coords.setListener(ClickType.LEFT, () -> InventoryDrawer.open(new CoordsFrame(getViewer(), this, union)));
        coords.setPermission(ClickType.LEFT, "unionsog.member.coords");
        add(coords);

    }

    private void addAllies() {

        SCComponent allies = new SCComponentImpl(lang("gui.uniondetails.allies.title", getViewer()),
                Collections.singletonList(lang("gui.uniondetails.allies.lore", getViewer())), XMaterial.CYAN_BANNER,
                23);
        allies.setListener(ClickType.LEFT, () -> InventoryDrawer.open(new AlliesFrame(getViewer(), this, union)));
        allies.setPermission(ClickType.LEFT, "unionsog.anyone.alliances");
        add(allies);

    }

    private void addRivals() {

        SCComponent rivals = new SCComponentImpl(lang("gui.uniondetails.rivals.title", getViewer()),
                Collections.singletonList(lang("gui.uniondetails.rivals.lore", getViewer())), XMaterial.RED_BANNER, 25);
        rivals.setListener(ClickType.LEFT, () -> InventoryDrawer.open(new RivalsFrame(getViewer(), this, union)));
        rivals.setPermission(ClickType.LEFT, "unionsog.anyone.rivalries");
        add(rivals);

    }

    private void updateFrame() {

        InventoryDrawer.open(this);

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
