package net.trueog.unionsog.commands.conditions;

import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.managers.*;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractCondition implements IdentifiableCondition {

    protected final UnionsOG plugin;
    protected final PermissionsManager permissionsManager;
    protected final ClanManager clanManager;
    protected final RequestManager requestManager;
    protected final SettingsManager settingsManager;
    protected final ProtectionManager protectionManager;

    public AbstractCondition(@NotNull UnionsOG plugin) {

        this.plugin = plugin;
        permissionsManager = plugin.getPermissionsManager();
        clanManager = plugin.getClanManager();
        requestManager = plugin.getRequestManager();
        settingsManager = plugin.getSettingsManager();
        protectionManager = plugin.getProtectionManager();

    }

}
