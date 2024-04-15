/* (C)2023 */
package org.transitclock.core.dataCache.ehcache.scheduled;

import com.querydsl.jpa.impl.JPAQuery;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.hibernate.Session;

import org.transitclock.ApplicationProperties.Prediction;
import org.transitclock.core.TemporalDifference;
import org.transitclock.core.dataCache.DwellTimeModelCacheInterface;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheInterface;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheKey;
import org.transitclock.core.dataCache.StopPathCacheKey;
import org.transitclock.core.prediction.scheduled.dwell.DwellModel;
import org.transitclock.domain.structs.ArrivalDeparture;
import org.transitclock.domain.structs.Headway;
import org.transitclock.domain.structs.QArrivalDeparture;
import org.transitclock.service.dto.IpcArrivalDeparture;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author scrudden This stores DwellModel instances in the cache. TODO We should abstract the
 *     anomaly detection as per TODO in code below.
 */
@Slf4j
public class DwellTimeModelCache implements DwellTimeModelCacheInterface {
    private static final String cacheName = "dwellTimeModelCache";
    private final Integer minScheduleAdherence;
    private final Integer maxScheduleAdherence;
    private final Cache<StopPathCacheKey, DwellModel> cache;
    private final DwellModel dwellModel;
    private final StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface;
    private final Prediction.Rls rlsPredictionConfig;

    public DwellTimeModelCache(CacheManager cm,
                               DwellModel dwellModel,
                               StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface,
                               Prediction.Rls rlsPredictionConfig) {
        cache = cm.getCache(cacheName, StopPathCacheKey.class, DwellModel.class);
        this.dwellModel = dwellModel;
        this.stopArrivalDepartureCacheInterface = stopArrivalDepartureCacheInterface;
        this.rlsPredictionConfig = rlsPredictionConfig;
        this.minScheduleAdherence = rlsPredictionConfig.getMinSceheduleAdherence();
        this.maxScheduleAdherence = rlsPredictionConfig.getMaxSceheduleAdherence();
    }

    @Override
    public synchronized void addSample(ArrivalDeparture event, Headway headway, long dwellTime) {
        StopPathCacheKey key = new StopPathCacheKey(headway.getTripId(), event.getStopPathIndex(), false);
        DwellModel model = null;

        if (cache.get(key) != null) {
            model = cache.get(key);
            model.putSample((int) dwellTime, (int) headway.getHeadway(), null);
        } else {
            model = dwellModel;
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
                        stopArrivalDepartureCacheInterface.getStopHistory(key);

                if (stopData != null && stopData.size() > 1) {
                    IpcArrivalDeparture arrival = findArrival(stopData, new IpcArrivalDeparture(departure));

                    if (arrival != null) {
                        IpcArrivalDeparture previousArrival = findPreviousArrival(stopData, arrival);
                        if (previousArrival != null) {
                            Headway headway = new Headway();
                            headway.setHeadway(arrival.getTime().getTime() - previousArrival.getTime().getTime());
                            headway.setTripId(arrival.getTripId());

                            long dwelltime = departure.getTime() - arrival.getTime().getTime();

                            /* Leave out silly values as they are most likely errors or unusual circumstance. */
                            /* TODO Should abstract this behind an anomaly detention interface/Factory */
                            TemporalDifference departureScheduleAdherence = departure.getScheduleAdherence();
                            if (departureScheduleAdherence != null && departureScheduleAdherence.isWithinBounds(minScheduleAdherence, maxScheduleAdherence)) {
                                // Arrival schedule adherence appears not to be set much. So only
                                // stop if set and outside range.
                                TemporalDifference scheduledAdherence = previousArrival.getScheduledAdherence();
                                if (scheduledAdherence == null || scheduledAdherence.isWithinBounds(minScheduleAdherence, maxScheduleAdherence)) {
                                    if (dwelltime < rlsPredictionConfig.getMaxDwellTimeAllowedInModel() &&
                                            dwelltime > rlsPredictionConfig.getMinDwellTimeAllowedInModel()) {
                                        if (headway.getHeadway() < rlsPredictionConfig.getMaxHeadwayAllowedInModel() &&
                                                headway.getHeadway() > rlsPredictionConfig.getMinHeadwayAllowedInModel()) {
                                            addSample(departure, headway, dwelltime);
                                        } else {
                                            logger.warn("Headway outside allowable range . {}", headway);
                                        }
                                    } else {
                                        logger.warn("Dwell time {} outside allowable range for {}.", dwelltime, departure);
                                    }
                                } else {
                                    logger.warn("Schedule adherence outside allowable range. {}", scheduledAdherence);
                                }
                            } else {
                                logger.warn("Schedule adherence outside allowable range. {}", departureScheduleAdherence);
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
                                                    < rlsPredictionConfig.getMaxHeadwayAllowedInModel())) return event;
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

    @Override
    public void populateCacheFromDb(Session session, Date startDate, Date endDate) {
        JPAQuery<ArrivalDeparture> query = new JPAQuery<>(session);
        var qentity = QArrivalDeparture.arrivalDeparture;
        List<ArrivalDeparture> results = query.from(qentity)
                .where(qentity.time.between(startDate,endDate))
                .orderBy(qentity.time.asc())
                .fetch();

        for (ArrivalDeparture result : results) {
            try {
                addSample(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
