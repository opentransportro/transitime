/* (C)2023 */
package org.transitclock.core.dataCache;

import org.hibernate.Session;
import org.transitclock.core.domain.ArrivalDeparture;
import org.transitclock.ipc.data.IpcArrivalDeparture;

import java.util.Date;
import java.util.List;

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
