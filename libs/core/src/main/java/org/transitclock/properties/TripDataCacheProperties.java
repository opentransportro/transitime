package org.transitclock.properties;

import lombok.Data;

@Data
public class TripDataCacheProperties {
    // config param: transitclock.tripdatacache.tripDataCacheMaxAgeSec
    // How old an arrivaldeparture has to be before it is removed from the cache
    private Integer tripDataCacheMaxAgeSec = 1296000;
}
