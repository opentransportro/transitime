/* (C)2023 */
package org.transitclock.core.dataCache;

import org.transitclock.core.domain.ArrivalDeparture;
import org.transitclock.core.domain.Headway;

public interface DwellTimeModelCacheInterface {

    void addSample(ArrivalDeparture event, Headway headway, long dwellTime);

    void addSample(ArrivalDeparture departure);

    Long predictDwellTime(StopPathCacheKey cacheKey, Headway headway);
}
