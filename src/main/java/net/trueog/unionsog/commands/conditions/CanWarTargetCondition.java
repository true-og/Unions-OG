package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.*;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.commands.UnionInput;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.WAR_MAX_MEMBERS_DIFFERENCE;

public class CanWarTargetCondition extends AbstractParameterCondition<UnionInput> {

    public CanWarTargetCondition(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public Class<UnionInput> getType() {

        return UnionInput.class;

    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context,
            BukkitCommandExecutionContext execContext, UnionInput target) throws InvalidCommandArgument
    {

        BukkitCommandIssuer issuer = execContext.getIssuer();
        Union issuerUnion = Conditions.assertUnionMember(unionManager, issuer);
        Union targetUnion = target.getUnion();

        if (!issuerUnion.isRival(targetUnion.getTag())) {

            throw new ConditionFailedException(lang("you.can.only.start.war.with.rivals", issuer));

        }

        if (issuerUnion.isWarring(targetUnion)) {

            throw new ConditionFailedException(lang("unions.already.at.war", issuer));

        }

        // War no longer needs the target's consent, so the size guard always applies.
        int maxDifference = settingsManager.getInt(WAR_MAX_MEMBERS_DIFFERENCE);

        if (maxDifference >= 0) {

            int difference = Math.abs(issuerUnion.getOnlineMembers().size() - targetUnion.getOnlineMembers().size());
            if (difference > maxDifference)
                throw new ConditionFailedException(lang("you.cant.start.war.online.members.difference", issuer));

        }

    }

    @Override
    public @NotNull String getId() {

        return "can_war_target";

    }

}
