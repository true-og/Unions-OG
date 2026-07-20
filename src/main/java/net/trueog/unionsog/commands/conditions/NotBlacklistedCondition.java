package net.trueog.unionsog.commands.conditions;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.UnionsOG;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.managers.SettingsManager.ConfigField.BLACKLISTED_WORLDS;

@SuppressWarnings("unused")
public class NotBlacklistedCondition extends AbstractCommandCondition {

    public NotBlacklistedCondition(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context) throws InvalidCommandArgument {

        Player player = context.getIssuer().getPlayer();
        if (player != null) {

            World world = player.getLocation().getWorld();
            if (world != null) {

                if (settingsManager.getStringList(BLACKLISTED_WORLDS).contains(world.getName())) {

                    throw new ConditionFailedException();

                }

            }

        }

    }

    @Override
    public @NotNull String getId() {

        return "not_blacklisted";

    }

}