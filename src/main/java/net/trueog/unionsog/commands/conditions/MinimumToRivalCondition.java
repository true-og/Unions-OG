package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.Clan;
import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.CLAN_MIN_SIZE_TO_SET_RIVAL;
import static org.bukkit.ChatColor.RED;

@SuppressWarnings("unused")
public class MinimumToRivalCondition extends AbstractCommandCondition {

    public MinimumToRivalCondition(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context) throws InvalidCommandArgument {

        BukkitCommandIssuer issuer = context.getIssuer();
        Clan clan = Conditions.assertClanMember(clanManager, issuer);
        if (clan.getSize() < settingsManager.getInt(CLAN_MIN_SIZE_TO_SET_RIVAL)) {

            throw new ConditionFailedException(
                    RED + lang("min.players.rivalries", issuer, settingsManager.getInt(CLAN_MIN_SIZE_TO_SET_RIVAL)));

        }

    }

    @Override
    public @NotNull String getId() {

        return "minimum_to_rival";

    }

}
