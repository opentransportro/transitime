/* (C)2023 */
package org.transitclock.core.holdingmethod;

import lombok.extern.slf4j.Slf4j;
import org.transitclock.config.data.HoldingConfig;
import org.transitclock.core.VehicleStatus;
import org.transitclock.core.dataCache.PredictionDataCache;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheInterface;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheKey;
import org.transitclock.domain.hibernate.DataDbLogger;
import org.transitclock.domain.structs.ArrivalDeparture;
import org.transitclock.domain.structs.HoldingTime;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.service.dto.IpcArrivalDeparture;
import org.transitclock.service.dto.IpcPrediction;
import org.transitclock.service.dto.IpcPredictionsForRouteStopDest;
import org.transitclock.utils.SystemTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Sean Óg Crudden Simple holding time generator.
 */
@Slf4j
public class SimpleHoldingTimeGeneratorImpl implements HoldingTimeGenerator {
    private final PredictionDataCache predictionDataCache;
    private final StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface;
    private final DataDbLogger dataDbLogger;
    private final DbConfig dbConfig;

    public SimpleHoldingTimeGeneratorImpl(PredictionDataCache predictionDataCache, StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface, DataDbLogger dataDbLogger, DbConfig dbConfig) {
        this.predictionDataCache = predictionDataCache;
        this.stopArrivalDepartureCacheInterface = stopArrivalDepartureCacheInterface;
        this.dataDbLogger = dataDbLogger;
        this.dbConfig = dbConfig;
    }

    public HoldingTime generateHoldingTime(VehicleStatus vehicleStatus, IpcArrivalDeparture event) {
        if (event.isArrival() && isControlStop(event.getStopId(), event.getStopPathIndex())) {
            logger.debug("Calling Simple Holding Generator for event : {}", event);

            IpcArrivalDeparture lastVehicleDeparture = getLastVehicleDepartureTime(
                    event.getTripId(),
                    event.getStopId(),
                    new Date(event.getTime().getTime()));

            if (lastVehicleDeparture != null) {
                logger.debug("Found last vehicle departure event: {}", lastVehicleDeparture);

                long current_vehicle_arrival_time = event.getTime().getTime();

                long holdingTimeValue = calculateHoldingTime(
                        current_vehicle_arrival_time,
                        lastVehicleDeparture.getTime().getTime());

                logger.debug("Holding time for : {} is {}.", event, holdingTimeValue);

                HoldingTime holdingTime = new HoldingTime(
                        dbConfig.getConfigRev(),
                        new Date(current_vehicle_arrival_time + holdingTimeValue),
                        SystemTime.getDate(),
                        event.getVehicleId(),
                        event.getStopId(),
                        event.getTripId(),
                        event.getRouteId(),
                        false,
                        true,
                        new Date(event.getTime().getTime()),
                        true,
                        0);

                if (HoldingConfig.storeHoldingTimes.getValue())
                    dataDbLogger.add(holdingTime);

                return holdingTime;

            } else {
                logger.debug(
                        "Did not find last vehicle departure for stop {}. This is required to calculate holding time.",
                        event.getStopId());

                long current_vehicle_arrival_time = event.getTime().getTime();
                return new HoldingTime(
                        dbConfig.getConfigRev(),
                        new Date(current_vehicle_arrival_time),
                        SystemTime.getDate(),
                        event.getVehicleId(),
                        event.getStopId(),
                        event.getTripId(),
                        event.getRouteId(),
                        false,
                        true,
                        new Date(event.getTime().getTime()),
                        false,
                        0);
            }
        }
        // Return null so has no effect.
        return null;
    }

    private IpcArrivalDeparture getLastVehicleDepartureTime(String tripId, String stopId, Date time) {
        StopArrivalDepartureCacheKey currentStopKey = new StopArrivalDepartureCacheKey(stopId, time);

        List<IpcArrivalDeparture> currentStopList =
                stopArrivalDepartureCacheInterface.getStopHistory(currentStopKey);

        IpcArrivalDeparture closestDepartureEvent = null;
        if (currentStopList != null) {
            /* These are sorted when placed in cache */
            for (IpcArrivalDeparture event : currentStopList) {
                // if(event.isDeparture() && event.getTripId().equals(tripId)) TODO This is because
                // of the GTFS config of Atlanta Streetcar. Could we use route_id instead?
                if (event.isDeparture()) {
                    if (closestDepartureEvent == null) closestDepartureEvent = event;
                    else if (time.getTime() - event.getTime().getTime()
                            < time.getTime() - closestDepartureEvent.getTime().getTime()) {
                        closestDepartureEvent = event;
                    }
                }
            }
        }
        return closestDepartureEvent;
    }

    private long calculateHoldingTime(
            long current_vehicle_arrival_time, long last_vehicle_departure_time, long N_1, long N_2) {
        // G16=MAX(IF((E18-H15)/3>(F17-H15)/2,((E18-H15)/3-(B16-H15))/1.5,((F17-H15)/2-(B16-H15))/2),0)

        /*Vehicle Z departure time at this stop. (H15)
        One vehicle arrive at control point at 3:20. (vehicle A) (B16)
        The next + 1 vehicle arrives at control point at 3:29 (vehicle B) (F17)
        The next + 2 vehicle arrives at control point at 3:42 (vehicle C) (E18) */

        long holdingTime;
        long E18 = N_2;
        long F17 = N_1;
        long B16 = current_vehicle_arrival_time;
        long H15 = last_vehicle_departure_time;

        if (((E18 - H15) / 3) > ((F17 - H15) / 2)) {
            holdingTime = ((E18 - H15) / 3 - (B16 - H15));
        } else {
            holdingTime = ((F17 - H15) / 2 - (B16 - H15));
        }
        return Math.max(holdingTime, 0);
    }

    private long calculateHoldingTime(long current_vehicle_arrival_time, long last_vehicle_departure_time) {
        // TODO to be implemented as per google doc.

        // HoldingTime= max(0,PlannedHeadway - TimeSinceLastDeparture)
        long holdingTime;

        holdingTime = Math.max(
                HoldingConfig.plannedHeadwayMsec.getValue().longValue()
                        - Math.abs(current_vehicle_arrival_time - last_vehicle_departure_time),
                0);

        return holdingTime;
    }

    @Override
    public HoldingTime generateHoldingTime(VehicleStatus vehicleStatus, IpcPrediction arrivalPrediction) {

        HoldingTime holdingTime = null;

        return holdingTime;
    }

    protected IpcPrediction getForwardVehicleDeparturePrediction(IpcPrediction predictionEvent) {
        List<IpcPrediction> predictions = new ArrayList<>();

        List<IpcPredictionsForRouteStopDest> predictionsForRouteStopDests =
                predictionDataCache.getPredictions(predictionEvent.getRouteId(), predictionEvent.getStopId());

        for (IpcPredictionsForRouteStopDest predictionForRouteStopDest : predictionsForRouteStopDests) {
            predictions.addAll(predictionForRouteStopDest.getPredictionsForRouteStop());
        }
        predictions.sort(new PredictionTimeComparator());

        int found = -1;

        for (int i = 0; i < predictions.size(); i++) {
            if (predictions.get(i).getStopId().equals(predictionEvent.getStopId())
                    && predictions.get(i).getTripId().equals(predictionEvent.getTripId())
                    && predictions.get(i).isArrival()
                    && predictionEvent.isArrival()) {
                found = i;
            }
            /* now look until after this prediction and it is the next one for same stop but a different vehicle */
            if (found != -1 && i > found) {
                if (!predictions.get(i).isArrival()
                        && predictions.get(i).getStopId().equals(predictionEvent.getStopId())
                        && !predictions.get(i).getTripId().equals(predictionEvent.getTripId())) {
                    return predictions.get(i);
                }
            }
        }
        return null;
    }

    protected List<IpcPrediction> getBackwardArrivalPredictions(IpcPrediction predictionEvent) {
        List<IpcPrediction> predictions = new ArrayList<>();

        List<IpcPredictionsForRouteStopDest> predictionsForRouteStopDests =
                predictionDataCache.getPredictions(predictionEvent.getRouteId(), predictionEvent.getStopId());

        for (IpcPredictionsForRouteStopDest predictionForRouteStopDest : predictionsForRouteStopDests) {
            for (IpcPrediction prediction : predictionForRouteStopDest.getPredictionsForRouteStop()) {
                logger.debug(prediction.toString());
                // TODO
                // if(prediction.getPredictionTime()>predictionEvent.getPredictionTime()&&prediction.isArrival())
                // The end of the route it seems to only generate departure predictions which points
                // towards an issue.
                // if(prediction.getPredictionTime()>predictionEvent.getPredictionTime())
                if (prediction.getPredictionTime() > predictionEvent.getPredictionTime() && prediction.isArrival())
                    predictions.add(prediction);
            }
        }
        predictions.sort(new PredictionTimeComparator());
        /* TODO get the first two of the same type */
        return predictions;
    }

    @Override
    public List<ControlStop> getControlPointStops() {
        List<ControlStop> controlStops = new ArrayList<>();

        for (String stopEntry : HoldingConfig.controlStopList.getValue()) {
            controlStops.add(new ControlStop(stopEntry));
        }
        return controlStops;
    }

    private boolean isControlStop(String stopId, int stopPathIndex) {
        ControlStop controlStop = new ControlStop("" + stopPathIndex, stopId);
        if (getControlPointStops() != null) return getControlPointStops().contains(controlStop);
        else return false;
    }

    @Override
    public void handleDeparture(VehicleStatus vehicleStatus, ArrivalDeparture arrivalDeparture) {
        // TODO Auto-generated method stub

    }
}
