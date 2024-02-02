/* (C)2023 */
package org.transitclock.core.dataCache;

import com.querydsl.jpa.impl.JPAQuery;
import org.hibernate.Session;
import org.transitclock.domain.structs.ArrivalDeparture;
import org.transitclock.domain.structs.QArrivalDeparture;
import org.transitclock.service.dto.IpcArrivalDeparture;

import java.util.Date;
import java.util.List;

public interface StopArrivalDepartureCacheInterface {

    List<IpcArrivalDeparture> getStopHistory(StopArrivalDepartureCacheKey key);

    StopArrivalDepartureCacheKey putArrivalDeparture(ArrivalDeparture arrivalDeparture);

    default void populateCacheFromDb(Session session, Date startDate, Date endDate) {
        JPAQuery<ArrivalDeparture> query = new JPAQuery<>(session);
        var qentity = QArrivalDeparture.arrivalDeparture;
        List<ArrivalDeparture> results = query.from(qentity)
                .where(qentity.time.between(startDate,endDate))
                .orderBy(qentity.time.asc())
                .fetch();

        for (ArrivalDeparture result : results) {
            this.putArrivalDeparture(result);
            // TODO might be better with its own populateCacheFromdb
            getDwellTimeModelCacheInterface().addSample(result);
        }
    }

    DwellTimeModelCacheInterface getDwellTimeModelCacheInterface();
}
