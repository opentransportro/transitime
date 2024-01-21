package org.transitclock.configData;

import org.transitclock.config.BooleanConfigValue;

public class FormattingConfig {

    public static final BooleanConfigValue capitalize = new BooleanConfigValue(
            "transitclock.gtfs.capitalize",
            false,
            "Sometimes GTFS titles have all capital letters or other "
                    + "capitalization issues. If set to true then will properly "
                    + "capitalize titles when process GTFS data. But note that "
                    + "this can require using regular expressions to fix things "
                    + "like acronyms that actually should be all caps.");

}
