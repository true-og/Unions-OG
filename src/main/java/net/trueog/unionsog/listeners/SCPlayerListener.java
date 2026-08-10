package net.trueog.unionsog.listeners;

import net.trueog.unionsog.*;
import net.trueog.unionsog.UnionPlayer.Channel;
import net.trueog.unionsog.managers.PermissionsManager;
import net.trueog.unionsog.managers.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static net.trueog.unionsog.UnionPlayer.Channel.UNION;
import static net.trueog.unionsog.UnionPlayer.Channel.NONE;
import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.chat.SCMessage.Source.SPIGOT;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;

/**
 * @author phaed
 */
public class SCPlayerListener extends SCListener {

    private final SettingsManager settingsManager;

    public SCPlayerListener(@NotNull UnionsOG plugin) {

        super(plugin);
        settingsManager = plugin.getSettingsManager();
        registerChatListener();

    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {

        String[] split = event.getMessage().substring(1).split(" ");
        String command = split[0];

        if (settingsManager.is(UNIONCHAT_TAG_BASED)) {

            Union union = plugin.getUnionManager().getUnion(command);
            if (union == null || !union.isMember(event.getPlayer())) {

                return;

            }

            String replaced = event.getMessage().replaceFirst(command, settingsManager.getString(COMMANDS_UNION_CHAT));
            event.setMessage(replaced);

        }

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void handleChatTags(AsyncPlayerChatEvent event) {

        Player player = event.getPlayer();
        if (isBlacklistedWorld(player)) {

            return;

        }

        UnionPlayer cp = plugin.getUnionManager().getAnyUnionPlayer(player.getUniqueId());
        String tagLabel = cp != null && cp.isTagEnabled() ? cp.getTagLabel() : null;
        if (settingsManager.is(CHAT_COMPATIBILITY_MODE) && settingsManager.is(DISPLAY_CHAT_TAGS)) {

            if (tagLabel != null) {

                if (player.getDisplayName().contains("{union}") || player.getDisplayName().contains("{clan}")) {

                    player.setDisplayName(
                            player.getDisplayName().replace("{union}", tagLabel).replace("{clan}", tagLabel));

                } else if (event.getFormat().contains("{union}") || event.getFormat().contains("{clan}")) {

                    event.setFormat(event.getFormat().replace("{union}", tagLabel).replace("{clan}", tagLabel));

                } else {

                    String format = event.getFormat();
                    event.setFormat(tagLabel + format);

                }

            } else {

                event.setFormat(event.getFormat().replace("{union}", "").replace("{clan}", ""));
                event.setFormat(event.getFormat().replace("tagLabel", ""));

            }

        } else {

            plugin.getUnionManager().updateDisplayName(player);

        }

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        final Player player = event.getPlayer();
        if (isBlacklistedWorld(player)) {

            return;

        }

        UnionPlayer cp = plugin.getUnionManager().getCreateUnionPlayer(player.getUniqueId());

        updatePlayerName(player);
        plugin.getUnionManager().updateLastSeen(player);
        plugin.getUnionManager().updateDisplayName(player);

        plugin.getPermissionsManager().addPlayerPermissions(cp);

        if (settingsManager.is(BB_SHOW_ON_LOGIN) && cp.isBbEnabled() && cp.getUnion() != null) {

            cp.getUnion().displayBb(player, settingsManager.getInt(BB_LOGIN_SIZE));

        }

        plugin.getPermissionsManager().addUnionPermissions(cp);

        plugin.getProposalManager().remind(player);

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {

        Player player = event.getPlayer();
        if (!settingsManager.is(TELEPORT_HOME_ON_SPAWN) || isBlacklistedWorld(player)) {

            return;

        }

        Union union = plugin.getUnionManager().getUnionByPlayerUniqueId(player.getUniqueId());
        Location home;
        if (union != null && (home = union.getHomeLocation()) != null) {

            String homeServer = new Flags(union.getFlags()).getString("homeServer", "");
            if (homeServer.isEmpty() || plugin.getProxyManager().getServerName().equals(homeServer)) {

                event.setRespawnLocation(plugin.getTeleportManager().getSafe(home));

            }

        }

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        UnionPlayer cp = plugin.getUnionManager().getUnionPlayer(event.getPlayer());
        if (cp != null) {

            Union union = Objects.requireNonNull(cp.getUnion());
            Bukkit.getScheduler().runTask(plugin, () -> {

                if (union.getOnlineMembers().isEmpty()) {

                    plugin.getProtectionManager().setWarExpirationTime(cp.getUnion(),
                            settingsManager.getMinutes(WAR_DISCONNECT_EXPIRATION_TIME));

                }

            });

        }

        if (isBlacklistedWorld(event.getPlayer())) {

            return;

        }

        plugin.getPermissionsManager().removeUnionPlayerPermissions(cp);
        plugin.getUnionManager().updateLastSeen(event.getPlayer());
        plugin.getRequestManager().endPendingRequest(event.getPlayer().getName());

    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {

        if (isBlacklistedWorld(event.getPlayer())) {

            return;

        }

        plugin.getUnionManager().updateLastSeen(event.getPlayer());

    }

    private void registerChatListener() {

        EventPriority priority = EventPriority.valueOf(settingsManager.getString(UNIONCHAT_LISTENER_PRIORITY));
        plugin.getServer().getPluginManager().registerEvent(AsyncPlayerChatEvent.class, this, priority, (l, e) -> {

            if (!(e instanceof AsyncPlayerChatEvent)) {

                return;

            }

            AsyncPlayerChatEvent event = (AsyncPlayerChatEvent) e;
            Player player = event.getPlayer();
            UnionPlayer cp = plugin.getUnionManager().getUnionPlayer(player.getUniqueId());
            if (cp == null || isBlacklistedWorld(player)) {

                return;

            }

            Channel channel = cp.getChannel();
            if (channel != NONE) {

                PermissionsManager pm = plugin.getPermissionsManager();
                if ((channel == Channel.ALLY && !pm.has(player, "unionsog.member.ally"))
                        || (channel == UNION && !pm.has(player, "unionsog.member.chat")))
                {

                    ChatBlock.sendMessage(player, ChatColor.RED + lang("insufficient.permissions", player));
                    return;

                }

                plugin.getChatManager().processChat(SPIGOT, channel, cp, event.getMessage());
                event.setCancelled(true);

            }

        }, plugin, true);

    }

    private void updatePlayerName(@NotNull final Player player) {

        final UnionPlayer cp = plugin.getUnionManager().getAnyUnionPlayer(player.getUniqueId());

        UnionPlayer duplicate = null;
        for (UnionPlayer other : plugin.getUnionManager().getAllUnionPlayers()) {

            if (other.getName().equals(player.getName()) && !other.getUniqueId().equals(player.getUniqueId())) {

                duplicate = other;
                break;

            }

        }

        if (duplicate != null) {

            plugin.getLogger().warning(String.format("Found duplicate for %s, UUIDs: %s, %s", player.getName(),
                    player.getUniqueId(), duplicate.getUniqueId()));
            duplicate.setName(duplicate.getUniqueId().toString());
            plugin.getStorageManager().updatePlayerName(duplicate);

        }

        if (cp != null) {

            cp.setName(player.getName());
            plugin.getStorageManager().updatePlayerName(cp);

        }

    }

}
