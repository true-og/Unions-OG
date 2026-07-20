package net.trueog.unionsog.tasks;

import net.trueog.unionsog.UnionsOG;
import org.bukkit.scheduler.BukkitRunnable;

import static net.trueog.unionsog.managers.SettingsManager.ConfigField.PERFORMANCE_SAVE_INTERVAL;

/**
 * 
 * @author RoinujNosde
 * @since 2.10.2
 *
 */
public class SaveDataTask extends BukkitRunnable {

    UnionsOG plugin = UnionsOG.getInstance();

    /**
     * Starts the repetitive task
     */
    public void start() {

        long interval = plugin.getSettingsManager().getMinutes(PERFORMANCE_SAVE_INTERVAL);
        runTaskTimerAsynchronously(plugin, interval, interval);

    }

    @Override
    public void run() {

        plugin.getStorageManager().saveModified();

    }

}
