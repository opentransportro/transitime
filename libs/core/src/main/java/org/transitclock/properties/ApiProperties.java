package org.transitclock.properties;

import lombok.Data;

@Data
public class ApiProperties {
    // config param: transitclock.api.gtfsRtCacheSeconds
    // How long to cache GTFS Realtime
    private Integer gtfsRtCacheSeconds = 15;

    // config param: transitclock.api.predictionMaxFutureSecs
    // Number of seconds in the future to accept predictions before
    private Integer predictionMaxFutureSecs = 3600;

    // config param: transitclock.api.includeTripUpdateDelay
    // Whether to include delay in the TripUpdate message
    private Boolean includeTripUpdateDelay = false;

}
