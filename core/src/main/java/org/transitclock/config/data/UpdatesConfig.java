package org.transitclock.config.data;

import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.IntegerConfigValue;

public class UpdatesConfig {

    public static boolean pageDbReads() {
        return pageDbReads.getValue();
    }

    public static BooleanConfigValue pageDbReads = new BooleanConfigValue(
            "transitclock.updates.pageDbReads",
            true,
            "page database reads to break up long reads. " + "It may impact performance on MySql");

    public static Integer pageSize() {
        return pageSize.getValue();
    }

    public static IntegerConfigValue pageSize =
            new IntegerConfigValue("transitclock.updates.pageSize", 50000, "Number of records to read in at a time");

}
