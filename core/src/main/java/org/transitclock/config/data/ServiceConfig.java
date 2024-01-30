package org.transitclock.config.data;

import org.transitclock.config.IntegerConfigValue;
import org.transitclock.utils.Time;

public class ServiceConfig {


    public static IntegerConfigValue minutesIntoMorningToIncludePreviousServiceIds = new IntegerConfigValue(
            "transitclock.service.minutesIntoMorningToIncludePreviousServiceIds",
            4 * Time.HOUR_IN_MINS,
            "Early in the morning also want to include at service IDs "
                    + "for previous day since a block might have started on "
                    + "that day. But don't want to always include previous day "
                    + "service IDs since that confuses things. Therefore just "
                    + "include them if before this time of the day, in minutes.");

}
