package net.trueog.unionsog.commands;

import net.trueog.unionsog.ChatBlock;
import net.trueog.unionsog.UnionsOG;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.COMMANDS_UNION;

/**
 * Points players at {@code /union} when they reach for another server's word
 * for the same thing, such as {@code /clan} or {@code /guilds}.
 * <p>
 * These are registered as plain Bukkit commands rather than command aliases so
 * they never execute anything: the player is told what to type instead, with
 * their own arguments carried over.
 * </p>
 */
public class GroupAliasCommand implements CommandExecutor, TabCompleter {

    /**
     * Words other group plugins use for a union. Deliberately excludes {@code f},
     * {@code faction} and {@code guild}, which Utilities-OG already redirects.
     */
    public static final List<String> ALIASES = Collections.unmodifiableList(
            Arrays.asList("clan", "clans", "factions", "guilds", "party", "tribe", "nation", "crew", "squad", "gang"));

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            String[] args)
    {

        String union = UnionsOG.getInstance().getSettingsManager().getString(COMMANDS_UNION);
        String arguments = args.length == 0 ? "" : " " + String.join(" ", args);

        ChatBlock.sendMessage(sender, lang("use.union.instead", sender, union + arguments, label));
        return true;

    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            String[] args)
    {

        return Collections.emptyList();

    }

}
