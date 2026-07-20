package net.trueog.unionsog.commands.data;

import net.trueog.unionsog.Clan;
import net.trueog.unionsog.Helper;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.utils.KDRFormat;
import net.trueog.unionsog.utils.VanishUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.PAGE_LEADER_COLOR;

public class ClanProfile extends Sendable {

    private final Clan clan;

    public ClanProfile(@NotNull UnionsOG plugin, @NotNull CommandSender sender, @NotNull Clan clan) {

        super(plugin, sender);
        this.clan = clan;

    }

    @Override
    public void send() {

        String message = lang("clan.profile").replace("%clan_name%", clan.getName())
                .replace("%clan_color_tag%", clan.getColorTag()).replace("%clan_description%", getDescription())
                .replace("%clan_status%", Helper.getFormattedClanStatus(clan, sender))
                .replace("%clan_leaders%", clan.getLeadersString(sm.getColored(PAGE_LEADER_COLOR), subColor + ", "))
                .replace("%clan_online_count%", String.valueOf(VanishUtils.getNonVanished(sender, clan).size()))
                .replace("%clan_size%", String.valueOf(clan.getSize()))
                .replace("%clan_kdr%", KDRFormat.format(clan.getTotalKDR()))
                .replace("%clan_rival_kills%", String.valueOf(clan.getTotalRival()))
                .replace("%clan_neutral_kills%", String.valueOf(clan.getTotalNeutral()))
                .replace("%clan_civilian_kills%", String.valueOf(clan.getTotalCivilian()))
                .replace("%clan_ally_kills%", String.valueOf(clan.getTotalAlly()))
                .replace("%clan_deaths%", String.valueOf(clan.getTotalDeaths()))
                .replace("%clan_allies%", clan.getAllyString(subColor + ", ", sender))
                .replace("%clan_rivals%", clan.getRivalString(subColor + ", ", sender))
                .replace("%clan_founded%", clan.getFoundedString())
                .replace("%clan_inactive_days%", String.valueOf(clan.getInactiveDays()))
                .replace("%clan_max_inactive_days%", Helper.formatMaxInactiveDays(clan.getMaxInactiveDays()));
        sender.sendMessage(message);

    }

    @NotNull
    private String getDescription() {

        return clan.getDescription() != null && !clan.getDescription().isEmpty() ? clan.getDescription()
                : lang("no.description", sender);

    }

}
