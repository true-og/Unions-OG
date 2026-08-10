package net.trueog.unionsog.commands.contexts;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.managers.UnionManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static co.aikar.commands.MessageKeys.*;
import static net.trueog.unionsog.UnionsOG.lang;

public class Contexts {

    private Contexts() {

    }

    @NotNull
    public static Union assertUnionMember(@NotNull UnionManager unionManager, @NotNull BukkitCommandIssuer issuer) {

        assertPlayer(issuer);
        Union union = unionManager.getUnionByPlayerUniqueId(issuer.getUniqueId());
        if (union == null) {

            throw new InvalidCommandArgument(lang("not.a.member.of.any.union", issuer), false);

        }

        return union;

    }

    @NotNull
    public static Player assertPlayer(@NotNull BukkitCommandIssuer issuer) {

        Player player = issuer.getPlayer();
        if (player == null) {

            throw new InvalidCommandArgument(NOT_ALLOWED_ON_CONSOLE, false);

        }

        return player;

    }

    public static void validateMinMax(Number val, Number minValue, Number maxValue) throws InvalidCommandArgument {

        if (maxValue != null && val.doubleValue() > maxValue.doubleValue()) {

            throw new InvalidCommandArgument(PLEASE_SPECIFY_AT_MOST, "{max}", String.valueOf(maxValue));

        }

        if (minValue != null && val.doubleValue() < minValue.doubleValue()) {

            throw new InvalidCommandArgument(PLEASE_SPECIFY_AT_LEAST, "{min}", String.valueOf(minValue));

        }

    }

}
