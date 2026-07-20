package net.trueog.unionsog.conversation;

import net.trueog.unionsog.Clan;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.events.CreateRankEvent;
import net.trueog.unionsog.events.PreCreateRankEvent;
import org.bukkit.Bukkit;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.RED;

public class CreateRankNamePrompt extends StringPrompt {

    @Override
    public @NotNull String getPromptText(@NotNull ConversationContext context) {

        Player forWhom = (Player) context.getForWhom();
        return lang("insert.rank.name", forWhom, lang("cancel", forWhom));

    }

    @Override
    public @Nullable Prompt acceptInput(@NotNull ConversationContext context, @Nullable String input) {

        UnionsOG plugin = (UnionsOG) context.getPlugin();
        Player player = (Player) context.getForWhom();
        Clan clan = (Clan) context.getSessionData("clan");
        if (clan == null || plugin == null)
            return END_OF_CONVERSATION;
        if (input == null)
            return this;

        String rank = input.toLowerCase().replace(" ", "_");
        PreCreateRankEvent event = new PreCreateRankEvent(player, clan, rank);
        Bukkit.getServer().getPluginManager().callEvent(event);
        rank = event.getRankName();

        if (event.isCancelled()) {

            return null;

        }

        if (clan.hasRank(rank)) {

            return new MessagePromptImpl(RED + lang("rank.already.exists", player), this);

        }

        clan.createRank(rank);
        Bukkit.getServer().getPluginManager().callEvent(new CreateRankEvent(player, clan, clan.getRank(rank)));
        plugin.getStorageManager().updateClan(clan, true);
        return new MessagePromptImpl(AQUA + lang("rank.created", player));

    }

}
