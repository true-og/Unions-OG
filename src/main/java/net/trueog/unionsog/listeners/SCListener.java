package net.trueog.unionsog.listeners;

import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.managers.SettingsManager.ConfigField;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class SCListener implements Listener {

    protected final UnionsOG plugin;

    public SCListener(UnionsOG plugin) {

        this.plugin = plugin;

    }

    public boolean isBlacklistedWorld(@NotNull Entity entity) {

        List<String> words = plugin.getSettingsManager().getStringList(ConfigField.BLACKLISTED_WORLDS);

        if (words.contains(entity.getWorld().getName())) {

            UnionsOG.debug("Blacklisted world");
            return true;

        }

        return false;

    }

}
