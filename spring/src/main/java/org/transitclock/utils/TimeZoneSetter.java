/* (C)2023 */
package org.transitclock.utils;

import org.transitclock.config.StringConfigValue;

import java.util.TimeZone;

/**
 * For setting timezone for application. Ideally would get timezone from the agency db but once a
 * Hibernate session factory is created, such as for reading timezone from db, then it is too late
 * to set the timezone. Therefore this provides ability to set it manually.
 *
 * @author Michael
 */
public class TimeZoneSetter {
    public static String getTimezone() {
        return timezone.getValue();
    }

    private static StringConfigValue timezone = new StringConfigValue(
            "transitclock.core.timezone",
            "For setting timezone for application. Ideally would get "
                    + "timezone from the agency db but once a Hibernate "
                    + "session factory is created, such as for reading "
                    + "timezone from db, then it is too late to set the "
                    + "timezone. Therefore this provides ability to set it "
                    + "manually.");

    /**
     * For setting timezone for application to name specified by the Java property
     * transitclock.core.timezone. Ideally would get timezone from the agency db but once a
     * Hibernate session factory is created, such as for reading timezone from db, then it is too
     * late to set the timezone. Therefore this provides ability to set it manually.
     */
    public static void setTimezone() {
        String timezoneStr = timezone.getValue();
        if (timezoneStr != null) {
            TimeZone.setDefault(TimeZone.getTimeZone(timezoneStr));
        }
    }
}
