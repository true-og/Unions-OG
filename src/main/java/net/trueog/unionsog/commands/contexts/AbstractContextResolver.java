package net.trueog.unionsog.commands.contexts;

import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.managers.ClanManager;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractContextResolver<T> {

    protected final @NotNull UnionsOG plugin;
    protected final @NotNull ClanManager clanManager;

    public AbstractContextResolver(@NotNull UnionsOG plugin) {

        this.plugin = plugin;
        clanManager = plugin.getClanManager();

    }

    public abstract Class<T> getType();

}
