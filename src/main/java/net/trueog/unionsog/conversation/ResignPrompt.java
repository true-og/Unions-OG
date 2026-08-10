package net.trueog.unionsog.conversation;

import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionPlayer;
import org.bukkit.conversations.Prompt;

import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.RED;

/**
 * @author roinujnosde
 */
public class ResignPrompt extends ConfirmationPrompt {

    @Override
    protected Prompt confirm(UnionPlayer sender, Union union) {

        if (union.isPermanent() || union.getSize() > 1) {

            union.addBb(sender.getName(), lang("0.has.resigned", sender.getName()));
            sender.addResignTime(union.getTag());
            union.removePlayerFromUnion(sender.getUniqueId());

            return new MessagePromptImpl(AQUA + lang("resign.success", sender));

        }

        union.disband(sender.toPlayer(), true, false);
        return new MessagePromptImpl(RED + lang("union.has.been.disbanded", sender, union.getName()));

    }

    @Override
    protected String getPromptTextKey() {

        return "resign.confirmation";

    }

    @Override
    protected String getDeclineTextKey() {

        return "resign.request.cancelled";

    }

}
