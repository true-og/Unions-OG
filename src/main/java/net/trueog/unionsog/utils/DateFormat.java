package net.trueog.unionsog.utils;

import net.trueog.unionsog.UnionsOG;

import java.text.SimpleDateFormat;
import java.util.Date;

import static net.trueog.unionsog.managers.SettingsManager.ConfigField.DATE_TIME_PATTERN;

public class DateFormat {

    private static final UnionsOG plugin = UnionsOG.getInstance();
    private static SimpleDateFormat format;

    static {

        String pattern = plugin.getSettingsManager().getString(DATE_TIME_PATTERN);
        try {

            format = new SimpleDateFormat(pattern);

        } catch (IllegalArgumentException ex) {

            plugin.getLogger().warning(String.format("%s is not a valid pattern!", (pattern)));
            format = new SimpleDateFormat("HH:mm - dd/MM/yyyy");

        }

    }

    public static String formatDateTime(long date) {

        return format.format(new Date(date));

    }

}
