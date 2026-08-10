package net.trueog.unionsog.commands.contexts;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.MinecraftMessageKeys;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.commands.UnionPlayerInput;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.USERNAME_REGEX;

@SuppressWarnings("unused")
public class UnionPlayerInputContextResolver extends AbstractInputOnlyContextResolver<UnionPlayerInput> {

    private final Pattern validUsername;

    public UnionPlayerInputContextResolver(@NotNull UnionsOG plugin) {

        super(plugin);
        validUsername = Pattern.compile(plugin.getSettingsManager().getString(USERNAME_REGEX));

    }

    @Override
    public UnionPlayerInput getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {

        String arg = context.popFirstArg();
        if (!validUsername.matcher(arg).matches()) {

            throw new InvalidCommandArgument(MinecraftMessageKeys.IS_NOT_A_VALID_NAME, "{name}", arg);

        }

        UnionPlayer cp = unionManager.getAnyUnionPlayer(arg);
        if (cp == null) {

            Player player = Bukkit.getPlayer(arg);
            if (player == null) {

                throw new InvalidCommandArgument(lang("user.hasnt.played.before", context.getSender()));

            }

            cp = unionManager.getCreateUnionPlayer(player.getUniqueId());

        }

        return new UnionPlayerInput(cp);

    }

    @Override
    public Class<UnionPlayerInput> getType() {

        return UnionPlayerInput.class;

    }

}
