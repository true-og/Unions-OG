package net.trueog.unionsog.conversation;

import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.ProposalType;
import net.trueog.unionsog.UnionsOG;
import org.bukkit.conversations.Prompt;

import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.RED;

public class DisbandPrompt extends ConfirmationPrompt {

    @Override
    protected Prompt confirm(UnionPlayer sender, Union union) {

        if (union.isPermanent()) {

            return new MessagePromptImpl(RED + lang("cannot.disband.permanent", sender));

        }

        UnionsOG.getInstance().getProposalManager().propose(sender, union, ProposalType.DISBAND, "");
        return new MessagePromptImpl(AQUA + lang("proposal.opened.confirmation", sender));

    }

    @Override
    protected String getPromptTextKey() {

        return "disband.confirmation";

    }

    @Override
    protected String getDeclineTextKey() {

        return "disband.request.cancelled";

    }

}
