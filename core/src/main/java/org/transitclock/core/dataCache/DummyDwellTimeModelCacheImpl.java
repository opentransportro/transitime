package org.transitclock.core.dataCache;

import org.hibernate.Session;
import org.transitclock.domain.structs.ArrivalDeparture;
import org.transitclock.domain.structs.Headway;

import java.util.Date;

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

    @Override
    public void populateCacheFromDb(Session session, Date startDate, Date endDate) {
        // do nothing
    }
}
