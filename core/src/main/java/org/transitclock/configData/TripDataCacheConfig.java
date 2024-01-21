package org.transitclock.configData;

import org.transitclock.config.IntegerConfigValue;
import org.transitclock.utils.Time;

public class TripDataCacheConfig {

    /** Default is 4 as we need 3 days worth for Kalman Filter implementation */
    public static final IntegerConfigValue tripDataCacheMaxAgeSec = new IntegerConfigValue(
            "transitclock.tripdatacache.tripDataCacheMaxAgeSec",
            15 * Time.SEC_PER_DAY,
            "How old an arrivaldeparture has to be before it is removed from the cache ");
}
