package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.Clan;
import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.CLAN_MIN_SIZE_TO_SET_ALLY;
import static org.bukkit.ChatColor.RED;

@SuppressWarnings("unused")
public class MinimumToAllyCondition extends AbstractCommandCondition {

    public MinimumToAllyCondition(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context) throws InvalidCommandArgument {

        Clan clan = Conditions.assertClanMember(clanManager, context.getIssuer());
        if (clan.getSize() < settingsManager.getInt(CLAN_MIN_SIZE_TO_SET_ALLY)) {

            throw new ConditionFailedException(RED + lang("minimum.to.make.alliance", context.getIssuer(),
                    settingsManager.getInt(CLAN_MIN_SIZE_TO_SET_ALLY)));

        }

    }

    @Override
    public @NotNull String getId() {

        return "minimum_to_ally";

    }

}
