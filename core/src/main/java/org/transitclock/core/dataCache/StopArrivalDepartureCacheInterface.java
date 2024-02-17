/* (C)2023 */
package org.transitclock.core.dataCache;

import org.hibernate.Session;
import org.transitclock.domain.structs.ArrivalDeparture;
import org.transitclock.service.dto.IpcArrivalDeparture;

import java.util.Date;
import java.util.List;

public interface StopArrivalDepartureCacheInterface {

    public List<IpcArrivalDeparture> getStopHistory(StopArrivalDepartureCacheKey key);

    public StopArrivalDepartureCacheKey putArrivalDeparture(ArrivalDeparture arrivalDeparture);

    public void populateCacheFromDb(Session session, Date startDate, Date endDate);
}
