/* (C)2023 */
package org.transitclock.core.dataCache.ehcache.scheduled;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.data.PredictionConfig;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheFactory;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheKey;
import org.transitclock.core.dataCache.StopPathCacheKey;
import org.transitclock.core.dataCache.ehcache.CacheManagerFactory;
import org.transitclock.core.predictiongenerator.scheduled.dwell.DwellModel;
import org.transitclock.core.predictiongenerator.scheduled.dwell.DwellTimeModelFactory;
import org.transitclock.domain.structs.ArrivalDeparture;
import org.transitclock.domain.structs.Headway;
import org.transitclock.service.dto.IpcArrivalDeparture;

/**
 * @author scrudden This stores DwellModel instances in the cache. TODO We should abstract the
 *     anomaly detection as per TODO in code below.
 */
@Slf4j
public class DwellTimeModelCache implements org.transitclock.core.dataCache.DwellTimeModelCacheInterface {

    private static final String cacheName = "dwellTimeModelCache";

    private final Cache<StopPathCacheKey, DwellModel> cache;

    public DwellTimeModelCache() throws IOException {
        CacheManager cm = CacheManagerFactory.getInstance();
        cache = cm.getCache(cacheName, StopPathCacheKey.class, DwellModel.class);
    }

    @Override
    public synchronized void addSample(ArrivalDeparture event, Headway headway, long dwellTime) {

        StopPathCacheKey key = new StopPathCacheKey(headway.getTripId(), event.getStopPathIndex(), false);

        DwellModel model = null;

        if (cache.get(key) != null) {
            model = cache.get(key);

            model.putSample((int) dwellTime, (int) headway.getHeadway(), null);
        } else {
            model = DwellTimeModelFactory.getInstance();
        }
        model.putSample((int) dwellTime, (int) headway.getHeadway(), null);
        cache.put(key, model);
    }

    @Override
    public void addSample(ArrivalDeparture departure) {
        try {
            if (departure != null && !departure.isArrival()) {
                StopArrivalDepartureCacheKey key =
                        new StopArrivalDepartureCacheKey(departure.getStopId(), departure.getDate());
                List<IpcArrivalDeparture> stopData =
                        StopArrivalDepartureCacheFactory.getInstance().getStopHistory(key);

                if (stopData != null && stopData.size() > 1) {
                    IpcArrivalDeparture arrival = findArrival(stopData, new IpcArrivalDeparture(departure));

                    if (arrival != null) {
                        IpcArrivalDeparture previousArrival = findPreviousArrival(stopData, arrival);
                        if (arrival != null && previousArrival != null) {
                            Headway headway = new Headway();
                            headway.setHeadway(arrival.getTime().getTime()
                                    - previousArrival.getTime().getTime());
                            long dwelltime =
                                    departure.getTime() - arrival.getTime().getTime();
                            headway.setTripId(arrival.getTripId());

                            /* Leave out silly values as they are most likely errors or unusual circumstance. */
                            /* TODO Should abstract this behind an anomaly detention interface/Factory */

                            if (departure.getScheduleAdherence() != null
                                    && departure
                                            .getScheduleAdherence()
                                            .isWithinBounds(
                                                    PredictionConfig.minSceheduleAdherence.getValue(),
                                                    PredictionConfig.maxSceheduleAdherence.getValue())) {

                                // Arrival schedule adherence appears not to be set much. So only
                                // stop if set and outside range.
                                if (previousArrival.getScheduledAdherence() == null
                                        || previousArrival
                                                .getScheduledAdherence()
                                                .isWithinBounds(
                                                        PredictionConfig.minSceheduleAdherence.getValue(),
                                                        PredictionConfig.maxSceheduleAdherence.getValue())) {
                                    if (dwelltime < PredictionConfig.maxDwellTimeAllowedInModel.getValue()
                                            && dwelltime > PredictionConfig.minDwellTimeAllowedInModel.getValue()) {
                                        if (headway.getHeadway() < PredictionConfig.maxHeadwayAllowedInModel.getValue()
                                                && headway.getHeadway() > PredictionConfig.minHeadwayAllowedInModel.getValue()) {
                                            addSample(departure, headway, dwelltime);
                                        } else {
                                            logger.warn("Headway outside allowable range . {}", headway);
                                        }
                                    } else {
                                        logger.warn("Dwell time {} outside allowable range for {}.", dwelltime, departure);
                                    }
                                } else {
                                    logger.warn("Schedule adherence outside allowable range. {}", previousArrival.getScheduledAdherence());
                                }
                            } else {
                                logger.warn("Schedule adherence outside allowable range. {}", departure.getScheduleAdherence());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private IpcArrivalDeparture findPreviousArrival(List<IpcArrivalDeparture> stopData, IpcArrivalDeparture arrival) {
        for (IpcArrivalDeparture event : stopData) {
            if (event.isArrival()) {
                if (!event.getVehicleId().equals(arrival.getVehicleId())) {
                    if (!event.getTripId().equals(arrival.getTripId())) {
                        if (event.getStopId().equals(arrival.getStopId())) {
                            if (event.getTime().getTime() < arrival.getTime().getTime()
                                    && (sameDay(
                                                    event.getTime().getTime(),
                                                    arrival.getTime().getTime())
                                            || Math.abs(event.getTime().getTime()
                                                            - arrival.getTime().getTime())
                                                    < PredictionConfig.maxHeadwayAllowedInModel.getValue())) return event;
                        }
                    }
                }
            }
        }

        return null;
    }

    private boolean sameDay(Long date1, Long date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(new Date(date1));
        cal2.setTime(new Date(date2));
        boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

        return sameDay;
    }

    private IpcArrivalDeparture findArrival(List<IpcArrivalDeparture> stopData, IpcArrivalDeparture departure) {

        for (IpcArrivalDeparture event : stopData) {
            if (event.isArrival()) {
                if (event.getStopId().equals(departure.getStopId())) {
                    if (event.getVehicleId().equals(departure.getVehicleId())) {
                        if (event.getTripId().equals(departure.getTripId())) {
                            return event;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Long predictDwellTime(StopPathCacheKey cacheKey, Headway headway) {
        DwellModel model = cache.get(cacheKey);
        if (model == null || headway == null) return null;

        if (model.predict((int) headway.getHeadway(), null) != null)
            return Long.valueOf(model.predict((int) headway.getHeadway(), null));

        return null;
    }
}
