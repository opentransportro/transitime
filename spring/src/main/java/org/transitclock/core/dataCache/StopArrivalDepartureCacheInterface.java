/* (C)2023 */
package org.transitclock.core.dataCache;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.transitclock.core.domain.ArrivalDeparture;
import org.transitclock.ipc.data.IpcArrivalDeparture;

import java.util.Date;
import java.util.List;

public abstract class StopArrivalDepartureCacheInterface {

    public abstract List<IpcArrivalDeparture> getStopHistory(StopArrivalDepartureCacheKey key);

    public abstract StopArrivalDepartureCacheKey putArrivalDeparture(ArrivalDeparture arrivalDeparture);

    public void populateCacheFromDb(Session session, Date startDate, Date endDate) {
        Criteria criteria = session.createCriteria(ArrivalDeparture.class);

        @SuppressWarnings("unchecked")
        List<ArrivalDeparture> results = criteria.add(Restrictions.between("time", startDate, endDate))
                .addOrder(Order.asc("time"))
                .list();

        for (ArrivalDeparture result : results) {
            this.putArrivalDeparture(result);
            // TODO might be better with its own populateCacheFromdb
            DwellTimeModelCacheFactory.getInstance().addSample(result);
        }
    }
}
