package net.trueog.unionsog.conversation;

import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.utils.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static net.trueog.unionsog.UnionsOG.lang;

public class CreateUnionTagPrompt extends StringPrompt {

    public static final String TAG_KEY = "tag";

    @Override
    public @NotNull String getPromptText(@NotNull ConversationContext context) {

        Player forWhom = (Player) context.getForWhom();
        if (context.getSessionData(TAG_KEY) != null) {

            return "";

        }

        return lang("insert.union.tag", forWhom, lang("cancel", forWhom));

    }

    @Override
    public boolean blocksForInput(@NotNull ConversationContext context) {

        return context.getSessionData(TAG_KEY) == null;

    }

    @Override
    public @Nullable Prompt acceptInput(@NotNull ConversationContext context, @Nullable String input) {

        Player player = (Player) context.getForWhom();
        UnionsOG plugin = (UnionsOG) context.getPlugin();
        input = input != null ? input : (String) context.getSessionData(TAG_KEY);
        context.setSessionData(TAG_KEY, null);
        if (input == null || plugin == null)
            return this;

        Prompt errorPrompt = validateTag(plugin, player, input);
        if (errorPrompt != null)
            return errorPrompt;
        context.setSessionData(TAG_KEY, input);
        return new CreateUnionNamePrompt();

    }

    @Nullable
    private Prompt validateTag(UnionsOG plugin, Player player, @NotNull String unionTag) {

        String cleanTag = ChatUtils.stripColors(unionTag);
        if (cleanTag.trim().equalsIgnoreCase("None")) {

            return new MessagePromptImpl(ChatColor.RED + "Your union cannot be named \"None\".", this);

        }

        if (plugin.getUnionManager().isUnion(cleanTag)) {

            return new MessagePromptImpl(ChatColor.RED + lang("union.with.this.tag.already.exists", player), this);

        }

        Optional<String> validationError = plugin.getTagValidator().validate(player, unionTag, false);
        return validationError.map(error -> new MessagePromptImpl(error, this)).orElse(null);

    }

}
