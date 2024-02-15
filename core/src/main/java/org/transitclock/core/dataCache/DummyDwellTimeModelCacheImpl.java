package org.transitclock.core.dataCache;

import org.transitclock.domain.structs.ArrivalDeparture;
import org.transitclock.domain.structs.Headway;

public class DummyDwellTimeModelCacheImpl implements DwellTimeModelCacheInterface {
    @Override
    public void addSample(ArrivalDeparture event, Headway headway, long dwellTime) {
        // do nothing
    }

    @Override
    public void addSample(ArrivalDeparture departure) {
        // do nothing
    }

    @Override
    public Long predictDwellTime(StopPathCacheKey cacheKey, Headway headway) {
        return 0L;
    }
}
