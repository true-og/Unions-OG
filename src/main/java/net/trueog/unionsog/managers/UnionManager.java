package net.trueog.unionsog.managers;

import com.cryptomorin.xseries.XMaterial;
import net.trueog.unionsog.*;
import net.trueog.unionsog.events.UnionBalanceUpdateEvent;
import net.trueog.unionsog.events.CreateUnionEvent;
import net.trueog.unionsog.events.EconomyTransactionEvent.Cause;
import net.trueog.unionsog.loggers.BankLogger;
import net.trueog.unionsog.loggers.BankOperator;
import net.trueog.unionsog.utils.ChatUtils;
import net.trueog.unionsog.utils.CurrencyFormat;
import net.trueog.unionsog.utils.VanishUtils;
import net.trueog.unionsog.uuid.UUIDMigration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;
import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.RED;

/**
 * @author phaed
 */
public final class UnionManager {

    private final UnionsOG plugin;
    private final ConcurrentHashMap<String, Union> unions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, UnionPlayer> unionPlayers = new ConcurrentHashMap<>();
    private final HashMap<UnionPlayer, List<Kill>> kills = new HashMap<>();

    /**
     *
     */
    public UnionManager() {

        plugin = UnionsOG.getInstance();

    }

    /**
     * Deletes all unions and union players in memory
     */
    public void cleanData() {

        unions.clear();
        unionPlayers.clear();
        kills.clear();

    }

    /**
     * Adds a kill to the memory
     */
    public void addKill(Kill kill) {

        if (kill == null) {

            return;

        }

        List<Kill> list = kills.computeIfAbsent(kill.getKiller(), k -> new ArrayList<>());

        Iterator<Kill> iterator = list.iterator();
        while (iterator.hasNext()) {

            Kill oldKill = iterator.next();
            if (oldKill.getVictim().equals(kill.getKiller())) {

                iterator.remove();
                continue;

            }

            // cleaning
            final int delay = plugin.getSettingsManager().getInt(KDR_DELAY_BETWEEN_KILLS);
            long timePassed = oldKill.getTime().until(LocalDateTime.now(), ChronoUnit.MINUTES);
            if (timePassed >= delay) {

                iterator.remove();

            }

        }

        list.add(kill);

    }

    /**
     * Checks if this kill respects the delay
     */
    public boolean isKillBeforeDelay(Kill kill) {

        if (kill == null) {

            return false;

        }

        List<Kill> list = kills.get(kill.getKiller());
        if (list == null) {

            return false;

        }

        for (Kill oldKill : list) {

            if (oldKill.getVictim().equals(kill.getVictim())) {

                final int delay = plugin.getSettingsManager().getInt(KDR_DELAY_BETWEEN_KILLS);
                long timePassed = oldKill.getTime().until(kill.getTime(), ChronoUnit.MINUTES);
                if (timePassed < delay) {

                    return true;

                }

            }

        }

        return false;

    }

    /**
     * Import a union into the in-memory store
     */
    public void importUnion(Union union) {

        this.unions.put(union.getTag(), union);

    }

    /**
     * Import a union player into the in-memory store
     */
    public void importUnionPlayer(UnionPlayer cp) {

        if (cp.getUniqueId() != null) {

            this.unionPlayers.put(cp.getUniqueId(), cp);

        }

    }

    /**
     * Create a new union
     */
    public void createUnion(Player player, String colorTag, String name) {

        UnionPlayer cp = getCreateUnionPlayer(player.getUniqueId());

        Union union = new Union(colorTag, name);
        union.addPlayerToUnion(cp);

        plugin.getStorageManager().insertUnion(union);
        importUnion(union);
        plugin.getStorageManager().updateUnionPlayer(cp);

        plugin.getRequestManager().deny(cp); // denies any previous invitation
        UnionsOG.getInstance().getPermissionsManager().updateUnionPermissions(union);
        UnionsOG.getInstance().getServer().getPluginManager().callEvent(new CreateUnionEvent(union));

    }

    /**
     * Reset a player's KDR
     */
    public void resetKdr(UnionPlayer cp) {

        cp.setCivilianKills(0);
        cp.setNeutralKills(0);
        cp.setRivalKills(0);
        cp.setAllyKills(0);
        cp.setDeaths(0);
        plugin.getStorageManager().updateUnionPlayer(cp);

    }

    /**
     * Delete a players data file
     */
    public void deleteUnionPlayer(UnionPlayer cp) {

        Union union = cp.getUnion();
        if (union != null) {

            union.removePlayerFromUnion(cp.getUniqueId());

        }

        unionPlayers.remove(cp.getUniqueId());
        plugin.getStorageManager().deleteUnionPlayer(cp);

    }

    /**
     * Delete a player data from memory
     */
    public void deleteUnionPlayerFromMemory(UUID playerUniqueId) {

        unionPlayers.remove(playerUniqueId);

    }

    /**
     * Remove a union from memory
     */
    public void removeUnion(String tag) {

        unions.remove(tag);

    }

    /**
     * Whether the tag belongs to a union
     */
    public boolean isUnion(String tag) {

        return unions.containsKey(Helper.cleanTag(tag));

    }

    /**
     * Returns the union the tag belongs to
     */
    public Union getUnion(String tag) {

        return unions.get(Helper.cleanTag(tag));

    }

    @SuppressWarnings("deprecation")
    @Nullable
    public Union getUnionByPlayerName(String playerName) {

        return getUnionByPlayerUniqueId(Bukkit.getOfflinePlayer(playerName).getUniqueId());

    }

    /**
     * Get a player's union
     *
     * @return null if not in a union
     */
    @Nullable
    public Union getUnionByPlayerUniqueId(UUID playerUniqueId) {

        UnionPlayer cp = getUnionPlayer(playerUniqueId);

        if (cp != null) {

            return cp.getUnion();

        }

        return null;

    }

    /**
     * @return the unions
     */
    public List<Union> getUnions() {

        return new ArrayList<>(unions.values());

    }

    /**
     * Returns the collection of all union players, including the disabled ones
     */
    public List<UnionPlayer> getAllUnionPlayers() {

        return new ArrayList<>(unionPlayers.values());

    }

    /**
     * Gets the UnionPlayer data object if a player is currently in a union, null if
     * he's not in a union Used for BungeeCord Reload UnionPlayer and your Union
     */
    @Deprecated
    public @Nullable UnionPlayer getUnionPlayerJoinEvent(Player player) {

        UnionsOG.getInstance().getStorageManager().importFromDatabaseOnePlayer(player);
        return getUnionPlayer(player.getUniqueId());

    }

    /**
     * Gets the UnionPlayer data object if a player is currently in a union, null if
     * he's not in a union
     */
    public @Nullable UnionPlayer getUnionPlayer(@NotNull OfflinePlayer player) {

        return getUnionPlayer(player.getUniqueId());

    }

    /**
     * Gets the UnionPlayer data object if a player is currently in a union, null if
     * he's not in a union
     */
    public @Nullable UnionPlayer getUnionPlayer(@NotNull Player player) {

        return getUnionPlayer((OfflinePlayer) player);

    }

    /**
     * Gets the UnionPlayer data object if a player is currently in a union, null if
     * he's not in a union
     */
    public @Nullable UnionPlayer getUnionPlayer(UUID playerUniqueId) {

        UnionPlayer cp = unionPlayers.get(playerUniqueId);

        if (cp == null) {

            return null;

        }

        if (cp.getUnion() == null) {

            return null;

        }

        return cp;

    }

    @SuppressWarnings("deprecation")
    @Nullable
    public UnionPlayer getUnionPlayer(String playerName) {

        return getUnionPlayer(Bukkit.getOfflinePlayer(playerName).getUniqueId());

    }

    /**
     * Gets the UnionPlayer data object if a player is currently in a union, null if
     * he's not in a union
     */
    @Deprecated
    public @Nullable UnionPlayer getUnionPlayerName(String playerName) {

        UUID uuid = UUIDMigration.getForcedPlayerUUID(playerName);

        if (uuid == null) {

            return null;

        }

        UnionPlayer cp = unionPlayers.get(uuid);

        if (cp == null) {

            return null;

        }

        if (cp.getUnion() == null) {

            return null;

        }

        return cp;

    }

    /**
     * Gets the UnionPlayer data object for the player, will retrieve disabled union
     * players as well, these are players who used to be in a union but are not
     * currently in one, their data file persists and can be accessed. their union
     * will be null though.
     */

    @Nullable
    public UnionPlayer getAnyUnionPlayer(UUID uuid) {

        return unionPlayers.get(uuid);

    }

    @SuppressWarnings("deprecation")
    @Nullable
    public UnionPlayer getAnyUnionPlayer(String playerName) {

        for (UnionPlayer cp : getAllUnionPlayers()) {

            if (cp.getName().equalsIgnoreCase(playerName)) {

                return cp;

            }

        }

        return null;

    }

    /**
     * Gets the UnionPlayer object for the player, creates one if not found
     */
    @Deprecated
    public @Nullable UnionPlayer getCreateUnionPlayerUUID(String playerName) {

        UUID playerUniqueId = UUIDMigration.getForcedPlayerUUID(playerName);
        if (playerUniqueId != null) {

            return getCreateUnionPlayer(playerUniqueId);

        } else {

            return null;

        }

    }

    /**
     * Gets the UnionPlayer object for the player, creates one if not found
     */
    public UnionPlayer getCreateUnionPlayer(UUID uuid) {

        Objects.requireNonNull(uuid, "UUID must not be null");
        if (unionPlayers.containsKey(uuid)) {

            return unionPlayers.get(uuid);

        }

        UnionPlayer cp = new UnionPlayer(uuid);

        boolean save = true;
        for (UnionPlayer other : getAllUnionPlayers()) {

            if (other.getName().equals(cp.getName())) {

                save = false;
                break;

            }

        }

        if (save) {

            plugin.getStorageManager().insertUnionPlayer(cp);
            importUnionPlayer(cp);

        } else if (plugin.getSettingsManager().is(DEBUG)) {

            plugin.getLogger().log(Level.WARNING,
                    String.format("There already is a ClanPlayer with the name %s", cp.getName()), new Exception());

        }

        return cp;

    }

    @SuppressWarnings("deprecation")
    @NotNull
    public UnionPlayer getCreateUnionPlayer(String playerName) {

        return getCreateUnionPlayer(Bukkit.getOfflinePlayer(playerName).getUniqueId());

    }

    /**
     * Announce message to the server
     *
     * @param msg the message
     */
    public void serverAnnounce(String msg) {

        if (plugin.getSettingsManager().is(DISABLE_MESSAGES)) {

            return;

        }

        plugin.getProxyManager().sendMessage("ALL", ChatColor.DARK_GRAY + "* " + AQUA + msg);
        Bukkit.getConsoleSender().sendMessage(AQUA + "[" + lang("server.announce") + "] " + ChatColor.WHITE + msg);

    }

    /**
     * Update the players display name with his union's tag
     */
    public void updateDisplayName(@Nullable Player player) {
        // do not update displayname if in compat mode

        if (plugin.getSettingsManager().is(CHAT_COMPATIBILITY_MODE)) {

            return;

        }

        if (player == null) {

            return;

        }

        if (plugin.getSettingsManager().is(DISPLAY_CHAT_TAGS)) {

            String prefix = plugin.getPermissionsManager().getPrefix(player);
            // String suffix = plugin.getPermissionsManager().getSuffix(player);
            String lastColor = plugin.getSettingsManager().is(COLOR_CODE_FROM_PREFIX_FOR_NAME)
                    ? ChatUtils.getLastColorCode(prefix)
                    : ChatColor.WHITE + "";
            String fullName = player.getName();

            UnionPlayer cp = plugin.getUnionManager().getAnyUnionPlayer(player.getUniqueId());

            if (cp == null) {

                return;

            }

            if (cp.isTagEnabled()) {

                Union union = cp.getUnion();

                if (union != null) {

                    fullName = union.getTagLabel() + lastColor + fullName + ChatColor.WHITE;

                }

                player.setDisplayName(fullName);

            } else {

                player.setDisplayName(lastColor + fullName + ChatColor.WHITE);

            }

        }

    }

    /**
     * Process a player and his union's last seen date
     */
    public void updateLastSeen(Player player) {

        UnionPlayer cp = getAnyUnionPlayer(player.getUniqueId());

        if (cp != null) {

            cp.updateLastSeen();
            plugin.getStorageManager().updateUnionPlayer(cp);

            Union union = cp.getUnion();

            if (union != null) {

                union.updateLastUsed();
                plugin.getStorageManager().updateUnion(union);

            }

        }

    }

    @SuppressWarnings("deprecation")
    public void ban(String playerName) {

        ban(Bukkit.getOfflinePlayer(playerName).getUniqueId());

    }

    /**
     * Bans a player from union commands
     *
     * @param uuid the player's uuid
     */
    public void ban(UUID uuid) {

        UnionPlayer cp = getUnionPlayer(uuid);
        Union union = null;
        if (cp != null) {

            union = cp.getUnion();

        }

        if (union != null) {

            if (union.getSize() == 1) {

                union.disband(null, false, false);

            } else {

                cp.setUnion(null);
                cp.addPastUnion(union.getColorTag());
                cp.setJoinDate(0);
                union.removeMember(uuid);

                plugin.getStorageManager().updateUnionPlayer(cp);
                plugin.getStorageManager().updateUnion(union);

            }

        }

        plugin.getSettingsManager().addBanned(uuid);

    }

    /**
     * Get a count of rivable unions
     */
    public int getRivableUnionCount() {

        int unionCount = 0;

        for (Union tm : unions.values()) {

            if (!UnionsOG.getInstance().getSettingsManager().isUnrivable(tm.getTag())) {

                unionCount++;

            }

        }

        return unionCount;

    }

    /**
     * Returns a formatted string detailing the players armor
     */
    public String getArmorString(PlayerInventory inv) {

        String out = "";

        ItemStack h = inv.getHelmet();

        Player player = null;
        InventoryHolder holder = inv.getHolder();
        if (holder instanceof Player) {

            player = (Player) holder;

        }

        if (h != null) {

            if (h.getType().equals(XMaterial.CHAINMAIL_HELMET.parseMaterial())) {

                out += ChatColor.WHITE + lang("armor.h", player);

            } else if (h.getType().equals(XMaterial.DIAMOND_HELMET.parseMaterial())) {

                out += AQUA + lang("armor.h", player);

            } else if (h.getType().equals(XMaterial.GOLDEN_HELMET.parseMaterial())) {

                out += ChatColor.YELLOW + lang("armor.h", player);

            } else if (h.getType().equals(XMaterial.IRON_HELMET.parseMaterial())) {

                out += ChatColor.GRAY + lang("armor.h", player);

            } else if (h.getType().equals(XMaterial.LEATHER_HELMET.parseMaterial())) {

                out += ChatColor.GOLD + lang("armor.h", player);

            } else if (h.getType().equals(XMaterial.AIR.parseMaterial())) {

                out += ChatColor.BLACK + lang("armor.h", player);

            } else {

                out += RED + lang("armor.h", player);

            }

        }

        ItemStack c = inv.getChestplate();

        if (c != null) {

            if (c.getType().equals(XMaterial.CHAINMAIL_CHESTPLATE.parseMaterial())) {

                out += ChatColor.WHITE + lang("armor.c", player);

            } else if (c.getType().equals(XMaterial.DIAMOND_CHESTPLATE.parseMaterial())) {

                out += AQUA + lang("armor.c", player);

            } else if (c.getType().equals(XMaterial.GOLDEN_CHESTPLATE.parseMaterial())) {

                out += ChatColor.YELLOW + lang("armor.c", player);

            } else if (c.getType().equals(XMaterial.IRON_CHESTPLATE.parseMaterial())) {

                out += ChatColor.GRAY + lang("armor.c", player);

            } else if (c.getType().equals(XMaterial.LEATHER_CHESTPLATE.parseMaterial())) {

                out += ChatColor.GOLD + lang("armor.c", player);

            } else if (c.getType().equals(XMaterial.AIR.parseMaterial())) {

                out += ChatColor.BLACK + lang("armor.c", player);

            } else {

                out += RED + lang("armor.c", player);

            }

        }

        ItemStack l = inv.getLeggings();

        if (l != null) {

            if (l.getType().equals(XMaterial.CHAINMAIL_LEGGINGS.parseMaterial())) {

                out += ChatColor.WHITE + lang("armor.l", player);

            } else if (l.getType().equals(XMaterial.DIAMOND_LEGGINGS.parseMaterial())) {

                out += lang("armor.l", player);

            } else if (l.getType().equals(XMaterial.GOLDEN_LEGGINGS.parseMaterial())) {

                out += lang("armor.l", player);

            } else if (l.getType().equals(XMaterial.IRON_LEGGINGS.parseMaterial())) {

                out += lang("armor.l", player);

            } else if (l.getType().equals(XMaterial.LEATHER_LEGGINGS.parseMaterial())) {

                out += lang("armor.l", player);

            } else if (l.getType().equals(XMaterial.AIR.parseMaterial())) {

                out += lang("armor.l", player);

            } else {

                out += lang("armor.l", player);

            }

        }

        ItemStack b = inv.getBoots();

        if (b != null) {

            if (b.getType().equals(XMaterial.CHAINMAIL_BOOTS.parseMaterial())) {

                out += ChatColor.WHITE + lang("armor.B", player);

            } else if (b.getType().equals(XMaterial.DIAMOND_BOOTS.parseMaterial())) {

                out += AQUA + lang("armor.B", player);

            } else if (b.getType().equals(XMaterial.GOLDEN_BOOTS.parseMaterial())) {

                out += ChatColor.YELLOW + lang("armor.B", player);

            } else if (b.getType().equals(XMaterial.IRON_BOOTS.parseMaterial())) {

                out += ChatColor.WHITE + lang("armor.B", player);

            } else if (b.getType().equals(XMaterial.LEATHER_BOOTS.parseMaterial())) {

                out += ChatColor.GOLD + lang("armor.B", player);

            } else if (b.getType().equals(XMaterial.AIR.parseMaterial())) {

                out += ChatColor.BLACK + lang("armor.B", player);

            } else {

                out += RED + lang("armor.B", player);

            }

        }

        if (out.length() == 0) {

            out = lang("none", player);

        }

        return out;

    }

    /**
     * Returns a formatted string detailing the players weapons
     */
    public String getWeaponString(PlayerInventory inv) {

        String headColor = plugin.getSettingsManager().getColored(PAGE_HEADINGS_COLOR);

        String out = "";

        Player player = null;
        InventoryHolder holder = inv.getHolder();
        if (holder instanceof Player) {

            player = (Player) holder;

        }

        int count = getItemCount(inv, XMaterial.DIAMOND_SWORD);

        if (count > 0) {

            String countString = count > 1 ? count + "" : "";
            out += AQUA + lang("weapon.S", player) + headColor + countString;

        }

        count = getItemCount(inv, XMaterial.GOLDEN_SWORD);

        if (count > 0) {

            String countString = count > 1 ? count + "" : "";
            out += ChatColor.YELLOW + lang("weapon.S", player) + headColor + countString;

        }

        count = getItemCount(inv, XMaterial.IRON_SWORD);

        if (count > 0) {

            String countString = count > 1 ? count + "" : "";
            out += ChatColor.WHITE + lang("weapon.S", player) + headColor + countString;

        }

        count = getItemCount(inv, XMaterial.STONE_SWORD);

        if (count > 0) {

            String countString = count > 1 ? count + "" : "";
            out += ChatColor.GRAY + lang("weapon.S", player) + headColor + countString;

        }

        count = getItemCount(inv, XMaterial.WOODEN_SWORD);

        if (count > 0) {

            String countString = count > 1 ? count + "" : "";
            out += ChatColor.GOLD + lang("weapon.S", player) + headColor + countString;

        }

        count = getItemCount(inv, XMaterial.BOW);

        if (count > 0) {

            String countString = count > 1 ? count + "" : "";
            out += ChatColor.GOLD + lang("weapon.B", player) + headColor + countString;

        }

        count = getItemCount(inv, XMaterial.ARROW);
        count += getItemCount(inv, XMaterial.SPECTRAL_ARROW);
        count += getItemCount(inv, XMaterial.TIPPED_ARROW);

        if (count > 0) {

            out += ChatColor.WHITE + lang("weapon.A", player) + headColor + count;

        }

        if (out.length() == 0) {

            out = lang("none", player);

        }

        return out;

    }

    private int getItemCount(@NotNull PlayerInventory inv, @NotNull XMaterial material) {

        Material parsed = material.parseMaterial();
        if (parsed == null) {

            return 0;

        }

        return getItemCount(inv.all(parsed));

    }

    private int getItemCount(HashMap<Integer, ? extends ItemStack> all) {

        int count = 0;

        for (ItemStack is : all.values()) {

            count += is.getAmount();

        }

        return count;

    }

    private double getFoodPoints(PlayerInventory inv, XMaterial material, int points, double saturation) {

        Material parsed = material.parseMaterial();
        if (parsed == null) {

            return 0;

        }

        return getFoodPoints(inv, parsed, points, saturation);

    }

    private double getFoodPoints(PlayerInventory inv, Material material, int points, double saturation) {

        return getItemCount(inv.all(material)) * (points + saturation);

    }

    /**
     * Returns a formatted string detailing the players food
     *
     * @param inv the PlayerInventory
     * @return the food points string
     */
    public String getFoodString(PlayerInventory inv) {

        Player player = null;
        InventoryHolder holder = inv.getHolder();
        if (holder instanceof Player) {

            player = (Player) holder;

        }

        double count = getFoodPoints(inv, XMaterial.APPLE, 4, 2.4);
        count += getFoodPoints(inv, XMaterial.BAKED_POTATO, 5, 6);
        count += getFoodPoints(inv, XMaterial.BEETROOT, 1, 1.2);
        count += getFoodPoints(inv, XMaterial.BEETROOT_SOUP, 6, 7.2);
        count += getFoodPoints(inv, XMaterial.BREAD, 5, 6);
        count += getFoodPoints(inv, XMaterial.CAKE, 14, 2.8);
        count += getFoodPoints(inv, XMaterial.CARROT, 3, 3.6);
        count += getFoodPoints(inv, XMaterial.CHORUS_FRUIT, 4, 2.4);
        count += getFoodPoints(inv, XMaterial.COOKED_CHICKEN, 6, 7.2);
        count += getFoodPoints(inv, XMaterial.COOKED_MUTTON, 6, 9.6);
        count += getFoodPoints(inv, XMaterial.COOKED_PORKCHOP, 8, 12.8);
        count += getFoodPoints(inv, XMaterial.COOKED_RABBIT, 5, 6);
        count += getFoodPoints(inv, XMaterial.COOKED_SALMON, 6, 9.6);
        count += getFoodPoints(inv, XMaterial.COOKIE, 2, .4);
        count += getFoodPoints(inv, XMaterial.GOLDEN_APPLE, 4, 9.6);
        count += getFoodPoints(inv, XMaterial.GOLDEN_CARROT, 6, 14.4);
        count += getFoodPoints(inv, XMaterial.MELON, 2, 1.2);
        count += getFoodPoints(inv, XMaterial.MUSHROOM_STEW, 6, 7.2);
        count += getFoodPoints(inv, XMaterial.POISONOUS_POTATO, 2, 1.2);
        count += getFoodPoints(inv, XMaterial.POTATO, 1, 0.6);
        count += getFoodPoints(inv, XMaterial.PUFFERFISH, 1, 0.2);
        count += getFoodPoints(inv, XMaterial.PUMPKIN_PIE, 8, 4.8);
        count += getFoodPoints(inv, XMaterial.RABBIT_STEW, 10, 12);
        count += getFoodPoints(inv, XMaterial.BEEF, 3, 1.8);
        count += getFoodPoints(inv, XMaterial.CHICKEN, 2, 1.2);
        count += getFoodPoints(inv, XMaterial.MUTTON, 2, 1.2);
        count += getFoodPoints(inv, XMaterial.PORKCHOP, 3, 1.8);
        count += getFoodPoints(inv, XMaterial.RABBIT, 3, 1.8);
        count += getFoodPoints(inv, XMaterial.SALMON, 1, .4);
        count += getFoodPoints(inv, XMaterial.COD, 2, .4);
        count += getFoodPoints(inv, XMaterial.COOKED_COD, 5, 6);
        count += getFoodPoints(inv, XMaterial.TROPICAL_FISH, 1, .2);
        count += getFoodPoints(inv, XMaterial.ROTTEN_FLESH, 4, .8);
        count += getFoodPoints(inv, XMaterial.SPIDER_EYE, 2, 3.2);
        count += getFoodPoints(inv, XMaterial.COOKED_BEEF, 8, 12.8);

        if (count == 0) {

            return lang("none", player);

        } else {

            return ((int) count) + "" + ChatColor.GOLD + "p";

        }

    }

    /**
     * Returns a colored bar based on the length
     */
    public String getBar(double length) {

        StringBuilder out = new StringBuilder();

        if (length >= 16) {

            out.append(ChatColor.GREEN);

        } else if (length >= 8) {

            out.append(ChatColor.GOLD);

        } else {

            out.append(RED);

        }

        for (int i = 0; i < length; i++) {

            out.append('|');

        }

        return out.toString();

    }

    /**
     * Sort unions by active
     */
    public void sortUnionsByActive(List<Union> unions, boolean asc) {

        unions.sort((c1, c2) -> {

            int o = 1;
            if (!asc) {

                o = -1;

            }

            return Long.compare(c1.getLastUsed(), c2.getLastUsed()) * o;

        });

    }

    /**
     * Sort unions by founded date
     */
    public void sortUnionsByFounded(List<Union> unions, boolean asc) {

        unions.sort((c1, c2) -> {

            int o = 1;
            if (!asc) {

                o = -1;

            }

            return Long.compare(c1.getFounded(), c2.getFounded()) * o;

        });

    }

    /**
     * Sort unions by kdr
     */
    public void sortUnionsByKDR(List<Union> unions, boolean asc) {

        unions.sort((c1, c2) -> {

            int o = 1;
            if (!asc) {

                o = -1;

            }

            return Float.compare(c1.getTotalKDR(), c2.getTotalKDR()) * o;

        });

    }

    /**
     * Sort unions by size
     */
    public void sortUnionsBySize(List<Union> unions, boolean asc) {

        unions.sort((c1, c2) -> {

            int o = 1;
            if (!asc) {

                o = -1;

            }

            return Integer.compare(c1.getSize(), c2.getSize()) * o;

        });

    }

    /**
     * Sort unions by name
     */
    public void sortUnionsByName(List<Union> unions, boolean asc) {

        unions.sort((c1, c2) -> {

            int o = 1;
            if (!asc) {

                o = -1;

            }

            return c1.getName().compareTo(c2.getName()) * o;

        });

    }

    /**
     * Sort unions by KDR
     */
    public void sortUnionsByKDR(List<Union> unions) {

        unions.sort((c1, c2) -> {

            Float o1 = c1.getTotalKDR();
            Float o2 = c2.getTotalKDR();

            return o2.compareTo(o1);

        });

    }

    /**
     * Sort unions by KDR
     */
    public void sortUnionsBySize(List<Union> unions) {

        unions.sort((c1, c2) -> {

            Integer o1 = c1.getMembers().size();
            Integer o2 = c2.getMembers().size();

            return o2.compareTo(o1);

        });

    }

    /**
     * Sort union players by KDR
     */
    public void sortUnionPlayersByKDR(List<UnionPlayer> cps) {

        cps.sort((c1, c2) -> {

            Float o1 = c1.getKDR();
            Float o2 = c2.getKDR();

            return o2.compareTo(o1);

        });

    }

    /**
     * Sort union players by last seen days
     */
    public void sortUnionPlayersByLastSeen(List<UnionPlayer> cps) {

        cps.sort((c1, c2) -> {

            Double o1 = c1.getLastSeenDays();
            Double o2 = c2.getLastSeenDays();

            return o1.compareTo(o2);

        });

    }

    public long getMinutesBeforeRejoin(@NotNull UnionPlayer cp, @NotNull Union union) {

        SettingsManager settings = plugin.getSettingsManager();
        if (settings.is(ENABLE_REJOIN_COOLDOWN)) {

            Long resign = cp.getResignTime(union.getTag());
            if (resign != null) {

                long timePassed = Instant.ofEpochMilli(resign).until(Instant.now(), ChronoUnit.MINUTES);
                int cooldown = settings.getInt(REJOIN_COOLDOWN);
                if (timePassed < cooldown) {

                    return cooldown - timePassed;

                }

            }

        }

        return 0;

    }

    /**
     * Purchase union creation
     */
    public boolean purchaseCreation(Player player) {

        if (!plugin.getSettingsManager().is(ECONOMY_PURCHASE_UNION_CREATE)) {

            return true;

        }

        double price = plugin.getSettingsManager().getDouble(ECONOMY_CREATION_PRICE);

        if (plugin.getPermissionsManager().hasEconomy()) {

            if (plugin.getPermissionsManager().chargePlayer(player, price, Cause.UNION_CREATION)) {

                player.sendMessage(RED + MessageFormat.format(lang("account.has.been.debited", player),
                        CurrencyFormat.format(price, player)));

            } else {

                player.sendMessage(RED + lang("not.sufficient.money", player, CurrencyFormat.format(price, player)));
                return false;

            }

        }

        return true;

    }

    /**
     * Purchase invite
     */
    public boolean purchaseInvite(Player player) {

        if (!plugin.getSettingsManager().is(ECONOMY_PURCHASE_UNION_INVITE)) {

            return true;

        }

        double price = plugin.getSettingsManager().getDouble(ECONOMY_INVITE_PRICE);

        if (plugin.getPermissionsManager().hasEconomy()) {

            if (plugin.getPermissionsManager().chargePlayer(player, price, Cause.UNION_INVITATION)) {

                player.sendMessage(RED + MessageFormat.format(lang("account.has.been.debited", player),
                        CurrencyFormat.format(price, player)));

            } else {

                player.sendMessage(RED + lang("not.sufficient.money", player, CurrencyFormat.format(price, player)));
                return false;

            }

        }

        return true;

    }

    /**
     * Purchase Home Teleport
     */
    public boolean purchaseHomeTeleport(Player player) {

        if (!plugin.getSettingsManager().is(ECONOMY_PURCHASE_HOME_TELEPORT)) {

            return true;

        }

        double price = plugin.getSettingsManager().getDouble(ECONOMY_HOME_TELEPORT_PRICE);

        if (plugin.getPermissionsManager().hasEconomy()) {

            if (plugin.getPermissionsManager().chargePlayer(player, price, Cause.UNION_HOME_TELEPORT)) {

                player.sendMessage(RED + MessageFormat.format(lang("account.has.been.debited", player),
                        CurrencyFormat.format(price, player)));

            } else {

                player.sendMessage(RED + lang("not.sufficient.money", player, CurrencyFormat.format(price, player)));
                return false;

            }

        }

        return true;

    }

    /**
     * Purchase Home Teleport Set
     */
    public boolean purchaseHomeTeleportSet(Player player) {

        if (!plugin.getSettingsManager().is(ECONOMY_PURCHASE_HOME_TELEPORT_SET)) {

            return true;

        }

        double price = plugin.getSettingsManager().getDouble(ECONOMY_HOME_TELEPORT_SET_PRICE);

        if (plugin.getPermissionsManager().hasEconomy()) {

            if (plugin.getPermissionsManager().chargePlayer(player, price, Cause.UNION_HOME_TELEPORT_SET)) {

                player.sendMessage(RED + MessageFormat.format(lang("account.has.been.debited", player),
                        CurrencyFormat.format(price, player)));

            } else {

                player.sendMessage(RED + lang("not.sufficient.money", player, CurrencyFormat.format(price, player)));
                return false;

            }

        }

        return true;

    }

    /**
     * Purchase Home Regroup
     */
    public boolean purchaseHomeRegroup(Player player) {

        UnionPlayer cp = plugin.getUnionManager().getUnionPlayer(player);
        if (cp == null) {

            return false;

        }

        if (!plugin.getSettingsManager().is(ECONOMY_PURCHASE_HOME_REGROUP)) {

            return true;

        }

        double price = plugin.getSettingsManager().getDouble(ECONOMY_REGROUP_PRICE);
        Union union = Objects.requireNonNull(cp.getUnion(), "Clan cannot be null");
        long shards = plugin.getPermissionsManager().diamondsToShards(price);
        if (shards < 0) {

            return false;

        }

        if (!plugin.getSettingsManager().is(ECONOMY_UNIQUE_TAX_ON_REGROUP)) {

            shards = shards * VanishUtils.getNonVanished(player, union).size();

        }

        if (plugin.getSettingsManager().is(ECONOMY_ISSUER_PAYS_REGROUP)
                && plugin.getPermissionsManager().hasEconomy())
        {

            if (plugin.getPermissionsManager().chargePlayerShards(player, shards, Cause.UNION_REGROUP)) {

                player.sendMessage(RED + MessageFormat.format(lang("account.has.been.debited", player),
                        CurrencyFormat.formatShards(shards, player)));

            } else {

                player.sendMessage(
                        RED + lang("not.sufficient.money", player, CurrencyFormat.formatShards(shards, player)));
                return false;

            }

        } else {

            BankOperator operator = new BankOperator(player, plugin.getPermissionsManager().playerGetShards(player));
            switch (union.withdraw(operator, UnionBalanceUpdateEvent.Cause.COMMAND, shards)) {

                case SUCCESS:
                    if (plugin.getPermissionsManager().grantPlayerShards(player, shards, null)) {

                        player.sendMessage(AQUA
                                + lang("player.union.withdraw", player, CurrencyFormat.formatShards(shards, player)));
                        union.addBb(player.getName(), lang("bb.union.withdraw", CurrencyFormat.formatShards(shards)));
                        return true;

                    }

                    union.setBalance(operator, UnionBalanceUpdateEvent.Cause.REVERT, BankLogger.Operation.WITHDRAW,
                            union.getBalance() + shards);
                    return false;
                case NOT_ENOUGH_BALANCE:
                    player.sendMessage(lang("union.bank.not.enough.money", player));

            }

        }

        return false;

    }

    /**
     * Processes a global chat command
     */
    @Deprecated
    public boolean processGlobalChat(Player player, String msg) {

        UnionPlayer cp = plugin.getUnionManager().getUnionPlayer(player.getUniqueId());

        if (cp == null) {

            return false;

        }

        String[] split = msg.split(" ");

        if (split.length == 0) {

            return false;

        }

        String command = split[0];

        if (command.equals(lang("on", player))) {

            cp.setGlobalChat(true);
            plugin.getStorageManager().updateUnionPlayer(cp);
            ChatBlock.sendMessage(player, AQUA + "You have enabled global chat");

        } else if (command.equals(lang("off", player))) {

            cp.setGlobalChat(false);
            plugin.getStorageManager().updateUnionPlayer(cp);
            ChatBlock.sendMessage(player, AQUA + "You have disabled global chat");

        } else {

            return true;

        }

        return false;

    }

}
