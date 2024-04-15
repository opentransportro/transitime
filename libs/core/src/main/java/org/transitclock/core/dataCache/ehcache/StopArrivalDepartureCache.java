/* (C)2023 */
package org.transitclock.core.dataCache.ehcache;

import com.querydsl.jpa.impl.JPAQuery;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.hibernate.Session;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheInterface;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheKey;
import org.transitclock.core.dataCache.StopEvents;
import org.transitclock.domain.structs.ArrivalDeparture;
import org.transitclock.domain.structs.QArrivalDeparture;
import org.transitclock.service.dto.IpcArrivalDeparture;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Sean Og Crudden This is a Cache to hold a sorted list of all arrival departure events for
 *     each stop in a cache. We can use this to look up all event for a stop for a day. The date
 *     used in the key should be the start of the day concerned.
 *     <p>TODO this could do with an interface, factory class, and alternative implementations,
 *     perhaps using Infinispan.
 */
@Slf4j
public class StopArrivalDepartureCache implements StopArrivalDepartureCacheInterface {
    private static final String cacheByStop = "arrivalDeparturesByStop";
    private final Cache<StopArrivalDepartureCacheKey, StopEvents> cache;

    public StopArrivalDepartureCache(CacheManager cm) {
        cache = cm.getCache(cacheByStop, StopArrivalDepartureCacheKey.class, StopEvents.class);
    }


    public synchronized List<IpcArrivalDeparture> getStopHistory(StopArrivalDepartureCacheKey key) {
        Calendar date = Calendar.getInstance();
        date.setTime(key.getDate());

        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        key.setDate(date.getTime());
        StopEvents result = cache.get(key);

        if (result != null) {
            return result.getEvents();
        } else {
            return null;
        }
    }


    public synchronized StopArrivalDepartureCacheKey putArrivalDeparture(ArrivalDeparture arrivalDeparture) {

        logger.debug("Putting :{} in StopArrivalDepartureCache cache.", arrivalDeparture.toString());

        Calendar date = Calendar.getInstance();
        date.setTime(arrivalDeparture.getDate());

        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        if (arrivalDeparture.getStopId() != null) {

            StopArrivalDepartureCacheKey key =
                    new StopArrivalDepartureCacheKey(arrivalDeparture.getStopId(), date.getTime());

            StopEvents element = cache.get(key);

            if (element == null) {
                element = new StopEvents();
            }

            try {
                element.addEvent(new IpcArrivalDeparture(arrivalDeparture));
            } catch (Exception e) {
                logger.error("Error adding {} event to StopArrivalDepartureCache.", arrivalDeparture, e);
            }

            cache.put(key, element);

            return key;
        } else {
            return null;
        }
    }

    public void populateCacheFromDb(Session session, Date startDate, Date endDate) {
        JPAQuery<ArrivalDeparture> query = new JPAQuery<>(session);
        var qentity = QArrivalDeparture.arrivalDeparture;
        List<ArrivalDeparture> results = query.from(qentity)
                .where(qentity.time.between(startDate,endDate))
                .orderBy(qentity.time.asc())
                .fetch();

        for (ArrivalDeparture result : results) {
            putArrivalDeparture(result);
        }
    }
}
