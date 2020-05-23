/**
 *
 */
package org.transitclock.ipc.servers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.dataCache.*;
import org.transitclock.core.dataCache.frequency.FrequencyBasedHistoricalAverageCache;
import org.transitclock.core.dataCache.scheduled.ScheduleBasedHistoricalAverageCache;
import org.transitclock.ipc.data.*;
import org.transitclock.ipc.interfaces.CacheQueryInterface;
import org.transitclock.ipc.rmi.AbstractServer;

import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * @author Sean Og Crudden Server to allow cache content to be queried.
 */
public class CacheQueryServer extends AbstractServer implements CacheQueryInterface {
    private static final Logger logger = LoggerFactory.getLogger(CacheQueryServer.class);
    // Should only be accessed as singleton class
    private static CacheQueryServer singleton;

    protected CacheQueryServer(String agencyId) {
        super(agencyId, CacheQueryInterface.class.getSimpleName());

    }

    /**
     * Starts up the CacheQueryServer so that RMI calls can be used to query
     * cache. This will automatically cause the object to continue to run and
     * serve requests.
     *
     * @param agencyId
     * @return the singleton CacheQueryServer object. Usually does not need to
     *         used since the server will be fully running.
     */
    public static CacheQueryServer start(String agencyId) {
        if (singleton == null) {
            singleton = new CacheQueryServer(agencyId);
        }

        if (!singleton.getAgencyId().equals(agencyId)) {
            logger.error(
                    "Tried calling CacheQueryServer.start() for "
                            + "agencyId={} but the singleton was created for agencyId={}",
                    agencyId, singleton.getAgencyId());
            return null;
        }

        return singleton;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.transitclock.ipc.interfaces.CacheQueryInterface#
     * getStopArrivalDepartures(java.lang.String)
     */
    @Override
    public List<IpcArrivalDeparture> getStopArrivalDepartures(String stopId) throws RemoteException {

        try {
            StopArrivalDepartureCacheKey nextStopKey = new StopArrivalDepartureCacheKey(stopId,
                    Calendar.getInstance().getTime());

            List<IpcArrivalDeparture> result = StopArrivalDepartureCacheFactory.getInstance().getStopHistory(nextStopKey);

            return result;

        } catch (Exception e) {

            throw new RemoteException(e.toString(), e);
        }
    }

    @Override
    public Integer entriesInCache(String cacheName) throws RemoteException {

        // TODO Auto-generated method stub
        return -1;

    }

    @Override
    public IpcHistoricalAverage getHistoricalAverage(String tripId, Integer stopPathIndex) throws RemoteException {
        StopPathCacheKey key = new StopPathCacheKey(tripId, stopPathIndex);

        HistoricalAverage average = ScheduleBasedHistoricalAverageCache.getInstance().getAverage(key);
        return new IpcHistoricalAverage(average);
    }

    @Override
    public List<IpcArrivalDeparture> getTripArrivalDepartures(String tripId, LocalDate localDate, Integer starttime)
            throws RemoteException {

        try {
            List<IpcArrivalDeparture> result = new ArrayList<IpcArrivalDeparture>();

            if (tripId != null && localDate != null && starttime != null) {

                Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                TripKey tripKey = new TripKey(tripId, date, starttime);

                result = TripDataHistoryCacheFactory.getInstance().getTripHistory(tripKey);
            } else if (tripId != null && localDate != null && starttime == null) {
                Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                for (TripKey key : TripDataHistoryCacheFactory.getInstance().getKeys()) {
                    if (key.getTripId().equals(tripId) && date.compareTo(key.getTripStartDate()) == 0) {
                        result.addAll(TripDataHistoryCacheFactory.getInstance().getTripHistory(key));
                    }
                }
            } else if (tripId != null && localDate == null && starttime == null) {
                for (TripKey key : TripDataHistoryCacheFactory.getInstance().getKeys()) {
                    if (key.getTripId().equals(tripId)) {
                        result.addAll(TripDataHistoryCacheFactory.getInstance().getTripHistory(key));
                    }
                }
            } else if (tripId == null && localDate != null && starttime == null) {
                Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                for (TripKey key : TripDataHistoryCacheFactory.getInstance().getKeys()) {
                    if (date.compareTo(key.getTripStartDate()) == 0) {
                        result.addAll(TripDataHistoryCacheFactory.getInstance().getTripHistory(key));
                    }
                }
            }

            Collections.sort(result, new IpcArrivalDepartureComparator());

            return result;

        } catch (Exception e) {

            throw new RemoteException(e.toString(), e);
        }
    }

    @Override
    public List<IpcHistoricalAverageCacheKey> getScheduledBasedHistoricalAverageCacheKeys() throws RemoteException {

        List<StopPathCacheKey> keys = ScheduleBasedHistoricalAverageCache.getInstance().getKeys();
        List<IpcHistoricalAverageCacheKey> ipcResultList = new ArrayList<IpcHistoricalAverageCacheKey>();

        for (StopPathCacheKey key : keys) {
            ipcResultList.add(new IpcHistoricalAverageCacheKey(key));
        }
        return ipcResultList;
    }

    @Override
    public Double getKalmanErrorValue(String tripId, Integer stopPathIndex) throws RemoteException {
        KalmanErrorCacheKey key = new KalmanErrorCacheKey(tripId, stopPathIndex);
        Double result = ErrorCacheFactory.getInstance().getErrorValue(key).getError();
        return result;
    }

    @Override
    public List<IpcKalmanErrorCacheKey> getKalmanErrorCacheKeys() throws RemoteException {
        List<KalmanErrorCacheKey> keys = ErrorCacheFactory.getInstance().getKeys();
        List<IpcKalmanErrorCacheKey> ipcResultList = new ArrayList<IpcKalmanErrorCacheKey>();

        for (KalmanErrorCacheKey key : keys) {
            ipcResultList.add(new IpcKalmanErrorCacheKey(key));
        }
        return ipcResultList;
    }

    @Override
    public List<IpcHoldingTimeCacheKey> getHoldingTimeCacheKeys() throws RemoteException {
        List<HoldingTimeCacheKey> keys = HoldingTimeCache.getInstance().getKeys();
        List<IpcHoldingTimeCacheKey> ipcResultList = new ArrayList<IpcHoldingTimeCacheKey>();

        for (HoldingTimeCacheKey key : keys) {
            ipcResultList.add(new IpcHoldingTimeCacheKey(key));
        }
        return ipcResultList;
    }

    @Override
    public List<IpcHistoricalAverageCacheKey> getFrequencyBasedHistoricalAverageCacheKeys() throws RemoteException {
        // TODO Auto-generated method stub
        FrequencyBasedHistoricalAverageCache.getInstance();
        return null;
    }
}
