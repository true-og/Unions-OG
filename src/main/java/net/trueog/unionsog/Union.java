package net.trueog.unionsog;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.trueog.unionsog.events.*;
import net.trueog.unionsog.hooks.papi.Placeholder;
import net.trueog.unionsog.loggers.BankLog;
import net.trueog.unionsog.loggers.BankLogger;
import net.trueog.unionsog.loggers.BankOperator;
import net.trueog.unionsog.managers.PermissionsManager;
import net.trueog.unionsog.managers.SettingsManager;
import net.trueog.unionsog.utils.ChatUtils;
import net.trueog.unionsog.utils.CurrencyFormat;
import net.trueog.unionsog.utils.DateFormat;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static net.trueog.unionsog.EconomyResponse.*;
import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.events.UnionBalanceUpdateEvent.Cause;
import static net.trueog.unionsog.loggers.BankLogger.Operation.*;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;
import static org.bukkit.ChatColor.*;

/**
 * @author phaed
 */
public class Union implements Serializable, Comparable<Union> {

    private static final long serialVersionUID = 1L;
    private static final String WARRING_KEY = "warring";
    private String tag;
    private String colorTag;
    private String name;
    private String description;
    private double balance;
    private double fee;
    private boolean friendlyFire;
    private long founded;
    private long lastUsed;
    private String capeUrl;
    private List<String> allies = new ArrayList<>();
    private List<String> rivals = new ArrayList<>();
    private List<String> bb = new ArrayList<>();
    private final List<UnionPlayer> members = new ArrayList<>();
    private Flags flags = new Flags(null);
    private boolean feeEnabled;
    private @Nullable ItemStack banner;

    /**
     *
     */
    public Union() {

        this.capeUrl = "";
        this.tag = "";

    }

    public Union(String tag, String name) {

        this.tag = Helper.cleanTag(tag);
        this.colorTag = ChatUtils.parseColors(tag);
        this.name = name;
        this.founded = (new Date()).getTime();
        this.lastUsed = (new Date()).getTime();
        this.capeUrl = "";
        if (UnionsOG.getInstance().getSettingsManager().is(UNION_FF_ON_BY_DEFAULT)) {

            friendlyFire = true;

        }

    }

    @Override
    public int hashCode() {

        return getTag().hashCode() >> 13;

    }

    @Override
    public boolean equals(@Nullable Object obj) {

        if (!(obj instanceof Union)) {

            return false;

        }

        Union other = (Union) obj;
        return other.getTag().equals(this.getTag());

    }

    @Override
    public int compareTo(Union other) {

        return this.getTag().compareToIgnoreCase(other.getTag());

    }

    @Override
    public String toString() {

        return tag;

    }

    /**
     * Deposits Shards to the union
     */
    public EconomyResponse deposit(@NotNull BankOperator sender, @NotNull Cause cause, double amount) {

        EconomyResponse response = null;

        if (amount < 0) {

            response = NEGATIVE_VALUE;

        }

        if (response == null) {

            response = setBalance(sender, cause, DEPOSIT, getBalance() + amount);

        }

        UnionsOG.getInstance().getBankLogger().log(new BankLog(sender, this, response, DEPOSIT, cause, amount));
        return response;

    }

    /**
     * Withdraws Shards from the union
     */
    public EconomyResponse withdraw(@NotNull BankOperator sender, @NotNull Cause cause, double amount) {

        EconomyResponse response = null;

        if (amount < 0) {

            response = NEGATIVE_VALUE;

        }

        if (getBalance() < amount) {

            response = NOT_ENOUGH_BALANCE;

        }

        if (response == null) {

            response = setBalance(sender, cause, WITHDRAW, getBalance() - amount);

        }

        UnionsOG.getInstance().getBankLogger().log(new BankLog(sender, this, response, WITHDRAW, cause, amount));
        return response;

    }

    /**
     * Returns the union's name
     *
     * @return the name
     */
    @Placeholder("name")
    public String getName() {

        return name;

    }

    /**
     * (used internally)
     *
     * @param name the name to set
     */
    public void setName(String name) {

        this.name = name;

    }

    /**
     * Renames the union. When the new name is just the tag in a different case, the
     * displayed tag is re-cased to match, so a union can fix its capitalization
     * even though the tag itself can never change.
     *
     * @param name the new name
     */
    public void rename(String name) {

        if (colorTag != null && Helper.cleanTag(name).equals(tag)) {

            colorTag = ChatUtils.applyCase(colorTag, ChatUtils.stripColors(name));

        }

        this.name = name;

    }

    /**
     * Returns the union's description
     *
     * @return the description or null if it doesn't have one
     */
    public String getDescription() {

        return description;

    }

    /**
     * (used internally)
     */
    public void setDescription(String description) {

        this.description = description;

    }

    /**
     * Sets the union's fee
     */
    public void setMemberFee(double fee) {

        if (fee < 0) {

            fee = 0;

        }

        this.fee = fee;

    }

    /**
     * Returns the union's fee
     *
     * @return the fee
     */
    public double getMemberFee() {

        return fee;

    }

    /**
     * Returns the union's balance
     *
     * @return the balance
     */
    @Placeholder("balance")
    public double getBalance() {

        return balance;

    }

    /**
     * Returns the union's balance (in Shards) formatted as Diamonds
     *
     * @return the balance formatted
     */
    @Placeholder("balance_formatted")
    public String getBalanceFormatted() {

        return CurrencyFormat.formatShards((long) balance);

    }

    /**
     * (used internally)
     *
     * @param balance the balance to set
     */
    private void setBalance(double balance) {

        setBalance(BankOperator.INTERNAL, Cause.INTERNAL, SET, balance);

    }

    public EconomyResponse setBalance(@NotNull BankOperator operator, @NotNull Cause cause,
            @NotNull BankLogger.Operation operation, double balance)
    {

        EconomyResponse response = SUCCESS;

        UnionBalanceUpdateEvent event = new UnionBalanceUpdateEvent(operator, this, getBalance(), balance, cause);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {

            response = CANCELLED;

        }

        this.balance = event.getNewBalance();
        if (cause != Cause.LOADING) {

            if (operation == SET) {

                UnionsOG.getInstance().getBankLogger().log(new BankLog(operator, this, response, SET, cause, balance));

            }

            UnionsOG.getInstance().getStorageManager().updateUnion(this);

        }

        return response;

    }

    /**
     * Returns the union's tag clean (no colors)
     *
     * @return the tag
     */
    @Placeholder("tag")
    public String getTag() {

        return tag;

    }

    /**
     * (used internally)
     *
     * @param tag the tag to set
     */
    public void setTag(String tag) {

        this.tag = tag;

    }

    /**
     * Returns the first color in the union's tag
     *
     * @return the color code or an empty string if there is no color
     */
    @Placeholder("color")
    public String getColor() {

        if (colorTag.startsWith("\u00a7x")) { // Hexadecimal Code

            return colorTag.substring(0, 14);

        } else if (colorTag.charAt(0) == '\u00a7') { // Regular Code

            return colorTag.substring(0, 2);

        } else { // No Code

            return "";

        }

    }

    /**
     * Returns the last used date in milliseconds
     *
     * @return the lastUsed
     */
    public long getLastUsed() {

        return lastUsed;

    }

    /**
     * Updates last used date to today (does not update union on db)
     */
    public void updateLastUsed() {

        setLastUsed((new Date()).getTime());

    }

    /**
     * Returns the number of days the union has been inactive
     */
    @Placeholder("inactivedays")
    public int getInactiveDays() {

        Timestamp now = new Timestamp((new Date()).getTime());
        return (int) Math.floor(Dates.differenceInDays(new Timestamp(getLastUsed()), now));

    }

    /**
     * (used internally)
     *
     * @param lastUsed the lastUsed to set
     */
    public void setLastUsed(long lastUsed) {

        this.lastUsed = lastUsed;

    }

    /**
     * Check whether this union allows friendly fire
     *
     * @return the friendlyFire
     */
    @Placeholder("friendly_fire")
    public boolean isFriendlyFire() {

        return friendlyFire;

    }

    /**
     * Sets the friendly fire status of this union (does not update union on db)
     *
     * @param friendlyFire the friendlyFire to set
     */
    public void setFriendlyFire(boolean friendlyFire) {

        this.friendlyFire = friendlyFire;

    }

    /**
     * Check if the player is a member of this union
     *
     * @param player the Player
     * @return confirmation
     */
    public boolean isMember(Player player) {

        return isMember(player.getUniqueId());

    }

    /**
     * Check if the player is a member of this union
     *
     * @param uuid the Player's UUID
     * @return confirmation
     */
    public boolean isMember(UUID uuid) {

        for (UnionPlayer cp : members) {

            if (cp.getUniqueId().equals(uuid)) {

                return true;

            }

        }

        return false;

    }

    @SuppressWarnings("deprecation")
    public boolean isMember(String playerName) {

        return isMember(Bukkit.getOfflinePlayer(playerName).getUniqueId());

    }

    /**
     * Returns a list with the contents of the bulletin board
     *
     * @return the bb
     */
    public List<String> getBb() {

        return Collections.unmodifiableList(bb);

    }

    /**
     * Return a list of all the allies' tags clean (no colors)
     *
     * @return the allies
     */
    @Placeholder(value = "allies_count", resolver = "list_size")
    public List<String> getAllies() {

        return Collections.unmodifiableList(allies);

    }

    private void addAlly(String tag) {

        allies.add(tag);

    }

    private boolean removeAlly(String ally) {

        if (!allies.contains(ally)) {

            return false;

        }

        allies.remove(ally);
        return true;

    }

    /**
     * The founded date in milliseconds
     *
     * @return the founded
     */
    public long getFounded() {

        return founded;

    }

    /**
     * The string representation of the founded date
     */
    @Placeholder("founded")
    public String getFoundedString() {

        return DateFormat.formatDateTime(founded);

    }

    /**
     * (used internally)
     *
     * @param founded the founded to set
     */
    public void setFounded(long founded) {

        this.founded = founded;

    }

    /**
     * Returns the color tag for this union
     *
     * @return the colorTag
     */
    @Placeholder("color_tag")
    public String getColorTag() {

        return colorTag;

    }

    // Returns the color tag wrapped in dark gray brackets with a trailing space,
    // blank when resolved without a union.
    @Placeholder("bracket_tag")
    public String getBracketTag() {

        return DARK_GRAY + "[" + colorTag + DARK_GRAY + "] ";

    }

    /**
     * (used internally)
     *
     * @param colorTag the colorTag to set
     */
    public void setColorTag(String colorTag) {

        this.colorTag = ChatUtils.parseColors(colorTag);

    }

    /**
     * Adds a bulletin board message without announcer
     */
    public void addBb(String msg) {

        addBbWithoutSaving(msg);
        UnionsOG.getInstance().getStorageManager().updateUnion(this);

    }

    public void setBb(List<String> bb) {

        this.bb = new ArrayList<>(bb);

    }

    /**
     * Adds a bulletin board message without saving it to the database
     */
    public void addBbWithoutSaving(String msg) {

        while (bb.size() > UnionsOG.getInstance().getSettingsManager().getInt(BB_SIZE)) {

            bb.remove(0);

        }

        bb.add(System.currentTimeMillis() + "_" + msg);

    }

    /**
     * Adds a bulletin board message without announcer and saves it to the database
     *
     * @param updateLastUsed should the union's last used time be updated as well?
     */
    public void addBb(String msg, boolean updateLastUsed) {

        addBbWithoutSaving(msg);
        UnionsOG.getInstance().getStorageManager().updateUnion(this, updateLastUsed);

    }

    /**
     * Clears the bulletin board
     */
    public void clearBb() {

        bb.clear();
        UnionsOG.getInstance().getStorageManager().updateUnion(this);

    }

    /**
     * (used internally)
     */
    public void importMember(UnionPlayer cp) {

        if (!members.contains(cp)) {

            members.add(cp);

        }

    }

    /**
     * (used internally)
     */
    public void removeMember(UUID uuid) {

        members.removeIf(cp -> cp.getUniqueId().equals(uuid));

    }

    /**
     * Get total union size
     */
    @Placeholder("size")
    public int getSize() {

        return members.size();

    }

    /**
     * Returns a list of all rival tags clean (no colors)
     *
     * @return the rivals
     */
    @Placeholder(value = "rivals_count", resolver = "list_size")
    public List<String> getRivals() {

        return Collections.unmodifiableList(rivals);

    }

    private void addRival(String tag) {

        rivals.add(tag);

    }

    private boolean removeRival(String rival) {

        return rivals.remove(rival);

    }

    /**
     * Check if the tag is a rival
     */
    public boolean isRival(String tag) {

        return rivals.contains(tag);

    }

    /**
     * Check if the tag is an ally
     */
    public boolean isAlly(String tag) {

        return allies.contains(tag);

    }

    @Placeholder("is_permanent")
    public boolean isPermanent() {

        return flags.getBoolean("permanent", false);

    }

    public void setPermanent(boolean permanent) {

        flags.put("permanent", permanent);

    }

    /**
     * Returns the cape url for this union
     *
     * @return the capeUrl
     */
    @Deprecated
    public String getCapeUrl() {

        return capeUrl;

    }

    /**
     * (used internally)
     *
     * @param capeUrl the capeUrl to set
     */
    @Deprecated
    public void setCapeUrl(String capeUrl) {

        this.capeUrl = capeUrl;

    }

    /**
     * (used internally)
     *
     * @return the packedBb
     */
    public String getPackedBb() {

        return String.join("|", bb);

    }

    /**
     * (used internally)
     *
     * @param packedBb the packedBb to set
     */
    public void setPackedBb(String packedBb) {

        bb = Helper.fromArrayToList(packedBb.split("[|]"));

    }

    /**
     * (used internally)
     *
     * @return the packedAllies
     */
    public String getPackedAllies() {

        return String.join("|", allies);

    }

    /**
     * (used internally)
     *
     * @param packedAllies the packedAllies to set
     */
    public void setPackedAllies(String packedAllies) {

        allies = Helper.fromArrayToList(packedAllies.split("[|]"));

    }

    /**
     * (used internally)
     *
     * @return the packedRivals
     */
    public String getPackedRivals() {

        return String.join("|", rivals);

    }

    /**
     * (used internally)
     *
     * @param packedRivals the packedRivals to set
     */
    public void setPackedRivals(String packedRivals) {

        rivals = Helper.fromArrayToList(packedRivals.split("[|]"));

    }

    /**
     * Returns a separator delimited string with all the ally union's colored tags
     */
    public String getAllyString(String sep, @Nullable CommandSender viewer) {

        String coloredAllies = getAllies().stream()
                .map(allyTag -> UnionsOG.getInstance().getUnionManager().getUnion(allyTag)).filter(Objects::nonNull)
                .map(Union::getColorTag).collect(Collectors.joining(sep));

        return coloredAllies.isEmpty() ? lang("none", viewer) : coloredAllies;

    }

    /**
     * @deprecated use {@link Union#getAllyString(String, CommandSender)}
     */
    @Deprecated
    public String getAllyString(String sep) {

        return getAllyString(sep, null);

    }

    /**
     * Returns a separator delimited string with all the rival union's colored tags
     */
    public String getRivalString(String sep, @Nullable CommandSender viewer) {

        String coloredRivals = getRivals().stream()
                .map(rivalTag -> UnionsOG.getInstance().getUnionManager().getUnion(rivalTag)).filter(Objects::nonNull)
                .map(rival -> isWarring(rival) ? DARK_RED + "[" + rival.getTag() + "]" : rival.getColorTag())
                .map(ChatUtils::parseColors).collect(Collectors.joining(sep));

        return coloredRivals.isEmpty() ? lang("none", viewer) : coloredRivals;

    }

    /**
     * @deprecated use {@link Union#getRivalString(String, CommandSender)}
     */
    @Deprecated
    public String getRivalString(String sep) {

        return getRivalString(sep, null);

    }

    /**
     * Get all members in the union
     *
     * @return the members
     */
    public List<UnionPlayer> getMembers() {

        return new ArrayList<>(members);

    }

    /**
     * Get all online members in the union
     *
     * @return the members
     */
    @Placeholder(value = "onlinemembers_count", resolver = "list_size", config = "filter_vanished")
    public List<UnionPlayer> getOnlineMembers() {

        return members.stream().filter(cp -> cp.toPlayer() != null).collect(Collectors.toList());

    }

    /**
     * Get all union's members
     *
     * @deprecated use {@link Union#getMembers()}
     */
    @Deprecated
    public List<UnionPlayer> getAllMembers() {

        return getMembers();

    }

    /**
     * Get all the ally union's members
     */
    public Set<UnionPlayer> getAllAllyMembers() {

        Set<UnionPlayer> out = new HashSet<>();

        for (String tag : allies) {

            Union ally = UnionsOG.getInstance().getUnionManager().getUnion(tag);

            if (ally != null) {

                out.addAll(ally.getMembers());

            }

        }

        return out;

    }

    /**
     * Gets the union's total KDR
     */
    @Placeholder(value = "total_kdr", resolver = "kdr")
    @Placeholder(value = "topclans_position", resolver = "ranking_position")
    public float getTotalKDR() {

        if (members.isEmpty()) {

            return 0;

        }

        double totalWeightedKills = 0;
        int totalDeaths = 0;

        for (UnionPlayer cp : members) {

            totalWeightedKills += cp.getWeightedKills();
            totalDeaths += cp.getDeaths();

        }

        if (totalDeaths == 0) {

            totalDeaths = 1;

        }

        return ((float) totalWeightedKills) / ((float) totalDeaths);

    }

    /**
     * Gets the union's total KDR
     */
    @Placeholder("total_deaths")
    public int getTotalDeaths() {

        int totalDeaths = 0;

        if (members.isEmpty()) {

            return totalDeaths;

        }

        for (UnionPlayer cp : members) {

            totalDeaths += cp.getDeaths();

        }

        return totalDeaths;

    }

    /**
     * Gets average weighted kills for the union
     */
    @Placeholder("average_wk")
    public int getAverageWK() {

        int total = 0;

        if (members.isEmpty()) {

            return total;

        }

        for (UnionPlayer cp : members) {

            total += cp.getWeightedKills();

        }

        return total / getSize();

    }

    @Placeholder("total_kills")
    public int getTotalKills() {

        return getTotalCivilian() + getTotalNeutral() + getTotalRival() + getTotalAlly();

    }

    /**
     * Gets total rival kills for the union
     */
    @Placeholder("total_rival")
    public int getTotalRival() {

        int total = 0;

        for (UnionPlayer cp : getMembers()) {

            total += cp.getRivalKills();

        }

        return total;

    }

    /**
     * Gets total neutral kills for the union
     */
    @Placeholder("total_neutral")
    public int getTotalNeutral() {

        int total = 0;

        for (UnionPlayer cp : getMembers()) {

            total += cp.getNeutralKills();

        }

        return total;

    }

    /**
     * Gets total civilian kills for the union
     */
    @Placeholder("total_civilian")
    public int getTotalCivilian() {

        int total = 0;

        for (UnionPlayer cp : getMembers()) {

            total += cp.getCivilianKills();

        }

        return total;

    }

    @Placeholder("total_ally")
    public int getTotalAlly() {

        int total = 0;

        for (UnionPlayer cp : getMembers()) {

            total += cp.getAllyKills();

        }

        return total;

    }

    /**
     * Check whether the union has crossed the rival limit
     */
    public boolean reachedRivalLimit() {

        int rivalCount = rivals.size();
        int unionCount = UnionsOG.getInstance().getUnionManager().getRivableUnionCount() - 1;
        double rivalPercent = UnionsOG.getInstance().getSettingsManager().getPercent(RIVAL_LIMIT_PERCENT);

        double limit = ((double) unionCount) * (rivalPercent / ((double) 100));

        return rivalCount > limit;

    }

    /**
     * Add a new player to the union
     */
    public void addPlayerToUnion(UnionPlayer cp) {

        cp.removePastUnion(getColorTag());
        cp.setUnion(this);
        cp.setJoinDate(new Date().getTime());
        cp.setTrusted(UnionsOG.getInstance().getSettingsManager().is(UNION_TRUST_MEMBERS_BY_DEFAULT));
        importMember(cp);

        UnionsOG.getInstance().getStorageManager().updateUnionPlayer(cp);
        UnionsOG.getInstance().getStorageManager().updateUnion(this);

        // add union permission
        UnionsOG.getInstance().getPermissionsManager().addUnionPermissions(cp);
        UnionsOG.getInstance().getPermissionsManager().addPlayerPermissions(cp);

        Player player = UnionsOG.getInstance().getServer().getPlayer(cp.getUniqueId());

        if (player != null) {

            UnionsOG.getInstance().getUnionManager().updateDisplayName(player);

        }

        Bukkit.getPluginManager().callEvent(new PlayerJoinedUnionEvent(this, cp));

    }

    @SuppressWarnings("deprecation")
    public void removePlayerFromUnion(String playerName) {

        removePlayerFromUnion(Bukkit.getOfflinePlayer(playerName).getUniqueId());

    }

    /**
     * Remove a player from a union
     */
    public void removePlayerFromUnion(UUID playerUniqueId) {

        UnionPlayer cp = UnionsOG.getInstance().getUnionManager().getUnionPlayer(playerUniqueId);
        if (cp == null || !isMember(playerUniqueId)) {

            return;

        }

        // remove union group-permission
        UnionsOG.getInstance().getPermissionsManager().removeUnionPermissions(cp);

        // remove permissions
        UnionsOG.getInstance().getPermissionsManager().removeUnionPlayerPermissions(cp);

        cp.setUnion(null);
        cp.addPastUnion(getColorTag());
        cp.setTrusted(false);
        cp.setJoinDate(0);
        removeMember(playerUniqueId);

        UnionsOG.getInstance().getStorageManager().updateUnionPlayer(cp);
        UnionsOG.getInstance().getStorageManager().updateUnion(this);

        Player matched = UnionsOG.getInstance().getServer().getPlayer(playerUniqueId);

        if (matched != null) {

            UnionsOG.getInstance().getUnionManager().updateDisplayName(matched);

        }

        Bukkit.getPluginManager().callEvent(new PlayerKickedUnionEvent(this, cp));

    }

    /**
     * Add an ally to a union, and the union to the ally
     */
    public void addAlly(Union ally) {

        removeRival(ally.getTag());
        addAlly(ally.getTag());

        ally.removeRival(getTag());
        ally.addAlly(getTag());

        UnionsOG.getInstance().getStorageManager().updateUnion(this);
        UnionsOG.getInstance().getStorageManager().updateUnion(ally);
        Bukkit.getPluginManager().callEvent(new AllyUnionAddEvent(this, ally));

    }

    /**
     * Remove an ally from the union, and the union from the ally
     */
    public void removeAlly(Union ally) {

        removeAlly(ally.getTag());
        ally.removeAlly(getTag());

        UnionsOG.getInstance().getStorageManager().updateUnion(this);
        UnionsOG.getInstance().getStorageManager().updateUnion(ally);
        Bukkit.getPluginManager().callEvent(new AllyUnionRemoveEvent(this, ally));

    }

    /**
     * Add a rival to the union, and the union to the rival
     */
    public void addRival(Union rival) {

        removeAlly(rival.getTag());
        addRival(rival.getTag());

        rival.removeAlly(getTag());
        rival.addRival(getTag());

        UnionsOG.getInstance().getStorageManager().updateUnion(this);
        UnionsOG.getInstance().getStorageManager().updateUnion(rival);
        Bukkit.getPluginManager().callEvent(new RivalUnionAddEvent(this, rival));

    }

    /**
     * Removes a rival from the union, the union from the rival
     */
    public void removeRival(Union rival) {

        removeRival(rival.getTag());
        rival.removeRival(getTag());

        UnionsOG.getInstance().getStorageManager().updateUnion(this);
        UnionsOG.getInstance().getStorageManager().updateUnion(rival);
        Bukkit.getPluginManager().callEvent(new RivalUnionRemoveEvent(this, rival));

    }

    /**
     * Check whether any union member is online
     */
    @Placeholder("is_anyonline")
    public boolean isAnyOnline() {

        return members.stream().anyMatch(cp -> cp.toPlayer() != null);

    }

    /**
     * Change a union's tag
     */
    public void changeUnionTag(String tag) {

        setColorTag(tag);
        UnionsOG.getInstance().getStorageManager().updateUnion(this);

    }

    /**
     * Announce message to a whole union
     */
    public void unionAnnounce(String playerName, String msg) {

        String message = UnionsOG.getInstance().getSettingsManager().getColored(UNIONCHAT_ANNOUNCEMENT_COLOR) + msg;

        for (UnionPlayer cp : getMembers()) {

            ChatBlock.sendMessage(cp, message);

        }

        Bukkit.getConsoleSender().sendMessage(AQUA + "[" + lang("union.announce") + AQUA + "] " + AQUA + "["
                + Helper.getColorName(playerName) + WHITE + "] " + message);

    }

    /**
     * Announce message to all the members of a union
     */
    public void memberAnnounce(String msg) {

        String message = UnionsOG.getInstance().getSettingsManager().getColored(UNIONCHAT_ANNOUNCEMENT_COLOR) + msg;

        for (UnionPlayer cp : getMembers()) {

            ChatBlock.sendMessage(cp, message);

        }

        Bukkit.getConsoleSender().sendMessage(AQUA + "[" + lang("union.announce") + AQUA + "] " + WHITE + message);

    }

    /**
     * Add a new bb message and announce it to all online members of a union
     */
    public void addBb(String announcerName, String msg) {

        addBb(msg);
        unionAnnounce(announcerName,
                UnionsOG.getInstance().getSettingsManager().getColored(BB_PREFIX) + ChatUtils.parseColors(msg));

    }

    /**
     * Add a new bb message and announce it to all online members of a union
     */
    public void addBb(String announcerName, String msg, boolean updateLastUsed) {

        addBb(msg, updateLastUsed);
        unionAnnounce(announcerName,
                UnionsOG.getInstance().getSettingsManager().getColored(BB_PREFIX) + ChatUtils.parseColors(msg));

    }

    /**
     * Displays bb to a player
     */
    public void displayBb(Player player) {

        displayBb(player, -1);

    }

    /**
     * Displays bb to a player
     *
     * @param maxSize amount of lines to display
     */
    public void displayBb(Player player, int maxSize) {

        SettingsManager settings = UnionsOG.getInstance().getSettingsManager();

        ChatBlock.sendBlank(player);
        ChatBlock.saySingle(player, lang("bulletin.board.header", getName()));

        List<String> localBb;
        if (maxSize == -1) {

            localBb = bb;
            maxSize = settings.getInt(BB_SIZE);

        } else {

            localBb = new ArrayList<>(bb);

        }

        while (localBb.size() > maxSize) {

            localBb.remove(0);

        }

        for (String msg : localBb) {

            if (!sendBbTime(player, msg)) {

                String bbPrefix = settings.getColored(BB_PREFIX);
                ChatBlock.sendMessage(player, bbPrefix + ChatUtils.parseColors(msg));

            }

        }

        ChatBlock.sendBlank(player);

    }

    /**
     * Sends a bb message with the timestamp in a hover message, if the bb message
     * is timestamped
     *
     * @param msg the bb message
     * @return true if sent
     */
    @SuppressWarnings("deprecation")
    private boolean sendBbTime(Player player, String msg) {

        try {

            int index = msg.indexOf("_");
            if (index < 1) {

                return false;

            }

            String bbPrefix = UnionsOG.getInstance().getSettingsManager().getColored(BB_PREFIX);

            long time = (System.currentTimeMillis() - Long.parseLong(msg.substring(0, index))) / 1000L;
            msg = ChatUtils.parseColors(bbPrefix + msg.substring(++index));

            BaseComponent[] baseComponent = TextComponent.fromLegacyText(msg);
            TextComponent textMessage = new TextComponent(baseComponent);
            textMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    TextComponent.fromLegacyText(Dates.formatTime(time, 1) + lang("bb.ago"))));
            player.spigot().sendMessage(textMessage);
            return true;

        } catch (Exception rock) {

            return false;

        }

    }

    /**
     * Disbands the union
     *
     * @param sender   who is trying to disband
     * @param announce should it be announced?
     * @param force    should it be force disbanded?
     */
    public void disband(@Nullable CommandSender sender, boolean announce, boolean force) {

        Collection<UnionPlayer> unionPlayers = UnionsOG.getInstance().getUnionManager().getAllUnionPlayers();
        List<Union> unions = UnionsOG.getInstance().getUnionManager().getUnions();

        if (isPermanent() && !force) {

            ChatBlock.sendMessage(sender, RED + lang("cannot.disband.permanent", sender));
            return;

        }

        if (announce) {

            if (UnionsOG.getInstance().getSettingsManager().is(DISABLE_MESSAGES) && sender != null) {

                unionAnnounce(sender.getName(), AQUA + lang("union.has.been.disbanded", getName()));

            } else {

                UnionsOG.getInstance().getUnionManager()
                        .serverAnnounce(AQUA + lang("union.has.been.disbanded", getName()));

            }

        }

        payOutBankOnDisband();

        UnionsOG.getInstance().getPermissionsManager().removeUnionPermissions(this);
        for (UnionPlayer cp : unionPlayers) {

            if (cp.getTag().equals(getTag())) {

                cp.setUnion(null);
                cp.setJoinDate(0);
                cp.addPastUnion(getColorTag());

            }

        }

        Bukkit.getPluginManager().callEvent(new DisbandUnionEvent(sender, this));
        unions.remove(this);

        for (Union c : unions) {

            String disbanded = lang("union.disbanded");

            if (c.removeWarringUnion(this)) {

                c.addBb(disbanded, lang("you.are.no.longer.at.war", c.getName(), getColorTag()));

            }

            if (c.removeRival(getTag())) {

                c.addBb(disbanded, lang("has.been.disbanded.rivalry.ended", getName()));

            }

            if (c.removeAlly(getTag())) {

                c.addBb(disbanded, lang("has.been.disbanded.alliance.ended", getName()));

            }

        }

        UnionsOG.getInstance().getRequestManager().removeRequest(getTag());
        UnionsOG.getInstance().getProposalManager().remove(getTag());

        UnionsOG.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(UnionsOG.getInstance(), () -> {

            UnionsOG.getInstance().getUnionManager().removeUnion(getTag());
            UnionsOG.getInstance().getStorageManager().deleteUnion(this);

        }, 1);

    }

    /**
     * Splits the union's bank evenly between its members. Shards that cannot be
     * divided evenly go to the first members in roster order, so nothing is lost.
     * <p>
     * Does nothing while no economy is available, in which case the balance simply
     * disappears with the union, as it does today.
     * </p>
     */
    private void payOutBankOnDisband() {

        PermissionsManager permissions = UnionsOG.getInstance().getPermissionsManager();
        long shards = (long) getBalance();
        List<UnionPlayer> members = getMembers();

        if (!permissions.hasEconomy() || shards <= 0 || members.isEmpty()) {

            return;

        }

        long each = shards / members.size();
        long remainder = shards % members.size();

        for (int i = 0; i < members.size(); i++) {

            UnionPlayer member = members.get(i);
            long payout = each + (i < remainder ? 1 : 0);
            if (payout <= 0) {

                continue;

            }

            if (permissions.grantPlayerShards(member.getUniqueId(), payout, "union disband payout")) {

                ChatBlock.sendMessage(member,
                        AQUA + lang("union.disbanded.payout", member, CurrencyFormat.formatShards(payout)));

            }

        }

        setBalance(BankOperator.INTERNAL, Cause.INTERNAL, SET, 0);

    }

    public void disband() {

        disband(null, true, false);

    }

    /**
     * Whether this union can be rivaled
     */
    @Placeholder("is_unrivable")
    public boolean isUnrivable() {

        return UnionsOG.getInstance().getSettingsManager().isUnrivable(getTag());

    }

    /**
     * Returns whether this union is warring with another union
     *
     * @param tag the tag of the union we are at war with
     */
    public boolean isWarring(String tag) {

        return flags.getStringList(WARRING_KEY).contains(tag);

    }

    /**
     * Returns whether this union is warring with another union
     *
     * @param union the union we are testing against
     */
    public boolean isWarring(Union union) {

        return isWarring(union.getTag());

    }

    /**
     * Add a union to be at war with
     */
    public void addWarringUnion(@Nullable UnionPlayer requestPlayer, Union targetUnion) {

        List<String> warring = flags.getStringList(WARRING_KEY);
        if (!warring.contains(targetUnion.getTag())) {

            warring.add(targetUnion.getTag());
            flags.put(WARRING_KEY, warring);
            if (requestPlayer != null) {

                addBb(requestPlayer.getName(), lang("you.are.at.war", getName(), targetUnion.getColorTag()));

            }

            UnionsOG.getInstance().getStorageManager().updateUnion(this);

        }

    }

    public void addWarringUnion(Union targetUnion) {

        addWarringUnion(null, targetUnion);

    }

    /**
     * Remove a warring union
     */
    public boolean removeWarringUnion(Union union) {

        List<String> warring = flags.getStringList(WARRING_KEY);
        if (warring.remove(union.getTag())) {

            flags.put(WARRING_KEY, warring);
            UnionsOG.getInstance().getStorageManager().updateUnion(this);
            return true;

        }

        return false;

    }

    /**
     * Return a collection of all the warring unions
     *
     * @return the union list
     */
    public List<Union> getWarringUnions() {

        return flags.getStringList(WARRING_KEY).stream()
                .map(tag -> UnionsOG.getInstance().getUnionManager().getUnion(tag)).collect(Collectors.toList());

    }

    /**
     * Return the list of flags and their data as a json string
     *
     * @return the flags
     */
    public String getFlags() {

        return flags.toJSONString();

    }

    /**
     * Read the list of flags in from a json string
     *
     * @param flagString the flags to set
     */
    public void setFlags(String flagString) {

        flags = new Flags(flagString);

    }

    public void validateWarring() {

        List<String> warring = flags.getStringList(WARRING_KEY);
        Iterator<String> iterator = warring.iterator();
        while (iterator.hasNext()) {

            String unionTag = iterator.next();
            Union union = UnionsOG.getInstance().getUnionManager().getUnion(unionTag);
            if (union == null) {

                iterator.remove();

            }

        }

        flags.put(WARRING_KEY, warring);

    }

    public void setHomeLocation(@Nullable Location home) {

        flags.put("homeX", home != null ? home.getX() : 0);
        flags.put("homeY", home != null ? home.getY() : 0);
        flags.put("homeZ", home != null ? home.getZ() : 0);
        flags.put("homePitch", home != null ? home.getPitch() : 0);
        flags.put("homeYaw", home != null ? home.getYaw() : 0);
        String world = home != null && home.getWorld() != null ? home.getWorld().getName() : "";
        flags.put("homeWorld", world);
        String name = UnionsOG.getInstance().getProxyManager().getServerName();
        flags.put("homeServer", name);

        UnionsOG.getInstance().getStorageManager().updateUnion(this);

    }

    public @Nullable Location getHomeLocation() {

        String homeWorld = flags.getString("homeWorld");
        if (homeWorld == null) {

            return null;

        }

        World world = Bukkit.getWorld(homeWorld);
        if (world == null) {

            return null;

        }

        double x = flags.getNumber("homeX").doubleValue();
        double y = flags.getNumber("homeY").doubleValue();
        double z = flags.getNumber("homeZ").doubleValue();
        float yaw = flags.getNumber("homeYaw").floatValue();
        float pitch = flags.getNumber("homePitch").floatValue();

        return new Location(world, x, y, z, yaw, pitch);

    }

    public String getTagLabel() {

        SettingsManager sm = UnionsOG.getInstance().getSettingsManager();
        String bracketColor = sm.getColored(TAG_BRACKET_COLOR);
        String bracketDefaultColor = sm.getColored(TAG_DEFAULT_COLOR);
        String bracketLeft = sm.getColored(TAG_BRACKET_LEFT);
        String bracketRight = sm.getColored(TAG_BRACKET_RIGHT);
        String tagSeparatorColor = sm.getColored(TAG_SEPARATOR_COLOR);
        String tagSeparator = sm.getString(TAG_SEPARATOR_CHAR);

        return bracketColor + bracketLeft + bracketDefaultColor + getColorTag() + bracketColor + bracketRight
                + tagSeparatorColor + tagSeparator;

    }

    /**
     * Checks if the fee is enabled
     *
     * @return true if enabled
     */
    public boolean isMemberFeeEnabled() {

        return feeEnabled;

    }

    /**
     * Enables or disables the fee
     */
    public void setMemberFeeEnabled(boolean enable) {

        feeEnabled = enable;

    }

    /**
     * @return the allowWithdraw
     */
    @Placeholder("allow_withdraw")
    public boolean isAllowWithdraw() {

        return flags.getBoolean("allowWithdraw", false);

    }

    /**
     * @param allowWithdraw the allowWithdraw to set
     */
    public void setAllowWithdraw(boolean allowWithdraw) {

        flags.put("allowWithdraw", allowWithdraw);

    }

    /**
     * @return the allowDeposit
     */
    @Placeholder("allow_deposit")
    public boolean isAllowDeposit() {

        return flags.getBoolean("allowDeposit", true);

    }

    /**
     * @param allowDeposit the allowDeposit to set
     */
    public void setAllowDeposit(boolean allowDeposit) {

        flags.put("allowDeposit", allowDeposit);

    }

    public void setBanner(@Nullable ItemStack banner) {

        if (banner == null) {

            this.banner = null;
            return;

        }

        banner = banner.clone();
        banner.setAmount(1);
        ItemMeta itemMeta = banner.getItemMeta();
        if (itemMeta != null) {

            // hides the banner patterns from the lore (I don't know why it's called
            // POTION_EFFECTS)
            itemMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            itemMeta.setLore(null);
            itemMeta.setDisplayName(null);
            banner.setItemMeta(itemMeta);

        }

        this.banner = banner;

    }

    public @Nullable ItemStack getBanner() {

        if (banner != null) {

            return banner.clone();

        }

        return null;

    }

}
