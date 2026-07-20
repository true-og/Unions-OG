package net.trueog.unionsog.commands.completions;

import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.managers.ClanManager;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractCompletion implements IdentifiableCompletion {

    protected final UnionsOG plugin;
    protected final ClanManager clanManager;

    public AbstractCompletion(@NotNull UnionsOG plugin) {

        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();

    }

}
