package net.trueog.unionsog.commands.data;

import net.trueog.unionsog.ChatBlock;
import net.trueog.unionsog.Helper;
import net.trueog.unionsog.UnionsOG;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.PAGE_UNION_NAME_COLOR;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.PAGE_SEPARATOR;

public class Kills extends Sendable {

    private final Player player;
    private final String polled;

    public Kills(@NotNull UnionsOG plugin, @NotNull Player player, @NotNull String polled) {

        super(plugin, player);
        this.player = player;
        this.polled = polled;

    }

    @Override
    public void send() {

        plugin.getStorageManager().getKillsPerPlayer(polled, data -> new BukkitRunnable() {

            @Override
            public void run() {

                if (data.isEmpty()) {

                    ChatBlock.sendMessage(player, ChatColor.RED + lang("nokillsfound", player));
                    return;

                }

                configureAndSendHeader();
                addLines(data);

                sendBlock();

            }

        }.runTask(plugin));

    }

    private void addLines(Map<String, Integer> data) {

        Map<String, Integer> killsPerPlayer = Helper.sortByValue(data);

        for (Map.Entry<String, Integer> playerKills : killsPerPlayer.entrySet()) {

            int count = playerKills.getValue();
            chatBlock.addRow("  " + playerKills.getKey(), ChatColor.AQUA + "" + count);

        }

    }

    private void configureAndSendHeader() {

        chatBlock.setFlexibility(true, false);
        chatBlock.setAlignment("l", "c");
        chatBlock.addRow("  " + headColor + lang("victim", player), lang("killcount", player));
        ChatBlock.saySingle(player, sm.getColored(PAGE_UNION_NAME_COLOR) + polled + subColor + " "
                + lang("kills", player) + " " + headColor + Helper.generatePageSeparator(sm.getString(PAGE_SEPARATOR)));
        ChatBlock.sendBlank(player);

    }

}
