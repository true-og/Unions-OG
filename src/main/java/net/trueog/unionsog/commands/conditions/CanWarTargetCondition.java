package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.*;
import net.trueog.unionsog.Clan;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.commands.ClanInput;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.WAR_MAX_MEMBERS_DIFFERENCE;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.WAR_START_REQUEST_ENABLED;

public class CanWarTargetCondition extends AbstractParameterCondition<ClanInput> {

    public CanWarTargetCondition(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public Class<ClanInput> getType() {

        return ClanInput.class;

    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context,
            BukkitCommandExecutionContext execContext, ClanInput target) throws InvalidCommandArgument
    {

        BukkitCommandIssuer issuer = execContext.getIssuer();
        Clan issuerClan = Conditions.assertClanMember(clanManager, issuer);
        Clan targetClan = target.getClan();

        if (!issuerClan.isRival(targetClan.getTag())) {

            throw new ConditionFailedException(lang("you.can.only.start.war.with.rivals", issuer));

        }

        if (issuerClan.isWarring(targetClan)) {

            throw new ConditionFailedException(lang("clans.already.at.war", issuer));

        }

        boolean isWarRequestEnabled = settingsManager.is(WAR_START_REQUEST_ENABLED);
        int maxDifference = settingsManager.getInt(WAR_MAX_MEMBERS_DIFFERENCE);

        if (!isWarRequestEnabled && maxDifference >= 0) {

            int difference = Math.abs(issuerClan.getOnlineMembers().size() - targetClan.getOnlineMembers().size());
            if (difference > maxDifference)
                throw new ConditionFailedException(lang("you.cant.start.war.online.members.difference", issuer));

        }

    }

    @Override
    public @NotNull String getId() {

        return "can_war_target";

    }

}
