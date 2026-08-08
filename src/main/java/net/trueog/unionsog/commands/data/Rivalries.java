package net.trueog.unionsog.commands.data;

import net.trueog.unionsog.ChatBlock;
import net.trueog.unionsog.Clan;
import net.trueog.unionsog.Helper;
import net.trueog.unionsog.UnionsOG;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.PAGE_SEPARATOR;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.SERVER_NAME;
import static org.bukkit.ChatColor.*;

public class Rivalries extends Sendable {

    public Rivalries(@NotNull UnionsOG plugin, @NotNull CommandSender sender) {

        super(plugin, sender);

    }

    @Override
    public void send() {

        List<Clan> clans = cm.getClans();
        cm.sortClansByKDR(clans);
        sendHeader();

        for (Clan clan : clans) {

            chatBlock.addRow("  " + AQUA + clan.getName(), clan.getRivalString(DARK_GRAY + ", ", sender));

        }

        sendBlock();

    }

    private void sendHeader() {

        ChatBlock.sendBlank(sender);
        ChatBlock.saySingle(sender, sm.getColored(SERVER_NAME) + subColor + " " + lang("rivalries", sender) + " "
                + headColor + Helper.generatePageSeparator(sm.getString(PAGE_SEPARATOR)));
        ChatBlock.sendBlank(sender);
        ChatBlock.sendMessage(sender, headColor + lang("legend", sender) + DARK_RED + " [" + lang("war", sender) + "]");
        ChatBlock.sendBlank(sender);

        chatBlock.setAlignment("l", "l");
        chatBlock.addRow(lang("clan", sender), lang("rivals", sender));

    }

}
