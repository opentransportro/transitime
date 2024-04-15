/* (C)2023 */
package org.transitclock.core.dataCache;

import java.util.Date;
import java.util.List;
import org.hibernate.Session;
import org.transitclock.domain.structs.ArrivalDeparture;
import org.transitclock.service.dto.IpcArrivalDeparture;

public interface TripDataHistoryCacheInterface {

    List<IpcArrivalDeparture> getTripHistory(TripKey tripKey);

    TripKey putArrivalDeparture(ArrivalDeparture arrivalDeparture);

    void populateCacheFromDb(Session session, Date startDate, Date endDate);

    IpcArrivalDeparture findPreviousArrivalEvent(
            List<IpcArrivalDeparture> arrivalDepartures, IpcArrivalDeparture current);

    IpcArrivalDeparture findPreviousDepartureEvent(
            List<IpcArrivalDeparture> arrivalDepartures, IpcArrivalDeparture current);

    List<TripKey> getKeys();
}
