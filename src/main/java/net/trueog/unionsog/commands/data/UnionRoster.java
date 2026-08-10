package net.trueog.unionsog.commands.data;

import net.trueog.unionsog.*;
import net.trueog.unionsog.utils.VanishUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;
import static org.bukkit.ChatColor.*;

public class UnionRoster extends Sendable {

    private final Union union;

    public UnionRoster(@NotNull UnionsOG plugin, @NotNull CommandSender sender, @NotNull Union union) {

        super(plugin, sender);
        this.union = union;

    }

    @Override
    public void send() {

        configureAndSendHeader();
        addMembers();

        sendBlock();

    }

    private void addMembers() {

        List<UnionPlayer> members = union.getMembers();
        plugin.getUnionManager().sortUnionPlayersByLastSeen(members);
        for (UnionPlayer cp : members) {

            String name = (cp.isTrusted() ? sm.getColored(PAGE_TRUSTED_COLOR) : sm.getColored(PAGE_UNTRUSTED_COLOR))
                    + cp.getName();
            String lastSeen = VanishUtils.isVanished(sender, cp) ? WHITE + cp.getLastSeenDaysString(sender)
                    : GREEN + lang("online", sender);

            chatBlock.addRow("  " + name, lastSeen);

        }

    }

    private void configureAndSendHeader() {

        ChatBlock.sendBlank(sender);
        ChatBlock.saySingle(sender,
                sm.getColored(PAGE_UNION_NAME_COLOR) + union.getName() + subColor + " " + lang("roster", sender) + " "
                        + headColor + Helper.generatePageSeparator(sm.getString(PAGE_SEPARATOR)));
        ChatBlock.sendBlank(sender);
        ChatBlock.sendMessage(sender,
                headColor + lang("legend", sender) + " " + sm.getColored(PAGE_TRUSTED_COLOR) + lang("trusted", sender)
                        + headColor + ", " + sm.getColored(PAGE_UNTRUSTED_COLOR) + lang("untrusted", sender));
        ChatBlock.sendBlank(sender);

        chatBlock.setFlexibility(false, true);
        chatBlock.addRow("  " + headColor + lang("player", sender), lang("seen", sender));

    }

}
