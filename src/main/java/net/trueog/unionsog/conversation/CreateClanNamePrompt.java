package net.trueog.unionsog.conversation;

import net.trueog.unionsog.Clan;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.events.PreCreateClanEvent;
import net.trueog.unionsog.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.conversation.CreateClanTagPrompt.TAG_KEY;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.CLAN_DEFAULT_RANK;
import static org.bukkit.ChatColor.RED;

public class CreateClanNamePrompt extends StringPrompt {

    public static final String NAME_KEY = "name";

    @Override
    public @NotNull String getPromptText(@NotNull ConversationContext context) {

        if (context.getSessionData(NAME_KEY) != null) {

            return "";

        }

        return lang("insert.clan.name", (Player) context.getForWhom());

    }

    @Override
    public boolean blocksForInput(@NotNull ConversationContext context) {

        return context.getSessionData(NAME_KEY) == null;

    }

    @Override
    public @Nullable Prompt acceptInput(@NotNull ConversationContext context, @Nullable String clanName) {

        UnionsOG plugin = (UnionsOG) context.getPlugin();
        Player player = (Player) context.getForWhom();
        clanName = clanName != null ? clanName : (String) context.getSessionData(NAME_KEY);
        context.setSessionData(NAME_KEY, null);
        if (plugin == null || clanName == null)
            return this;

        Prompt errorPrompt = validateName(plugin, player, clanName);
        if (errorPrompt != null)
            return errorPrompt;

        String finalClanName = clanName;
        Bukkit.getScheduler().runTask(plugin, () -> {

            String tag = (String) context.getSessionData(TAG_KEY);
            // noinspection ConstantConditions
            PreCreateClanEvent event = new PreCreateClanEvent(player, tag, finalClanName);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {

                processClanCreation(plugin, player, tag, finalClanName);

            }

        });

        return END_OF_CONVERSATION;

    }

    private void processClanCreation(@NotNull UnionsOG plugin, @NotNull Player player, @NotNull String tag,
            @NotNull String name)
    {

        if (plugin.getClanManager().purchaseCreation(player)) {

            plugin.getClanManager().createClan(player, tag, name);

            Clan clan = plugin.getClanManager().getClan(tag);
            clan.addBb(player.getName(), lang("clan.created", name));
            plugin.getStorageManager().updateClan(clan);
            if (!plugin.getSettingsManager().getString(CLAN_DEFAULT_RANK).isEmpty()) {

                clan.setDefaultRank(plugin.getSettingsManager().getString(CLAN_DEFAULT_RANK));

            }

        }

    }

    @Nullable
    private Prompt validateName(@NotNull UnionsOG plugin, @NotNull Player player, @NotNull String input) {

        boolean bypass = plugin.getPermissionsManager().has(player, "unionsog.mod.bypass");
        if (!bypass) {

            if (ChatUtils.stripColors(input).length() > plugin.getSettingsManager().getInt(CLAN_MAX_LENGTH)) {

                return new MessagePromptImpl(RED + lang("your.clan.name.cannot.be.longer.than.characters", player,
                        plugin.getSettingsManager().getInt(CLAN_MAX_LENGTH)), this);

            }

            if (ChatUtils.stripColors(input).length() <= plugin.getSettingsManager().getInt(CLAN_MIN_LENGTH)) {

                return new MessagePromptImpl(RED + lang("your.clan.name.must.be.longer.than.characters", player,
                        plugin.getSettingsManager().getInt(CLAN_MIN_LENGTH)), this);

            }

        }

        if (input.contains("&")) {

            return new MessagePromptImpl(RED + lang("your.clan.name.cannot.contain.color.codes", player), this);

        }

        if (ChatUtils.stripColors(input).trim().equalsIgnoreCase("None")) {

            return new MessagePromptImpl(RED + "Your union cannot be named \"None\".", this);

        }

        return null;

    }

}
