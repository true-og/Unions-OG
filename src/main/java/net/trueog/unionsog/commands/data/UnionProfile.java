package net.trueog.unionsog.commands.data;

import net.trueog.unionsog.Union;
import net.trueog.unionsog.Helper;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.utils.KDRFormat;
import net.trueog.unionsog.utils.VanishUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;

public class UnionProfile extends Sendable {

    private final Union union;

    public UnionProfile(@NotNull UnionsOG plugin, @NotNull CommandSender sender, @NotNull Union union) {

        super(plugin, sender);
        this.union = union;

    }

    @Override
    public void send() {

        String message = lang("union.profile").replace("%union_name%", union.getName())
                .replace("%union_color_tag%", union.getColorTag()).replace("%union_description%", getDescription())
                .replace("%union_status%", Helper.getFormattedUnionStatus(union, sender))
                .replace("%union_online_count%", String.valueOf(VanishUtils.getNonVanished(sender, union).size()))
                .replace("%union_size%", String.valueOf(union.getSize()))
                .replace("%union_kdr%", KDRFormat.format(union.getTotalKDR()))
                .replace("%union_rival_kills%", String.valueOf(union.getTotalRival()))
                .replace("%union_neutral_kills%", String.valueOf(union.getTotalNeutral()))
                .replace("%union_civilian_kills%", String.valueOf(union.getTotalCivilian()))
                .replace("%union_ally_kills%", String.valueOf(union.getTotalAlly()))
                .replace("%union_deaths%", String.valueOf(union.getTotalDeaths()))
                .replace("%union_allies%", union.getAllyString(subColor + ", ", sender))
                .replace("%union_rivals%", union.getRivalString(subColor + ", ", sender))
                .replace("%union_founded%", union.getFoundedString())
                .replace("%union_inactive_days%", String.valueOf(union.getInactiveDays()))
                .replace("%union_max_inactive_days%", Helper.formatMaxInactiveDays(union.getMaxInactiveDays()));
        sender.sendMessage(message);

    }

    @NotNull
    private String getDescription() {

        return union.getDescription() != null && !union.getDescription().isEmpty() ? union.getDescription()
                : lang("no.description", sender);

    }

}
