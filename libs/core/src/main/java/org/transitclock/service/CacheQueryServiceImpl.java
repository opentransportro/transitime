/* (C)2023 */
package org.transitclock.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.transitclock.core.dataCache.*;
import org.transitclock.core.dataCache.scheduled.ScheduleBasedHistoricalAverageCache;
import org.transitclock.service.contract.CacheQueryService;
import org.transitclock.service.dto.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Sean Og Crudden Server to allow cache content to be queried.
 */
@Slf4j
@Component
public class CacheQueryServiceImpl implements CacheQueryService {
    @Autowired
    private HoldingTimeCache holdingTimeCache;
    @Autowired
    private ErrorCache errorCache;
    @Autowired
    private ScheduleBasedHistoricalAverageCache scheduleBasedHistoricalAverageCache;
    @Autowired
    private StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface;
    @Autowired
    TripDataHistoryCacheInterface tripDataHistoryCacheInterface;

    @Override
    public List<IpcArrivalDeparture> getStopArrivalDepartures(String stopId) {

        try {
            StopArrivalDepartureCacheKey nextStopKey = new StopArrivalDepartureCacheKey(
                    stopId, Calendar.getInstance().getTime());

            return stopArrivalDepartureCacheInterface.getStopHistory(nextStopKey);

        } catch (Exception e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    @Override
    public Integer entriesInCache(String cacheName) {

        // TODO Auto-generated method stub
        return -1;
    }

    @Override
    public IpcHistoricalAverage getHistoricalAverage(String tripId, Integer stopPathIndex) {
        StopPathCacheKey key = new StopPathCacheKey(tripId, stopPathIndex);

        HistoricalAverage average = scheduleBasedHistoricalAverageCache.getAverage(key);
        return new IpcHistoricalAverage(average);
    }

    @Override
    public List<IpcArrivalDeparture> getTripArrivalDepartures(String tripId, LocalDate localDate, Integer starttime) {
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
            throw new RuntimeException(e.toString(), e);
        }
    }

    @Override
    public List<IpcHistoricalAverageCacheKey> getScheduledBasedHistoricalAverageCacheKeys() {

        List<StopPathCacheKey> keys = scheduleBasedHistoricalAverageCache.getKeys();
        List<IpcHistoricalAverageCacheKey> ipcResultList = new ArrayList<IpcHistoricalAverageCacheKey>();

        for (StopPathCacheKey key : keys) {
            ipcResultList.add(new IpcHistoricalAverageCacheKey(key));
        }
        return ipcResultList;
    }

    @Override
    public Double getKalmanErrorValue(String tripId, Integer stopPathIndex) {
        KalmanErrorCacheKey key = new KalmanErrorCacheKey(tripId, stopPathIndex);
        return errorCache.getErrorValue(key).getError();
    }

    @Override
    public List<IpcKalmanErrorCacheKey> getKalmanErrorCacheKeys() {
        List<KalmanErrorCacheKey> keys = errorCache.getKeys();
        List<IpcKalmanErrorCacheKey> ipcResultList = new ArrayList<>();

        for (KalmanErrorCacheKey key : keys) {
            ipcResultList.add(new IpcKalmanErrorCacheKey(key));
        }
        return ipcResultList;
    }

    @Override
    public List<IpcHoldingTimeCacheKey> getHoldingTimeCacheKeys() {
        List<HoldingTimeCacheKey> keys = holdingTimeCache.getKeys();
        List<IpcHoldingTimeCacheKey> ipcResultList = new ArrayList<>();

        for (HoldingTimeCacheKey key : keys) {
            ipcResultList.add(new IpcHoldingTimeCacheKey(key));
        }
        return ipcResultList;
    }

    @Override
    public List<IpcHistoricalAverageCacheKey> getFrequencyBasedHistoricalAverageCacheKeys() {
        // TODO Auto-generated method stub
        return null;
    }
}
