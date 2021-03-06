/**
 *
 */
package org.transitclock.core.dataCache.ehcache.frequency;

import org.apache.commons.lang3.time.DateUtils;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.core.dataCache.*;
import org.transitclock.core.dataCache.ehcache.CacheManagerFactory;
import org.transitclock.core.dataCache.frequency.FrequencyBasedHistoricalAverageCache;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.Trip;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.gtfs.GtfsData;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.utils.Time;

import java.util.*;

/**
 * @author Sean Og Crudden 
 * 		   This is a Cache to hold historical arrival departure data for trips. It
 *         is intended to look up a trips historical data when a trip starts and
 *         place in cache for use in generating predictions based on a Kalman
 *         filter. Uses Ehcache for caching rather than just using a concurrent
 *         hashmap. This approach to holding data in memory for transitime needs
 *         to be proven.
 *
 *         TODO this could do with an interface, factory class, and alternative implementations, perhaps using Infinispan.
 */
public class TripDataHistoryCache implements TripDataHistoryCacheInterface {
    private static final TripDataHistoryCacheInterface singleton = new TripDataHistoryCache();

    private static final boolean debug = false;

    final private static String cacheByTrip = "arrivalDeparturesByTrip";

    private static final Logger logger = LoggerFactory
            .getLogger(TripDataHistoryCache.class);
    /**
     * Default is 4 as we need 3 days worth for Kalman Filter implementation
     */
    private static final IntegerConfigValue tripDataCacheMaxAgeSec = new IntegerConfigValue(
            "transitclock.tripdatacache.tripDataCacheMaxAgeSec",
            15 * Time.SEC_PER_DAY,
            "How old an arrivaldeparture has to be before it is removed from the cache ");
    private Cache<TripKey, TripEvents> cache = null;

    public TripDataHistoryCache() {
        CacheManager cm = CacheManagerFactory.getInstance();


        cache = cm.getCache(cacheByTrip, TripKey.class, TripEvents.class);
    }

    /**
     * Gets the singleton instance of this class.
     *
     * @return
     */
    public static TripDataHistoryCacheInterface getInstance() {
        return singleton;
    }

    private static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
        return iterable == null ? Collections.emptyList() : iterable;
    }

    public void logCache(Logger logger) {
        logger.debug("Cache content log. Not implemented.");

    }

    /* (non-Javadoc)
     * @see org.transitclock.core.dataCache.TripDataHistoryCacheInterface#getTripHistory(org.transitclock.core.dataCache.TripKey)
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<IpcArrivalDeparture> getTripHistory(TripKey tripKey) {

        //logger.debug(cache.toString());
        logger.debug("Looking for TripDataHistoryCache cache element using key {}.", tripKey);

        TripEvents result = cache.get(tripKey);

        if (result != null) {
            logger.debug("Found TripDataHistoryCache cache element using key {}.", tripKey);
            return result.getEvents();
        } else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.transitclock.core.dataCache.TripDataHistoryCacheInterface#populateCacheFromDb(org.hibernate.Session, java.util.Date, java.util.Date)
     */

    /* (non-Javadoc)
     * @see org.transitclock.core.dataCache.TripDataHistoryCacheInterface#putArrivalDeparture(org.transitclock.db.structs.ArrivalDeparture)
     */
    @Override
    @SuppressWarnings("unchecked")
    synchronized public TripKey putArrivalDeparture(ArrivalDeparture arrivalDeparture) {

        Block block = null;
        if (arrivalDeparture.getBlock() == null) {
            DbConfig dbConfig = Core.getInstance().getDbConfig();
            block = dbConfig.getBlock(arrivalDeparture.getServiceId(), arrivalDeparture.getBlockId());
        } else {
            block = arrivalDeparture.getBlock();
        }

        /* just put todays time in for last three days to aid development. This means it will kick in in 1 days rather than 3. Perhaps be a good way to start rather than using default transiTime method but I doubt it. */
        int days_back = 1;
        if (debug)
            days_back = 3;
        TripKey tripKey = null;

        for (int i = 0; i < days_back; i++) {
            Date nearestDay = DateUtils.truncate(new Date(arrivalDeparture.getTime()), Calendar.DAY_OF_MONTH);

            nearestDay = DateUtils.addDays(nearestDay, i * -1);

            DbConfig dbConfig = Core.getInstance().getDbConfig();

            Trip trip = dbConfig.getTrip(arrivalDeparture.getTripId());

            // TODO need to set start time based on start of bucket
            if (arrivalDeparture.getFreqStartTime() != null) {
                Integer time = FrequencyBasedHistoricalAverageCache.secondsFromMidnight(arrivalDeparture.getFreqStartTime(), 2);

                time = FrequencyBasedHistoricalAverageCache.round(time, FrequencyBasedHistoricalAverageCache.getCacheIncrementsForFrequencyService());

                if (trip != null) {
                    tripKey = new TripKey(arrivalDeparture.getTripId(),
                            nearestDay,
                            time);

                    logger.debug("Putting :{} in TripDataHistoryCache cache using key {}.", arrivalDeparture, tripKey);

                    List<IpcArrivalDeparture> list = null;

                    TripEvents element = cache.get(tripKey);

                    if (element != null && element.getEvents() != null) {
                        list = element.getEvents();
                        cache.remove(tripKey);
                    } else {
                        list = new ArrayList<IpcArrivalDeparture>();
                    }

                    try {
                        list.add(new IpcArrivalDeparture(arrivalDeparture));
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    element.setEvents(list);

                    cache.put(tripKey, element);
                }
            } else {
                logger.error("Cannot add event to TripDataHistoryCache as it has no freqStartTime set. {}", arrivalDeparture);
            }
        }
        return tripKey;
    }

    @Override
    public void populateCacheFromDb(Session session, Date startDate, Date endDate) {
        Criteria criteria = session.createCriteria(ArrivalDeparture.class);

        @SuppressWarnings("unchecked")
        List<ArrivalDeparture> results = criteria.add(Restrictions.between("time", startDate, endDate)).list();

        for (ArrivalDeparture result : results) {
            // TODO this might be better done in the database.
            if (GtfsData.routeNotFiltered(result.getRouteId())) {
                TripDataHistoryCacheFactory.getInstance().putArrivalDeparture(result);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.transitclock.core.dataCache.ehcache.test#findPreviousArrivalEvent(java.util.List, org.transitclock.db.structs.ArrivalDeparture)
     */
    @Override
    public IpcArrivalDeparture findPreviousArrivalEvent(List<IpcArrivalDeparture> arrivalDepartures, IpcArrivalDeparture current) {
        Collections.sort(arrivalDepartures, new IpcArrivalDepartureComparator());
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
        Collections.sort(arrivalDepartures, new IpcArrivalDepartureComparator());
        for (IpcArrivalDeparture tocheck : emptyIfNull(arrivalDepartures)) {
            try {

                if (tocheck.getStopPathIndex() == (current.getStopPathIndex() - 1)
                        && (current.isArrival() && tocheck.isDeparture())
                        && current.getFreqStartTime().equals(tocheck.getFreqStartTime())) {
                    return tocheck;
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public List<TripKey> getKeys() {
        // TODO Auto-generated method stub
        return null;
    }

}
