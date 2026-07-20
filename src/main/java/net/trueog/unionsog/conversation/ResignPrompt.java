package net.trueog.unionsog.conversation;

import net.trueog.unionsog.Clan;
import net.trueog.unionsog.ClanPlayer;
import org.bukkit.conversations.Prompt;

import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.RED;

/**
 * @author roinujnosde
 */
public class ResignPrompt extends ConfirmationPrompt {

    @Override
    protected Prompt confirm(ClanPlayer sender, Clan clan) {

        if (clan.isPermanent() || !sender.isLeader() || clan.getLeaders().size() > 1) {

            clan.addBb(sender.getName(), lang("0.has.resigned", sender.getName()));
            sender.addResignTime(clan.getTag());
            clan.removePlayerFromClan(sender.getUniqueId());

            return new MessagePromptImpl(AQUA + lang("resign.success", sender));

        } else if (sender.isLeader() && clan.getLeaders().size() == 1) {

            clan.disband(sender.toPlayer(), true, false);
            return new MessagePromptImpl(RED + lang("clan.has.been.disbanded", sender, clan.getName()));

        } else {

            return new MessagePromptImpl(RED
                    + lang("last.leader.cannot.resign.you.must.appoint.another.leader.or.disband.the.clan", sender));

        }

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
