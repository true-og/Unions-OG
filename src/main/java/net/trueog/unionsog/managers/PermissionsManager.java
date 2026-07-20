package net.trueog.unionsog.managers;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import net.trueog.unionsog.*;
import net.trueog.unionsog.events.EconomyTransactionEvent;
import net.trueog.unionsog.events.EconomyTransactionEvent.Cause;
import net.trueog.diamondbankog.api.DiamondBankAPIJava;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.ENABLE_AUTO_GROUPS;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.PERMISSIONS_AUTO_GROUP_GROUPNAME;
import static org.bukkit.Bukkit.getPluginManager;

/**
 * @author phaed
 */
public final class PermissionsManager {

    private final UnionsOG plugin;

    private @Nullable LuckPerms luckPerms;
    private @Nullable DiamondBankAPIJava economy;

    private final HashMap<String, List<String>> permissions = new HashMap<>();
    private final HashMap<Player, PermissionAttachment> permAttaches = new HashMap<>();

    public PermissionsManager() {

        plugin = UnionsOG.getInstance();

        try {

            RegisteredServiceProvider<LuckPerms> registration = Bukkit.getServicesManager()
                    .getRegistration(LuckPerms.class);
            if (registration != null) {

                luckPerms = registration.getProvider();

            }

        } catch (NoClassDefFoundError e) {

            UnionsOG.getInstance().getLogger().info("LuckPerms not found. No extended permissions support.");

        }

        try {

            RegisteredServiceProvider<DiamondBankAPIJava> registration = Bukkit.getServicesManager()
                    .getRegistration(DiamondBankAPIJava.class);
            if (registration != null) {

                economy = registration.getProvider();

            }

        } catch (NoClassDefFoundError e) {

            UnionsOG.getInstance().getLogger().info("DiamondBank-OG not found. No economy support.");

        }

    }

    /**
     * Whether the DiamondBank-OG economy is available
     */
    public boolean hasEconomy() {

        return economy != null;

    }

    /**
     * Loads the permissions for each clan from the config
     */
    public void loadPermissions() {

        permissions.clear();
        for (Clan clan : plugin.getClanManager().getClans()) {

            permissions.put(clan.getTag(), UnionsOG.getInstance().getSettingsManager().getConfig()
                    .getStringList("permissions." + clan.getTag()));

        }

    }

    /**
     * Saves the permissions for each clan from the config
     */
    public void savePermissions() {

        for (Clan clan : plugin.getClanManager().getClans()) {

            if (permissions.containsKey(clan.getTag())) {

                UnionsOG.getInstance().getSettingsManager().getConfig().set("permissions." + clan.getTag(),
                        getPermissions(clan));

            }

        }

    }

    /**
     * Adds all permissions for a clan
     */
    public void updateClanPermissions(Clan clan) {

        for (ClanPlayer cp : clan.getMembers()) {

            addPlayerPermissions(cp);

        }

    }

    /**
     * Adds permissions for a player
     */
    public void addPlayerPermissions(@Nullable ClanPlayer cp) {

        if (cp == null) {

            return;

        }

        Clan clan = cp.getClan();
        if (clan == null) {

            return;

        }

        Player player = cp.toPlayer();
        if (player != null) {

            if (permissions.containsKey(clan.getTag())) {

                if (!permAttaches.containsKey(player)) {

                    permAttaches.put(player, player.addAttachment(UnionsOG.getInstance()));

                }

                // Adds all permissions from his clan
                for (String perm : getPermissions(clan)) {

                    permAttaches.get(player).setPermission(perm, true);

                }

                if (plugin.getSettingsManager().is(PERMISSIONS_AUTO_GROUP_GROUPNAME)) {

                    permAttaches.get(player).setPermission("group." + clan.getTag(), true);

                }

                player.recalculatePermissions();

            }

        }

    }

    /**
     * Removes permissions for a clan (when it gets disbanded for example)
     */
    public void removeClanPermissions(Clan clan) {

        for (ClanPlayer cp : clan.getMembers()) {

            removeClanPlayerPermissions(cp);
            removeClanPermissions(cp);

        }

    }

    /**
     * Removes permissions for a player (when he gets kicked for example)
     */
    public void removeClanPlayerPermissions(@Nullable ClanPlayer cp) {

        if (cp != null && cp.getClan() != null && cp.toPlayer() != null) {

            Player player = cp.toPlayer();
            if (player != null && permissions.containsKey(cp.getClan().getTag()) && permAttaches.containsKey(player)) {

                permAttaches.get(player).remove();
                permAttaches.remove(player);

            }

        }

    }

    /**
     * Removes permissions linked to a clan from the player
     */
    public void removeClanPermissions(ClanPlayer cp) {

        if (!plugin.getSettingsManager().is(ENABLE_AUTO_GROUPS)) {

            return;

        }

        Player player = cp.toPlayer();
        if (luckPerms != null && player != null) {

            User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
            user.data().remove(InheritanceNode.builder("clan_" + cp.getTag()).build());
            user.data().remove(InheritanceNode.builder("sc_untrusted").build());
            user.data().remove(InheritanceNode.builder("sc_trusted").build());
            user.data().remove(InheritanceNode.builder("sc_leader").build());
            luckPerms.getUserManager().saveUser(user);

        }

    }

    /**
     * @return the permissions for a clan
     */
    public List<String> getPermissions(Clan clan) {

        return permissions.get(clan.getTag());

    }

    /**
     * Converts a Diamond amount (at most one fractional digit, which counts Shards
     * 0-8) into Shards.
     *
     * @param diamonds the Diamond amount
     * @return the equivalent amount of Shards, or -1 if the amount is invalid
     */
    public long diamondsToShards(double diamonds) {

        if (economy == null) {

            return -1;

        }

        try {

            return economy.diamondsToShards(diamonds);

        } catch (Exception e) {

            plugin.getLogger().warning("Invalid Diamond amount (more than one fractional digit): " + diamonds);
            return -1;

        }

    }

    /**
     * Formats a Shard amount as a Diamond string (e.g. 113 -> "12.5")
     */
    public String shardsToDiamonds(long shards) {

        if (economy == null) {

            return String.valueOf(shards);

        }

        return economy.shardsToDiamonds(shards);

    }

    /**
     * Charges the online player the specified amount of Diamonds.
     * <p>
     * As the {@link EconomyTransactionEvent.Cause} is not passed, this method won't
     * fire the {@link EconomyTransactionEvent}. Use this method when you don't need
     * to track the cause or handle custom transaction events.
     * </p>
     *
     * @param player The player whose account will be charged.
     * @param money  The amount of Diamonds to charge.
     * @see EconomyTransactionEvent
     */
    public boolean chargePlayer(Player player, double money) {

        return chargePlayer(player, money, null);

    }

    /**
     * Charges the online player the specified amount of Diamonds.
     *
     * @param player The player whose account will be charged.
     * @param money  The amount of Diamonds to charge.
     * @param cause  The cause of the transaction.
     * @return {@code true} if the charge was successful, {@code false} otherwise.
     * @see EconomyTransactionEvent
     */
    public boolean chargePlayer(@NotNull Player player, double money, @Nullable Cause cause) {

        long shards = diamondsToShards(money);
        if (shards < 0) {

            return false;

        }

        return chargePlayerShards(player, shards, cause);

    }

    /**
     * Charges the online player the specified amount of Shards, consuming them from
     * the player's inventory, ender chest, and bank.
     *
     * @param player The player whose account will be charged.
     * @param shards The amount of Shards to charge.
     * @param cause  The cause of the transaction.
     * @return {@code true} if the charge was successful, {@code false} otherwise.
     * @see EconomyTransactionEvent
     */
    public boolean chargePlayerShards(@NotNull Player player, long shards, @Nullable Cause cause) {

        if (economy == null) {

            return false;

        }

        try {

            economy.consumeFromPlayer(player.getUniqueId(), shards, transactionReason(cause), null);

        } catch (Exception e) {

            return false;

        }

        if (cause == null) {

            return true;

        }

        EconomyTransactionEvent event = new EconomyTransactionEvent(player, shards, cause,
                EconomyTransactionEvent.TransactionType.WITHDRAW);
        getPluginManager().callEvent(event);

        if (event.isCancelled()) {

            refundShards(player, shards, cause);
            return false;

        }

        return true;

    }

    /**
     * Grants the online player the specified amount of Diamonds.
     * <p>
     * As the {@link EconomyTransactionEvent.Cause} is not passed, this method won't
     * fire the {@link EconomyTransactionEvent}. Use this method when you don't need
     * to track the cause or handle custom transaction events.
     * </p>
     *
     * @param player The player to whom the Diamonds will be granted.
     * @param money  The amount of Diamonds to grant.
     * @see PermissionsManager#grantPlayer(Player, double, Cause)
     * @see EconomyTransactionEvent
     */
    public boolean grantPlayer(Player player, double money) {

        return grantPlayer(player, money, null);

    }

    /**
     * Grants the online player the specified amount of Diamonds, deposited into the
     * player's DiamondBank-OG bank.
     *
     * @param player The player to whom the Diamonds will be granted.
     * @param money  The amount of Diamonds to grant.
     * @param cause  The cause of the transaction.
     * @return {@code true} if the grant was successful, {@code false} otherwise.
     * @see EconomyTransactionEvent
     */
    public boolean grantPlayer(@NotNull Player player, double money, @Nullable Cause cause) {

        long shards = diamondsToShards(money);
        if (shards < 0) {

            return false;

        }

        return grantPlayerShards(player, shards, cause);

    }

    /**
     * Grants the online player the specified amount of Shards, deposited into the
     * player's DiamondBank-OG bank.
     *
     * @param player The player to whom the Shards will be granted.
     * @param shards The amount of Shards to grant.
     * @param cause  The cause of the transaction.
     * @return {@code true} if the grant was successful, {@code false} otherwise.
     * @see EconomyTransactionEvent
     */
    public boolean grantPlayerShards(@NotNull Player player, long shards, @Nullable Cause cause) {

        if (economy == null) {

            return false;

        }

        try {

            economy.addToPlayerBankShards(player.getUniqueId(), shards, transactionReason(cause), null);

        } catch (Exception e) {

            return false;

        }

        if (cause == null) {

            return true;

        }

        EconomyTransactionEvent event = new EconomyTransactionEvent(player, shards, cause,
                EconomyTransactionEvent.TransactionType.DEPOSIT);
        getPluginManager().callEvent(event);

        if (event.isCancelled()) {

            try {

                economy.subtractFromPlayerBankShards(player.getUniqueId(), shards,
                        "Unions-OG revert: " + transactionReason(cause), null);

            } catch (Exception e) {

                plugin.getLogger().severe("Failed to revert cancelled grant of " + shards + " Shards to "
                        + player.getName() + ": " + e.getMessage());

            }

            return false;

        }

        return true;

    }

    /**
     * Check if the online player has the specified amount of Diamonds across their
     * inventory, ender chest, and bank.
     *
     * @return whether they have the money
     */
    public boolean playerHasMoney(Player player, double money) {

        long shards = diamondsToShards(money);
        return shards >= 0 && playerHasShards(player, shards);

    }

    /**
     * Check if the online player has the specified amount of Shards across their
     * inventory, ender chest, and bank.
     *
     * @return whether they have the Shards
     */
    public boolean playerHasShards(Player player, long shards) {

        return playerGetShards(player) >= shards;

    }

    /**
     * Returns the online player's total Shards (inventory + ender chest + bank), or
     * -1 if the economy is unavailable
     */
    public long playerGetShards(Player player) {

        if (economy == null) {

            return -1;

        }

        try {

            return economy.getTotalShards(player.getUniqueId());

        } catch (Exception e) {

            return -1;

        }

    }

    private String transactionReason(@Nullable Cause cause) {

        return cause == null ? "Unions-OG" : "Unions-OG: " + cause.name();

    }

    private void refundShards(Player player, long shards, Cause cause) {

        if (economy == null) {

            return;

        }

        try {

            economy.addToPlayerBankShards(player.getUniqueId(), shards, "Unions-OG revert: " + transactionReason(cause),
                    null);

        } catch (Exception e) {

            plugin.getLogger()
                    .severe("Failed to refund " + shards + " Shards to " + player.getName() + ": " + e.getMessage());

        }

    }

    /**
     * Check if a player has permissions
     *
     * @param player the player
     * @param perm   the permission
     * @return whether he has the permission
     */
    public boolean has(@Nullable Player player, String perm) {

        if (player == null) {

            UnionsOG.debug("null player");
            return false;

        }

        boolean hasPermission = player.hasPermission(perm);

        UnionsOG.debug(String.format("Permission %s is %s for %s", perm, hasPermission, player.getName()));
        return hasPermission;

    }

    /**
     * Checks if the player has the rank permission or the permission level, and the
     * equivalent Bukkit permission
     *
     * @param player     the player
     * @param permission the rank permission
     * @param notify     notify the player if they don't have permission
     * @deprecated use
     *             {@link PermissionsManager#has(Player, RankPermission, boolean)}
     *             or {@link PermissionsManager#has(Player, String)}
     */
    @Deprecated
    public boolean has(Player player, RankPermission permission, PermissionLevel level, boolean notify) {

        if (player == null || permission == null) {

            return false;

        }

        ClanPlayer clanPlayer = plugin.getClanManager().getClanPlayer(player);
        if (clanPlayer == null) {

            return false;

        }

        boolean hasBukkitPermission = has(player, permission.getBukkitPermission());
        if (!hasBukkitPermission) {

            return false;

        }

        boolean hasLevel = hasLevel(clanPlayer, level);

        boolean hasRankPermission = false;
        String rankName = clanPlayer.getRankId();
        Clan clan = clanPlayer.getClan();
        // noinspection ConstantConditions
        if (clan.hasRank(rankName)) {

            // noinspection ConstantConditions
            hasRankPermission = clan.getRank(rankName).getPermissions().contains(permission.toString());

        } else if (!rankName.isEmpty()) {

            clanPlayer.setRank(null);

        }

        if (notify && !hasLevel && !hasRankPermission) {

            ChatBlock.sendMessage(player,
                    ChatColor.RED
                            + MessageFormat.format(lang("you.must.be.0.or.have.the.permission.1.to.use.this", player),
                                    level == PermissionLevel.LEADER ? lang("leader", player) : lang("trusted", player),
                                    permission.toString()));

        }

        return hasLevel || hasRankPermission;

    }

    /**
     * Checks if the player has the rank permission or the permission level, and the
     * equivalent Bukkit permission
     *
     * @param player     the player
     * @param permission the rank permission
     * @param notify     notify the player if they don't have permission
     */
    public boolean has(Player player, RankPermission permission, boolean notify) {

        if (player == null || permission == null) {

            return false;

        }

        ClanPlayer clanPlayer = plugin.getClanManager().getClanPlayer(player);
        if (clanPlayer == null) {

            if (notify) {

                player.sendMessage(ChatColor.RED + lang("not.a.member.of.any.clan", player));

            }

            return false;

        }

        boolean hasBukkitPermission = has(player, permission.getBukkitPermission());
        if (!hasBukkitPermission) {

            if (notify) {

                ChatBlock.sendMessage(player, ChatColor.RED + lang("insufficient.permissions", player));

            }

            return false;

        }

        boolean hasLevel = hasLevel(clanPlayer, permission.getPermissionLevel());
        boolean hasRankPermission = false;
        String rankName = clanPlayer.getRankId();
        Clan clan = clanPlayer.getClan();
        if (clan != null) {

            Rank rank = clan.getRank(rankName);
            if (rank != null) {

                hasRankPermission = rank.getPermissions().contains(permission.toString());
                if (!hasRankPermission && permission == RankPermission.COLOR) {

                    hasRankPermission = rank.getPermissions().contains("modtag");

                }

            }

        } else if (!rankName.isEmpty()) {

            clanPlayer.setRank(null);

        }

        if (notify && !hasLevel && !hasRankPermission) {

            ChatBlock.sendMessage(player,
                    ChatColor.RED
                            + MessageFormat.format(lang("you.must.be.0.or.have.the.permission.1.to.use.this", player),
                                    permission.getPermissionLevel() == PermissionLevel.LEADER ? lang("leader", player)
                                            : lang("trusted", player),
                                    permission.toString()));

        }

        return hasLevel || hasRankPermission;

    }

    private boolean hasLevel(@NotNull ClanPlayer cp, @Nullable PermissionLevel level) {

        if (level != null) {

            switch (level) {

                case LEADER:
                    return cp.isLeader();
                case TRUSTED:
                    return cp.isTrusted();

            }

        }

        return false;

    }

    /**
     * Gives the player permissions linked to a clan
     */
    public void addClanPermissions(ClanPlayer cp) {

        if (!plugin.getSettingsManager().is(ENABLE_AUTO_GROUPS) || cp == null || luckPerms == null) {

            return;

        }

        Player player = cp.toPlayer();
        if (player == null) {

            return;

        }

        User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
        user.data().remove(InheritanceNode.builder("sc_leader").build());
        user.data().remove(InheritanceNode.builder("sc_trusted").build());
        user.data().remove(InheritanceNode.builder("sc_untrusted").build());

        if (cp.getClan() != null) {

            user.data().add(InheritanceNode.builder("clan_" + cp.getTag()).build());
            if (cp.isLeader()) {

                user.data().add(InheritanceNode.builder("sc_leader").build());

            } else if (cp.isTrusted()) {

                user.data().add(InheritanceNode.builder("sc_trusted").build());

            } else {

                user.data().add(InheritanceNode.builder("sc_untrusted").build());

            }

        }

        luckPerms.getUserManager().saveUser(user);

    }

    public String getPrefix(Player p) {

        if (luckPerms == null) {

            return "";

        }

        CachedMetaData metaData = luckPerms.getPlayerAdapter(Player.class).getMetaData(p);
        String prefix = metaData.getPrefix();
        if (prefix == null) {

            return "";

        }

        return prefix.replace("&", "§").replace(String.valueOf((char) 194), "");

    }

    public String getSuffix(Player p) {

        if (luckPerms == null) {

            return "";

        }

        CachedMetaData metaData = luckPerms.getPlayerAdapter(Player.class).getMetaData(p);
        String suffix = metaData.getSuffix();
        if (suffix == null) {

            return "";

        }

        return suffix.replace("&", "§").replace(String.valueOf((char) 194), "");

    }

}
