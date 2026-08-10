package net.trueog.unionsog.conversation;

import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.events.PreCreateUnionEvent;
import net.trueog.unionsog.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.conversation.CreateUnionTagPrompt.TAG_KEY;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;
import static org.bukkit.ChatColor.RED;

public class CreateUnionNamePrompt extends StringPrompt {

    public static final String NAME_KEY = "name";

    @Override
    public @NotNull String getPromptText(@NotNull ConversationContext context) {

        if (context.getSessionData(NAME_KEY) != null) {

            return "";

        }

        return lang("insert.union.name", (Player) context.getForWhom());

    }

    @Override
    public boolean blocksForInput(@NotNull ConversationContext context) {

        return context.getSessionData(NAME_KEY) == null;

    }

    @Override
    public @Nullable Prompt acceptInput(@NotNull ConversationContext context, @Nullable String unionName) {

        UnionsOG plugin = (UnionsOG) context.getPlugin();
        Player player = (Player) context.getForWhom();
        unionName = unionName != null ? unionName : (String) context.getSessionData(NAME_KEY);
        context.setSessionData(NAME_KEY, null);
        if (plugin == null || unionName == null)
            return this;

        Prompt errorPrompt = validateName(plugin, player, unionName);
        if (errorPrompt != null)
            return errorPrompt;

        String finalUnionName = unionName;
        Bukkit.getScheduler().runTask(plugin, () -> {

            String tag = (String) context.getSessionData(TAG_KEY);
            // noinspection ConstantConditions
            PreCreateUnionEvent event = new PreCreateUnionEvent(player, tag, finalUnionName);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {

                processUnionCreation(plugin, player, tag, finalUnionName);

            }

        });

        return END_OF_CONVERSATION;

    }

    private void processUnionCreation(@NotNull UnionsOG plugin, @NotNull Player player, @NotNull String tag,
            @NotNull String name)
    {

        if (plugin.getUnionManager().purchaseCreation(player)) {

            plugin.getUnionManager().createUnion(player, tag, name);

            Union union = plugin.getUnionManager().getUnion(tag);
            union.addBb(player.getName(), lang("union.created", name));
            plugin.getStorageManager().updateUnion(union);

        }

    }

    @Nullable
    private Prompt validateName(@NotNull UnionsOG plugin, @NotNull Player player, @NotNull String input) {

        boolean bypass = plugin.getPermissionsManager().has(player, "unionsog.mod.bypass");
        if (!bypass) {

            if (ChatUtils.stripColors(input).length() > plugin.getSettingsManager().getInt(UNION_MAX_LENGTH)) {

                return new MessagePromptImpl(RED + lang("your.union.name.cannot.be.longer.than.characters", player,
                        plugin.getSettingsManager().getInt(UNION_MAX_LENGTH)), this);

            }

            if (ChatUtils.stripColors(input).length() <= plugin.getSettingsManager().getInt(UNION_MIN_LENGTH)) {

                return new MessagePromptImpl(RED + lang("your.union.name.must.be.longer.than.characters", player,
                        plugin.getSettingsManager().getInt(UNION_MIN_LENGTH)), this);

            }

        }

        if (input.contains("&")) {

            return new MessagePromptImpl(RED + lang("your.union.name.cannot.contain.color.codes", player), this);

        }

        if (ChatUtils.stripColors(input).trim().equalsIgnoreCase("None")) {

            return new MessagePromptImpl(RED + "Your union cannot be named \"None\".", this);

        }

        return null;

    }

}
