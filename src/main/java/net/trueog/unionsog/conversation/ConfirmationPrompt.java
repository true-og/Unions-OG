package net.trueog.unionsog.conversation;

import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.managers.UnionManager;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.RED;

abstract public class ConfirmationPrompt extends StringPrompt {

    protected abstract Prompt confirm(UnionPlayer sender, Union union);

    protected Prompt decline(UnionPlayer sender) {

        return new MessagePromptImpl(RED + lang(getDeclineTextKey(), sender));

    }

    protected abstract String getPromptTextKey();

    protected abstract String getDeclineTextKey();

    @NotNull
    @Override
    public String getPromptText(@NotNull ConversationContext context) {

        Player player = (Player) context.getForWhom();
        List<String> options = Arrays.asList(lang("yes", player), lang("cancel", player));

        return RED + lang(getPromptTextKey(), player, options);

    }

    @Override
    public Prompt acceptInput(@NotNull ConversationContext cc, @Nullable String input) {

        final UnionsOG plugin = (UnionsOG) cc.getPlugin();

        Player player = (Player) cc.getForWhom();
        String yes = lang("yes", player);
        UnionManager cm = Objects.requireNonNull(plugin).getUnionManager();
        UnionPlayer cp = cm.getCreateUnionPlayer(player.getUniqueId());
        Union union = cp.getUnion();
        if (union == null) {

            return END_OF_CONVERSATION;

        }

        return yes.equalsIgnoreCase(input) ? confirm(cp, union) : decline(cp);

    }

}
