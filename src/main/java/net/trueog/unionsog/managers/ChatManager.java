package net.trueog.unionsog.managers;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordReadyEvent;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import me.clip.placeholderapi.PlaceholderAPI;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.Helper;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.chat.ChatHandler;
import net.trueog.unionsog.chat.SCMessage;
import net.trueog.unionsog.hooks.discord.DiscordHook;
import net.trueog.unionsog.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static net.trueog.unionsog.UnionPlayer.Channel;
import static net.trueog.unionsog.chat.SCMessage.Source;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;
import static org.bukkit.Bukkit.getPluginManager;

public final class ChatManager {

    private final UnionsOG plugin;
    private final Set<ChatHandler> handlers = new HashSet<>();
    private DiscordHook discordHook;

    public ChatManager(UnionsOG plugin) {

        this.plugin = plugin;
        registerHandlers();
        if (isDiscordHookEnabled()) {

            DiscordSRV.api.subscribe(this);

        }

    }

    @Subscribe
    public void registerDiscord(DiscordReadyEvent event) {

        discordHook = new DiscordHook(plugin);
        DiscordSRV.api.subscribe(discordHook);
        getPluginManager().registerEvents(discordHook, plugin);

    }

    @Nullable
    public DiscordHook getDiscordHook() {

        if (isDiscordHookEnabled()) {

            // Manually instantiate, if JDA did load faster than SC
            if (discordHook == null && DiscordSRV.getPlugin().getJda().getStatus() == JDA.Status.CONNECTED) {

                registerDiscord(new DiscordReadyEvent());

            }

        }

        return discordHook;

    }

    public void processChat(@NotNull SCMessage message) {

        Union union = Objects.requireNonNull(message.getSender().getUnion(), "Clan cannot be null");

        List<UnionPlayer> receivers = new ArrayList<>();
        switch (message.getChannel()) {

            case ALLY:
                if (!plugin.getSettingsManager().is(ALLYCHAT_ENABLE)) {

                    return;

                }

                receivers.addAll(getOnlineAllyMembers(union).stream().filter(allyMember -> !allyMember.isMutedAlly())
                        .collect(Collectors.toList()));
                receivers.addAll(union.getOnlineMembers().stream().filter(onlineMember -> !onlineMember.isMutedAlly())
                        .collect(Collectors.toList()));
                break;
            case UNION:
                if (!plugin.getSettingsManager().is(UNIONCHAT_ENABLE)) {

                    return;

                }

                receivers.addAll(union.getOnlineMembers().stream().filter(member -> !member.isMuted())
                        .collect(Collectors.toList()));

        }

        message.setReceivers(receivers);

        for (ChatHandler ch : handlers) {

            if (ch.canHandle(message.getSource())) {

                ch.sendMessage(message.clone());

            }

        }

    }

    public void processChat(@NotNull Source source, @NotNull Channel channel, @NotNull UnionPlayer unionPlayer,
            String message)
    {

        Objects.requireNonNull(unionPlayer.getUnion(), "Clan cannot be null");
        processChat(new SCMessage(source, channel, unionPlayer, message));

    }

    public String parseChatFormat(String format, SCMessage message) {

        return parseChatFormat(format, message, new HashMap<>());

    }

    public String parseChatFormat(String format, SCMessage message, Map<String, String> placeholders) {

        SettingsManager sm = plugin.getSettingsManager();
        UnionPlayer sender = message.getSender();

        String memberColor = sm.getColored(ConfigField.valueOf(message.getChannel() + "CHAT_MEMBER_COLOR"));
        String trustedColor = sm.getColored(ConfigField.valueOf(message.getChannel() + "CHAT_TRUSTED_COLOR"));

        if (placeholders != null) {

            for (Map.Entry<String, String> e : placeholders.entrySet()) {

                format = format.replace("%" + e.getKey() + "%", e.getValue());

            }

        }

        String parsedFormat = ChatUtils.parseColors(format)
                .replace("%union%", Objects.requireNonNull(sender.getUnion()).getColorTag())
                .replace("%clean-tag%", sender.getUnion().getTag())
                .replace("%nick-color%", (sender.isTrusted() ? trustedColor : memberColor))
                .replace("%player%", sender.getName());
        parsedFormat = parseWithPapi(message.getSender(), parsedFormat).replace("%message%", message.getContent());

        return parsedFormat;

    }

    public boolean isDiscordHookEnabled() {

        return getPluginManager().getPlugin("DiscordSRV") != null && plugin.getSettingsManager().is(DISCORDCHAT_ENABLE);

    }

    private String parseWithPapi(UnionPlayer cp, String message) {

        if (getPluginManager().getPlugin("PlaceholderAPI") == null) {

            return message;

        }

        OfflinePlayer sender = Bukkit.getOfflinePlayer(cp.getUniqueId());
        message = PlaceholderAPI.setPlaceholders(sender, message);

        // If there are still placeholders left, try to parse them
        // E.g. if the user has a placeholder as LuckPerms prefix/suffix
        if (message.contains("%")) {

            message = PlaceholderAPI.setPlaceholders(sender, message);

        }

        return message;

    }

    private void registerHandlers() {

        Set<Class<? extends ChatHandler>> chatHandlers = Helper.getSubTypesOf("net.trueog.unionsog.chat.handlers",
                ChatHandler.class);
        plugin.getLogger().log(Level.INFO, "Registering {0} chat handlers...", chatHandlers.size());

        for (Class<? extends ChatHandler> handler : chatHandlers) {

            try {

                handlers.add(handler.getConstructor().newInstance());

            } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException ex)
            {

                plugin.getLogger().log(Level.SEVERE, "Error while trying to register {0}: " + ex.getMessage(),
                        handler.getSimpleName());

            }

        }

    }

    private List<UnionPlayer> getOnlineAllyMembers(Union union) {

        return union.getAllAllyMembers().stream().filter(allyPlayer -> allyPlayer.toPlayer() != null)
                .collect(Collectors.toList());

    }

}
