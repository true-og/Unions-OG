package net.trueog.unionsog.commands.contexts;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.commands.UnionInput;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;
import static org.bukkit.ChatColor.RED;

@SuppressWarnings("unused")
public class UnionInputContextResolver extends AbstractInputOnlyContextResolver<UnionInput> {

    public UnionInputContextResolver(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public UnionInput getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {

        String arg = context.popFirstArg();
        Union union = unionManager.getUnion(arg);
        if (union == null) {

            throw new InvalidCommandArgument(RED + lang("the.union.does.not.exist", context.getSender()), false);

        }

        return new UnionInput(union);

    }

    @Override
    public Class<UnionInput> getType() {

        return UnionInput.class;

    }

}
