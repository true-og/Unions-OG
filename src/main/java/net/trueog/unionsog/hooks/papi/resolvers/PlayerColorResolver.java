package net.trueog.unionsog.hooks.papi.resolvers;

import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.hooks.papi.PlaceholderResolver;
import net.trueog.unionsog.managers.SettingsManager;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Map;

import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;

@SuppressWarnings("unused")
public class PlayerColorResolver extends PlaceholderResolver {

    private final SettingsManager settings;

    public PlayerColorResolver(@NotNull UnionsOG plugin) {

        super(plugin);
        settings = plugin.getSettingsManager();

    }

    @Override
    public @NotNull String getId() {

        return "player_color";

    }

    @Override
    public @NotNull String resolve(@Nullable OfflinePlayer player, @NotNull Object object, @NotNull Method method,
            @NotNull String placeholder, @NotNull Map<String, String> config)
    {

        UnionPlayer cp = object instanceof UnionPlayer ? ((UnionPlayer) object) : null;
        if (cp == null)
            return "";
        switch (placeholder) {

            case "clanchat_player_color": {

                return getUnionChatColor(cp);

            }
            case "allychat_player_color": {

                return getAllyChatColor(cp);

            }

        }

        return "";

    }

    private String getUnionChatColor(UnionPlayer player) {

        if (player.isTrusted()) {

            return settings.getColored(UNIONCHAT_TRUSTED_COLOR);

        }

        if (player.getUnion() != null) {

            return settings.getColored(UNIONCHAT_MEMBER_COLOR);

        }

        return "";

    }

    private String getAllyChatColor(UnionPlayer player) {

        if (player.isTrusted()) {

            return settings.getColored(ALLYCHAT_TRUSTED_COLOR);

        }

        if (player.getUnion() != null) {

            return settings.getColored(ALLYCHAT_MEMBER_COLOR);

        }

        return "";

    }

}
