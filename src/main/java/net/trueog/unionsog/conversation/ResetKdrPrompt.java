package net.trueog.unionsog.conversation;

import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.events.PlayerResetKdrEvent;
import net.trueog.unionsog.managers.UnionManager;
import org.bukkit.Bukkit;
import org.bukkit.conversations.Prompt;

import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.RED;

public class ResetKdrPrompt extends ConfirmationPrompt {

    private final UnionManager cm;

    public ResetKdrPrompt(UnionManager cm) {

        this.cm = cm;

    }

    @Override
    protected Prompt confirm(UnionPlayer sender, Union union) {

        PlayerResetKdrEvent event = new PlayerResetKdrEvent(sender);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {

            cm.resetKdr(sender);
            return new MessagePromptImpl(RED + lang("you.have.reseted.your.kdr", sender));

        } else {

            return END_OF_CONVERSATION;

        }

    }

    @Override
    protected String getPromptTextKey() {

        return "resetkdr.confirmation";

    }

    @Override
    protected String getDeclineTextKey() {

        return "resetkdr.request.cancelled";

    }

}
