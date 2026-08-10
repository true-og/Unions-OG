package net.trueog.unionsog.commands.data;

import net.trueog.unionsog.*;
import net.trueog.unionsog.utils.KDRFormat;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;
import static org.bukkit.ChatColor.*;

public class Lookup extends Sendable {

    @NotNull
    private final UUID targetUuid;
    @Nullable
    private final UnionPlayer target;
    @Nullable
    private final Union senderUnion;
    @Nullable
    private final Union targetUnion;

    public Lookup(@NotNull UnionsOG plugin, @NotNull CommandSender sender, @NotNull UUID targetUuid) {

        super(plugin, sender);
        this.targetUuid = targetUuid;
        target = cm.getAnyUnionPlayer(targetUuid);
        UnionPlayer senderCp = !isPlayer() ? null : cm.getUnionPlayer(getPlayer().getUniqueId());
        senderUnion = senderCp == null ? null : senderCp.getUnion();
        targetUnion = target != null ? target.getUnion() : null;

    }

    @Override
    public void send() {

        if (target != null) {

            String lookup = lang("player.lookup", sender).replace("%player_name%", target.getName())
                    .replace("%union_name%", getUnionName()).replace("%player_status%", getPlayerStatus())
                    .replace("%player_kdr%", KDRFormat.format(target.getKDR()))
                    .replace("%player_rival_kills%", String.valueOf(target.getRivalKills()))
                    .replace("%player_neutral_kills%", String.valueOf(target.getNeutralKills()))
                    .replace("%player_civilian_kills%", String.valueOf(target.getCivilianKills()))
                    .replace("%player_ally_kills%", String.valueOf(target.getAllyKills()))
                    .replace("%player_deaths%", String.valueOf(target.getDeaths()))
                    .replace("%player_join_date%", target.getJoinDateString())
                    .replace("%player_last_seen%", target.getLastSeenString(sender))
                    .replace("%player_past_unions%", target.getPastUnionsString(headColor + ", "))
                    .replace("%player_inactive_days%", String.valueOf(target.getInactiveDays()))
                    .replace("%player_max_inactive_days%",
                            Helper.formatMaxInactiveDays(sm.getInt(PURGE_INACTIVE_PLAYER_DAYS)))
                    .replace("%kill_type_line%", getKillTypeLine());
            sender.sendMessage(lookup);

        } else {

            ChatBlock.sendMessage(sender, RED + lang("no.player.data.found", sender));

            if (isOtherPlayer() && senderUnion != null) {

                ChatBlock.sendBlank(sender);
                ChatBlock.sendMessage(sender, lang("kill.type.civilian", sender, DARK_GRAY));

            }

        }

    }

    @NotNull
    private String getUnionName() {

        String unionName = lang("none", sender);
        if (targetUnion != null) {

            unionName = lang("player.lookup.unionname").replace("%union_color_tag%", targetUnion.getColorTag())
                    .replace("%union_name%", targetUnion.getName());

        }

        return unionName;

    }

    @NotNull
    private String getPlayerStatus() {

        if (target == null || targetUnion == null) {

            return lang("free.agent", sender);

        }

        if (target.isTrusted()) {

            return sm.getColored(PAGE_TRUSTED_COLOR) + lang("trusted", sender);

        }

        return sm.getColored(PAGE_UNTRUSTED_COLOR) + lang("untrusted", sender);

    }

    @NotNull
    private String getKillTypeLine() {

        String killTypeLine = "";
        if (isOtherPlayer()) {

            String killType = GRAY + lang("neutral", sender);

            if (targetUnion == null) {

                killType = DARK_GRAY + lang("civilian", sender);

            } else if (senderUnion != null) {

                if (senderUnion.isRival(targetUnion.getTag())) {

                    killType = WHITE + lang("rival", sender);

                }

                if (senderUnion.equals(targetUnion) || senderUnion.isAlly(targetUnion.getTag())) {

                    killType = RED + lang("ally", sender);

                }

            }

            killTypeLine = lang("player.lookup.killtype", sender).replace("%player_kill_type%", killType);

        }

        return killTypeLine;

    }

    private boolean isPlayer() {

        return sender instanceof Player;

    }

    private boolean isOtherPlayer() {

        if (isPlayer()) {

            return !getPlayer().getUniqueId().equals(targetUuid);

        }

        return true;

    }

    private Player getPlayer() {

        return (Player) sender;

    }

}
