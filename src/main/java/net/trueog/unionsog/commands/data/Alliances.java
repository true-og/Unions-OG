package net.trueog.unionsog.commands.data;

import net.trueog.unionsog.ChatBlock;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.Helper;
import net.trueog.unionsog.UnionsOG;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.PAGE_SEPARATOR;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.SERVER_NAME;
import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.DARK_GRAY;

public class Alliances extends Sendable {

    public Alliances(@NotNull UnionsOG plugin, @NotNull CommandSender sender) {

        super(plugin, sender);

    }

    @Override
    public void send() {

        List<Union> unions = cm.getUnions();
        cm.sortUnionsByKDR(unions);
        sendHeader();

        for (Union union : unions) {

            chatBlock.addRow("  " + AQUA + union.getName(), union.getAllyString(DARK_GRAY + ", ", sender));

        }

        sendBlock();

    }

    private void sendHeader() {

        ChatBlock.sendBlank(sender);
        ChatBlock.saySingle(sender, sm.getColored(SERVER_NAME) + subColor + " " + lang("alliances", sender) + " "
                + headColor + Helper.generatePageSeparator(sm.getString(PAGE_SEPARATOR)));
        ChatBlock.sendBlank(sender);

        chatBlock.setAlignment("l", "l");
        chatBlock.addRow("  " + headColor + lang("union", sender), lang("allies", sender));

    }

}
