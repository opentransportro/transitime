/* (C)2023 */
package org.transitclock.core.dataCache;

import com.querydsl.jpa.impl.JPAQuery;
import org.hibernate.Session;
import org.transitclock.domain.structs.ArrivalDeparture;
import org.transitclock.domain.structs.Headway;
import org.transitclock.domain.structs.QArrivalDeparture;

import java.util.Date;
import java.util.List;

public interface DwellTimeModelCacheInterface {

    void addSample(ArrivalDeparture event, Headway headway, long dwellTime);

    void addSample(ArrivalDeparture departure);

    Long predictDwellTime(StopPathCacheKey cacheKey, Headway headway);

    void populateCacheFromDb(Session session, Date startDate, Date endDate);
}
