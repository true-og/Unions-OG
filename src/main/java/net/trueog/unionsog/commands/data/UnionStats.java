package net.trueog.unionsog.commands.data;

import net.trueog.unionsog.*;
import net.trueog.unionsog.utils.KDRFormat;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.List;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;
import static org.bukkit.ChatColor.*;

public class UnionStats extends Sendable {

    private final Union union;

    public UnionStats(@NotNull UnionsOG plugin, @NotNull CommandSender sender, @NotNull Union union) {

        super(plugin, sender);
        this.union = union;

    }

    @Override
    public void send() {

        configureAndSendHeader();

        List<UnionPlayer> members = union.getMembers();
        cm.sortUnionPlayersByKDR(members);
        addRows(members);

        sendBlock();

    }

    private void configureAndSendHeader() {

        ChatBlock.saySingle(sender, sm.getColored(PAGE_UNION_NAME_COLOR) + union.getName() + subColor + " "
                + lang("stats", sender) + " " + headColor + Helper.generatePageSeparator(sm.getString(PAGE_SEPARATOR)));
        ChatBlock.sendBlank(sender);
        ChatBlock.sendMessage(sender,
                headColor + lang("kdr", sender) + " = " + subColor + lang("kill.death.ratio", sender));
        ChatBlock.sendMessage(sender,
                headColor + lang("weights", sender) + " = " + lang("rival", sender) + ": " + subColor
                        + sm.getDouble(KILL_WEIGHTS_RIVAL) + headColor + " " + lang("neutral", sender) + ": " + subColor
                        + sm.getDouble(KILL_WEIGHTS_NEUTRAL) + headColor + " " + lang("civilian", sender) + ": "
                        + subColor + sm.getDouble(KILL_WEIGHTS_CIVILIAN));
        ChatBlock.sendBlank(sender);

        chatBlock.setFlexibility(true, false, false, false, false, false, false);
        chatBlock.setAlignment("l", "c", "c", "c", "c", "c", "c");
        chatBlock.addRow("  " + headColor + lang("name", sender), lang("kdr", sender), lang("rival", sender),
                lang("neutral", sender), lang("civilian.abbreviation", sender), lang("deaths", sender));

    }

    private void addRows(List<UnionPlayer> unionPlayers) {

        for (UnionPlayer cp : unionPlayers) {

            String color;
            if (cp.isTrusted()) {

                color = sm.getColored(PAGE_TRUSTED_COLOR);

            } else {

                color = sm.getColored(PAGE_UNTRUSTED_COLOR);

            }

            NumberFormat formatter = NumberFormat.getInstance(cp.getLocale());
            String rival = formatter.format(cp.getRivalKills());
            String neutral = formatter.format(cp.getNeutralKills());
            String civilian = formatter.format(cp.getCivilianKills());
            String deaths = formatter.format(cp.getDeaths());
            String kdr = KDRFormat.format(cp.getKDR());

            chatBlock.addRow(color + cp.getName(), YELLOW + kdr, WHITE + rival, GRAY + neutral, DARK_GRAY + civilian,
                    DARK_RED + deaths);

        }

    }

}
