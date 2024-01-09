/* (C)2023 */
package org.transitclock.core.dataCache;

import java.util.Date;
import java.util.List;

import com.querydsl.jpa.impl.JPAQuery;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.QArrivalDeparture;
import org.transitclock.ipc.data.IpcArrivalDeparture;

public abstract class StopArrivalDepartureCacheInterface {

    public abstract List<IpcArrivalDeparture> getStopHistory(StopArrivalDepartureCacheKey key);

    public abstract StopArrivalDepartureCacheKey putArrivalDeparture(ArrivalDeparture arrivalDeparture);

    public void populateCacheFromDb(Session session, Date startDate, Date endDate) {
        JPAQuery<ArrivalDeparture> query = new JPAQuery<>(session);
        var qentity = QArrivalDeparture.arrivalDeparture;
        List<ArrivalDeparture> results = query.from(qentity)
                .where(qentity.time.between(startDate,endDate))
                .orderBy(qentity.time.asc())
                .fetch();

        for (ArrivalDeparture result : results) {
            this.putArrivalDeparture(result);
            // TODO might be better with its own populateCacheFromdb
            DwellTimeModelCacheFactory.getInstance().addSample(result);
        }
    }
}
