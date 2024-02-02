/* (C)2023 */
package org.transitclock.service;

import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.transitclock.SingletonContainer;
import org.transitclock.core.dataCache.*;
import org.transitclock.core.dataCache.frequency.FrequencyBasedHistoricalAverageCache;
import org.transitclock.core.dataCache.scheduled.ScheduleBasedHistoricalAverageCache;
import org.transitclock.service.dto.IpcArrivalDeparture;
import org.transitclock.service.dto.IpcHistoricalAverage;
import org.transitclock.service.dto.IpcHistoricalAverageCacheKey;
import org.transitclock.service.dto.IpcHoldingTimeCacheKey;
import org.transitclock.service.dto.IpcKalmanErrorCacheKey;
import org.transitclock.service.contract.CacheQueryInterface;

/**
 * @author Sean Og Crudden Server to allow cache content to be queried.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CacheQueryServiceImpl implements CacheQueryInterface {
    private final ScheduleBasedHistoricalAverageCache scheduleBasedHistoricalAverageCache;
    private final HoldingTimeCache holdingTimeCache;
    private final FrequencyBasedHistoricalAverageCache frequencyBasedHistoricalAverageCache;
    private final StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface;
    private final TripDataHistoryCacheInterface tripDataHistoryCacheInterface;
    private final ErrorCache errorCache;

    /*
     * (non-Javadoc)
     *
     * @see org.transitclock.ipc.interfaces.CacheQueryInterface#
     * getStopArrivalDepartures(java.lang.String)
     */
    @Override
    public List<IpcArrivalDeparture> getStopArrivalDepartures(String stopId) throws RemoteException {

        try {
            StopArrivalDepartureCacheKey nextStopKey = new StopArrivalDepartureCacheKey(
                    stopId, Calendar.getInstance().getTime());

            return stopArrivalDepartureCacheInterface.getStopHistory(nextStopKey);

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

        HistoricalAverage average = scheduleBasedHistoricalAverageCache.getAverage(key);
        return new IpcHistoricalAverage(average);
    }

    @Override
    public List<IpcArrivalDeparture> getTripArrivalDepartures(String tripId, LocalDate localDate, Integer starttime)
            throws RemoteException {

        try {
            List<IpcArrivalDeparture> result = new ArrayList<>();

            if (tripId != null && localDate != null && starttime != null) {

                Date date =
                        Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                TripKey tripKey = new TripKey(tripId, date, starttime);

                result = tripDataHistoryCacheInterface.getTripHistory(tripKey);
            } else if (tripId != null && localDate != null && starttime == null) {
                Date date =
                        Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                for (TripKey key : tripDataHistoryCacheInterface.getKeys()) {
                    if (key.getTripId().equals(tripId) && date.compareTo(key.getTripStartDate()) == 0) {
                        result.addAll(tripDataHistoryCacheInterface.getTripHistory(key));
                    }
                }
            } else if (tripId != null && localDate == null && starttime == null) {
                for (TripKey key : tripDataHistoryCacheInterface.getKeys()) {
                    if (key.getTripId().equals(tripId)) {
                        result.addAll(tripDataHistoryCacheInterface.getTripHistory(key));
                    }
                }
            } else if (tripId == null && localDate != null && starttime == null) {
                Date date =
                        Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                for (TripKey key : tripDataHistoryCacheInterface.getKeys()) {
                    if (date.compareTo(key.getTripStartDate()) == 0) {
                        result.addAll(tripDataHistoryCacheInterface.getTripHistory(key));
                    }
                }
            }

            result.sort(new IpcArrivalDepartureComparator());

            return result;

        } catch (Exception e) {

            throw new RemoteException(e.toString(), e);
        }
    }

    @Override
    public List<IpcHistoricalAverageCacheKey> getScheduledBasedHistoricalAverageCacheKeys() throws RemoteException {

        List<StopPathCacheKey> keys =
                scheduleBasedHistoricalAverageCache.getKeys();
        List<IpcHistoricalAverageCacheKey> ipcResultList = new ArrayList<IpcHistoricalAverageCacheKey>();

        for (StopPathCacheKey key : keys) {
            ipcResultList.add(new IpcHistoricalAverageCacheKey(key));
        }
        return ipcResultList;
    }

    @Override
    public Double getKalmanErrorValue(String tripId, Integer stopPathIndex) throws RemoteException {
        KalmanErrorCacheKey key = new KalmanErrorCacheKey(tripId, stopPathIndex);
        return errorCache.getErrorValue(key).getError();
    }

    @Override
    public List<IpcKalmanErrorCacheKey> getKalmanErrorCacheKeys() throws RemoteException {
        List<KalmanErrorCacheKey> keys = errorCache.getKeys();
        List<IpcKalmanErrorCacheKey> ipcResultList = new ArrayList<>();

        for (KalmanErrorCacheKey key : keys) {
            ipcResultList.add(new IpcKalmanErrorCacheKey(key));
        }
        return ipcResultList;
    }

    @Override
    public List<IpcHoldingTimeCacheKey> getHoldingTimeCacheKeys() throws RemoteException {
        List<HoldingTimeCacheKey> keys = holdingTimeCache.getKeys();
        List<IpcHoldingTimeCacheKey> ipcResultList = new ArrayList<IpcHoldingTimeCacheKey>();

        for (HoldingTimeCacheKey key : keys) {
            ipcResultList.add(new IpcHoldingTimeCacheKey(key));
        }
        return ipcResultList;
    }

    @Override
    public List<IpcHistoricalAverageCacheKey> getFrequencyBasedHistoricalAverageCacheKeys() throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }
}
