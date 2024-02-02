/* (C)2023 */
package org.transitclock.core.dataCache.ehcache.scheduled;

import com.querydsl.jpa.impl.JPAQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.hibernate.Session;
import org.transitclock.SingletonContainer;
import org.transitclock.core.dataCache.*;
import org.transitclock.domain.structs.ArrivalDeparture;
import org.transitclock.domain.structs.QArrivalDeparture;
import org.transitclock.domain.structs.Trip;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.gtfs.GtfsData;
import org.transitclock.service.dto.IpcArrivalDeparture;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Sean Og Crudden This is a Cache to hold historical arrival departure data for frequency
 *     based trips. It is intended to look up a trips historical data when a trip starts and place
 *     in cache for use in generating predictions based on a Kalman filter.
 */
@Slf4j
public class TripDataHistoryCache implements TripDataHistoryCacheInterface {
    private static final boolean debug = false;

    private static final String cacheByTrip = "arrivalDeparturesByTrip";

    private Cache<TripKey, TripEvents> cache = null;
    private final DbConfig dbConfig = SingletonContainer.getInstance(DbConfig.class);

    public TripDataHistoryCache() {
        CacheManager cm = SingletonContainer.getInstance(CacheManager.class);
        cache = cm.getCache(cacheByTrip, TripKey.class, TripEvents.class);
    }

    /* (non-Javadoc)
     * @see org.transitclock.core.dataCache.TripDataHistoryCacheInterface#getTripHistory(org.transitclock.core.dataCache.TripKey)
     */
    @Override
    public List<IpcArrivalDeparture> getTripHistory(TripKey tripKey) {

        TripEvents result = cache.get(tripKey);
        if (result != null) {
            return result.getEvents();
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.transitclock.core.dataCache.TripDataHistoryCacheInterface#putArrivalDeparture(org.transitclock.db.structs.ArrivalDeparture)
     */
    @Override
    public synchronized TripKey putArrivalDeparture(ArrivalDeparture arrivalDeparture) {

        logger.debug("Putting :{} in TripDataHistoryCache cache.", arrivalDeparture.toString());
        /* just put todays time in for last three days to aid development. This means it will kick in in 1 days rather than 3. Perhaps be a good way to start rather than using default transiTime method but I doubt it. */
        int days_back = 1;
        if (debug) days_back = 3;
        TripKey tripKey = null;

        for (int i = 0; i < days_back; i++) {
            Date nearestDay = DateUtils.truncate(new Date(arrivalDeparture.getTime()), Calendar.DAY_OF_MONTH);
            nearestDay = DateUtils.addDays(nearestDay, i * -1);

            Trip trip = dbConfig.getTrip(arrivalDeparture.getTripId());

            if (trip != null) {

                tripKey = new TripKey(arrivalDeparture.getTripId(), nearestDay, trip.getStartTime());

                TripEvents result = cache.get(tripKey);
                if (result == null) {
                    result = new TripEvents();
                }

                try {
                    result.addEvent(new IpcArrivalDeparture(arrivalDeparture));

                } catch (Exception e) {
                    logger.error("Error adding {} event to TripDataHistoryCache.", arrivalDeparture, e);
                }

                cache.put(tripKey, result);
            }
        }
        return tripKey;
    }

    /* (non-Javadoc)
     * @see org.transitclock.core.dataCache.TripDataHistoryCacheInterface#populateCacheFromDb(org.hibernate.Session, java.util.Date, java.util.Date)
     */

    @Override
    public void populateCacheFromDb(Session session, Date startDate, Date endDate) {
        JPAQuery<ArrivalDeparture> query = new JPAQuery<>(session);
        var qentity = QArrivalDeparture.arrivalDeparture;
        List<ArrivalDeparture> results = query.from(qentity)
                .where(qentity.time.between(startDate,endDate))
                .fetch();

        for (ArrivalDeparture result : results) {
            // TODO this might be better done in the database.
            if (GtfsData.routeNotFiltered(result.getRouteId())) {
                putArrivalDeparture(result);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.transitclock.core.dataCache.ehcache.test#findPreviousArrivalEvent(java.util.List, org.transitclock.db.structs.ArrivalDeparture)
     */
    @Override
    public IpcArrivalDeparture findPreviousArrivalEvent(
            List<IpcArrivalDeparture> arrivalDepartures, IpcArrivalDeparture current) {
        arrivalDepartures.sort(new IpcArrivalDepartureComparator());
        for (IpcArrivalDeparture tocheck : emptyIfNull(arrivalDepartures)) {
            if (tocheck.getStopId().equals(current.getStopId()) && (current.isDeparture() && tocheck.isArrival())) {
                return tocheck;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.transitclock.core.dataCache.ehcache.test#findPreviousDepartureEvent(java.util.List, org.transitclock.db.structs.ArrivalDeparture)
     */
    @Override
    public IpcArrivalDeparture findPreviousDepartureEvent(List<IpcArrivalDeparture> arrivalDepartures, IpcArrivalDeparture current) {
        arrivalDepartures.sort(new IpcArrivalDepartureComparator());
        for (IpcArrivalDeparture tocheck : emptyIfNull(arrivalDepartures)) {
            try {
                if (tocheck.getStopPathIndex() == (current.getStopPathIndex() - 1)
                        && (current.isArrival() && tocheck.isDeparture())) {
                    return tocheck;
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }

    private static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
        return iterable == null ? Collections.emptyList() : iterable;
    }

    @Override
    public List<TripKey> getKeys() {
        // TODO Auto-generated method stub
        return null;
    }
}
