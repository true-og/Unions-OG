package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.MessageKeys;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.managers.UnionManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;

public class Conditions {

    private Conditions() {

    }

    @NotNull
    public static Union assertUnionMember(@NotNull UnionManager unionManager, @NotNull BukkitCommandIssuer issuer) {

        Conditions.assertPlayer(issuer);
        Union union = unionManager.getUnionByPlayerUniqueId(issuer.getUniqueId());
        if (union == null) {

            throw new ConditionFailedException(lang("not.a.member.of.any.union", issuer));

        }

        return union;

    }

    @NotNull
    public static Player assertPlayer(@NotNull BukkitCommandIssuer issuer) {

        Player player = issuer.getPlayer();
        if (player == null) {

            throw new ConditionFailedException(MessageKeys.NOT_ALLOWED_ON_CONSOLE);

        }

        return player;

    }

}
