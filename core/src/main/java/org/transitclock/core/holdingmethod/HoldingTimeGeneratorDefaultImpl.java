/* (C)2023 */
package org.transitclock.core.holdingmethod;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.transitclock.config.data.HoldingConfig;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.*;
import org.transitclock.domain.hibernate.DataDbLogger;
import org.transitclock.domain.structs.ArrivalDeparture;
import org.transitclock.domain.structs.HoldingTime;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.service.dto.IpcArrivalDeparture;
import org.transitclock.service.dto.IpcPrediction;
import org.transitclock.service.dto.IpcPredictionsForRouteStopDest;
import org.transitclock.service.dto.IpcVehicleComplete;
import org.transitclock.utils.SystemTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Sean Óg Crudden This is a default implementation of the holding time generator and is an
 *     implementation of the approach proposed by Simon Berrebi et al.
 *     <p>http://www.worldtransitresearch.info/research/5644/
 *     <p>The theory is to use the time generated as the holding time for the vehicle at the control
 *     point. This is in an attempt to manage the headway between vehicles along the route.
 *     <p>This is a WIP.
 */
@Slf4j
@RequiredArgsConstructor
public class HoldingTimeGeneratorDefaultImpl implements HoldingTimeGenerator {
    private final PredictionDataCache predictionDataCache;
    private final StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface;
    private final DataDbLogger dataDbLogger;
    private final DbConfig dbConfig;
    private final VehicleDataCache vehicleDataCache;
    private final HoldingTimeCache holdingTimeCache;
    private final VehicleStateManager vehicleStateManager;

    public HoldingTime generateHoldingTime(VehicleState vehicleState, IpcArrivalDeparture event) {

        if (!HoldingConfig.useArrivalEvents.getValue()) {
            return null;
        }

        if (event.isArrival() && isControlStop(event.getStopId())) {

            List<IpcPrediction> predictions = new ArrayList<>();

            for (IpcVehicleComplete currentVehicle : vehicleDataCache.getVehicles()) {
                if (predictionDataCache.getPredictionForVehicle(
                                currentVehicle.getId(), event.getRouteId(), event.getStopId())
                        != null) {
                    predictions.add(predictionDataCache.getPredictionForVehicle(
                            currentVehicle.getId(), event.getRouteId(), event.getStopId()));
                }
            }
            predictions.sort(new PredictionTimeComparator());

            logger.debug(
                    "Calling Holding Generator for event : {} using predictions : {}",
                    event,
                    predictions);

            // This is to remove a prediction for the current vehicle and stop. Belt and braces.
            if (!predictions.isEmpty() && predictions.get(0).getVehicleId().equals(event.getVehicleId())) {
                predictions.remove(0);
            }

            logger.debug("Have {} predictions for stops {}.", predictions.size(), event.getStopId());

            IpcArrivalDeparture lastVehicleDeparture = getLastVehicleDepartureTime(
                    event.getVehicleId(),
                    event.getTripId(),
                    event.getStopId(),
                    new Date(event.getTime().getTime()));

            /* Now get check if there is current holdingTie for the stop and get the next vehicle scheduled to leave and use its holding time as the departure time for calculation. */
            List<HoldingTime> currentHoldingTimesForStop = getCurrentHoldingTimesForStop(event.getStopId());
            HoldingTime lastVehicleDepartureByHoldingTime = getNextDepartureByHoldingTime(event.getVehicleId(), currentHoldingTimesForStop);

            // IF ARRIVAL OF HOLDING VEHICLE IS AFTER CURRENT DO NOT CONSIDER AS DEPARTURE
            /*if(lastVehicleDepartureByHoldingTime.getArrivalTime().getTime()<event.getTime())
            {
            	lastVehicleDepartureByHoldingTime=null;
            }*/

            if (lastVehicleDepartureByHoldingTime != null) {
                logger.debug("Found waiting vehicle with holding time: {}", lastVehicleDepartureByHoldingTime);
                if (predictions.size() > 1) {
                    int counter = 0;
                    Long[] N = predictionsToLongArray(predictions);

                    for (int i = 0;
                            i < predictions.size() && counter < HoldingConfig.maxPredictionsForHoldingTimeCalculation.getValue();
                            i++) {
                        logger.debug(
                                "Prediction for N-{} {}: {} ",
                                counter + 1,
                                predictions.get(i).getVehicleId(),
                                predictions.get(i));
                        counter++;
                    }

                    long current_vehicle_arrival_time = event.getTime().getTime();
                    // Use time to leave from now rather than any time in the past.
                    long holdingTimeValue = calculateHoldingTime(
                            current_vehicle_arrival_time,
                            lastVehicleDepartureByHoldingTime
                                    .getTimeToLeave(event.getTime())
                                    .getTime(),
                            N,
                            HoldingConfig.maxPredictionsForHoldingTimeCalculation.getValue());

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
                            N.length);

                    logger.debug("Holding time for : {} is {}.", event, holdingTime);

                    if (HoldingConfig.storeHoldingTimes.getValue())
                        dataDbLogger.add(holdingTime);

                    return holdingTime;
                } else {
                    long current_vehicle_arrival_time = event.getTime().getTime();

                    long holdingTimeValue = calculateHoldingTime(
                            current_vehicle_arrival_time,
                            lastVehicleDepartureByHoldingTime.getHoldingTime().getTime());

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

                    logger.debug("Holding time for : {} is {}.", event, holdingTime);

                    if (HoldingConfig.storeHoldingTimes.getValue())
                        dataDbLogger.add(holdingTime);

                    return holdingTime;
                }
            } else if (lastVehicleDeparture != null) {
                logger.debug("Found last vehicle departure event: {}", lastVehicleDeparture);

                if (predictions.size() > 1) {
                    Long[] N;

                    int counter = 0;

                    N = predictionsToLongArray(predictions);

                    for (int i = 0;
                            i < predictions.size() && counter < HoldingConfig.maxPredictionsForHoldingTimeCalculation.getValue();
                            i++) {
                        logger.debug(
                                "Prediction for N-{} {}: {} ",
                                counter + 1,
                                predictions.get(i).getVehicleId(),
                                predictions.get(i));
                        counter++;
                    }

                    long current_vehicle_arrival_time = event.getTime().getTime();

                    long holdingTimeValue = calculateHoldingTime(
                            current_vehicle_arrival_time,
                            lastVehicleDeparture.getTime().getTime(),
                            N,
                            HoldingConfig.maxPredictionsForHoldingTimeCalculation.getValue());

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
                            N.length);

                    logger.debug("Holding time for : {} is {}.", event, holdingTime);

                    if (HoldingConfig.storeHoldingTimes.getValue())
                        dataDbLogger.add(holdingTime);

                    return holdingTime;
                } else {
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
                }
            } else {
                logger.debug(
                        "Did not find last vehicle departure for stop {}. This is required to"
                                + " calculate holding time.",
                        event.getStopId());
                long current_vehicle_arrival_time = event.getTime().getTime();
                // TODO WHY BOTHER?
                HoldingTime holdingTime = new HoldingTime(
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

                if (HoldingConfig.storeHoldingTimes.getValue())
                    dataDbLogger.add(holdingTime);

                return holdingTime;
            }
        }
        // Return null so has no effect.
        return null;
    }

    public List<String> getOrderedListOfVehicles(String routeId) {
        int count = 0;
        boolean canorder = true;
        List<VehicleState> unordered = new ArrayList<VehicleState>();
        List<String> ordered = null;
        for (VehicleState currentVehicleState : vehicleStateManager.getVehiclesState()) {
            if (currentVehicleState.getTrip() != null
                    && currentVehicleState.getTrip().getRoute(dbConfig).getId().equals(routeId)
                    && currentVehicleState.isPredictable()) {
                count++;
                unordered.add(currentVehicleState);
                if (currentVehicleState.getHeadway() == null) {
                    canorder = false;
                }
            }
        }
        if (canorder) {
            ordered = new ArrayList<String>();

            while (((count + 1) > 0) && !unordered.isEmpty()) {
                if (ordered.isEmpty()) {
                    String first = unordered.get(0).getVehicleId();
                    String second = unordered.get(0).getHeadway().getOtherVehicleId();

                    ordered.add(first);
                    count--;
                    ordered.add(second);
                    count--;
                } else {
                    ordered.add(vehicleStateManager
                            .getVehicleState(ordered.get(ordered.size() - 1))
                            .getHeadway()
                            .getOtherVehicleId());
                    count--;
                }
            }
            // check first vehicle equals last vehicle
            if (ordered.size() > 1) {
                if (!ordered.get(ordered.size() - 1).equals(ordered.get(0))) {
                    return null;
                }
            }
        }
        return ordered;
    }

    protected List<HoldingTime> getCurrentHoldingTimesForStop(String stopId) {
        List<HoldingTime> currentHoldingTimes = new ArrayList<HoldingTime>();

        for (IpcVehicleComplete currentVehicle : vehicleDataCache.getVehicles()) {
            VehicleState vehicleState = vehicleStateManager.getVehicleState(currentVehicle.getId());
            if (vehicleState.getHoldingTime() != null) {
                if (vehicleState.getHoldingTime().getStopId().equals(stopId)) {
                    currentHoldingTimes.add(vehicleState.getHoldingTime());
                }
            }
        }
        return currentHoldingTimes;
    }

    protected HoldingTime getNextDepartureByHoldingTime(String currentVehicleId, List<HoldingTime> holdingTimes) {
        logger.debug("Looking for next departure by holding time for vehicle {}.", currentVehicleId);

        HoldingTime nextDeparture = null;
        for (HoldingTime holdingTime : holdingTimes) {

            if (!holdingTime.getVehicleId().equals(currentVehicleId) && !holdingTime.isArrivalPredictionUsed()) {
                // Ignore predicted holding times in calculation
                if (!holdingTime.isArrivalPredictionUsed()) {
                    if (nextDeparture == null || holdingTime.getHoldingTime().before(nextDeparture.getHoldingTime())) {
                        nextDeparture = holdingTime;
                    }
                } else {
                    logger.debug("Holding time not considered as it is a predicted holding time. {}", holdingTime);
                }
            }
        }
        return nextDeparture;
    }

    private IpcArrivalDeparture getLastVehicleDepartureTime(
            String currentVehicleId, String tripId, String stopId, Date time) {
        StopArrivalDepartureCacheKey currentStopKey = new StopArrivalDepartureCacheKey(stopId, time);

        List<IpcArrivalDeparture> currentStopList =
                stopArrivalDepartureCacheInterface.getStopHistory(currentStopKey);

        IpcArrivalDeparture closestDepartureEvent = null;
        if (currentStopList != null) {
            /* These are sorted when placed in cache */
            for (IpcArrivalDeparture event : currentStopList) {
                // if(event.isDeparture() && event.getTripId().equals(tripId)) TODO This is because
                // of the GTFS config of Atlanta Streetcar. Could we use route_id instead?
                if (!event.getVehicleId().equals(currentVehicleId)) {
                    if (event.isDeparture()) {
                        if (closestDepartureEvent == null) closestDepartureEvent = event;
                        else if (time.getTime() - event.getTime().getTime()
                                < time.getTime()
                                        - closestDepartureEvent.getTime().getTime()) {
                            closestDepartureEvent = event;
                        }
                    }
                }
            }
        }
        return closestDepartureEvent;
    }

    private ArrayList<String> getOtherVehiclesAtStop(String stopId, String currentVehicleId) {
        ArrayList<String> alsoAtStop = new ArrayList<String>();

        for (IpcVehicleComplete currentVehicle : vehicleDataCache.getVehicles()) {
            if (currentVehicle.isAtStop()
                    && currentVehicle.getAtOrNextStopId().equals(stopId)
                    && !currentVehicle.getId().equals(currentVehicleId)) {
                alsoAtStop.add(currentVehicle.getId());
            }
        }

        return alsoAtStop;
    }

    private IpcArrivalDeparture getLastVehicleArrivalEvent(String stopid, String vehicleid, Date time) {
        StopArrivalDepartureCacheKey currentStopKey = new StopArrivalDepartureCacheKey(stopid, time);

        List<IpcArrivalDeparture> currentStopList =
                stopArrivalDepartureCacheInterface.getStopHistory(currentStopKey);

        IpcArrivalDeparture closestArrivalEvent = null;

        if (currentStopList != null) {
            for (IpcArrivalDeparture event : currentStopList) {
                if (event.getVehicleId().equals(vehicleid)) {
                    // if it arrives after the current time
                    if (event.isArrival() && event.getStopId().equals(stopid)) {
                        if (event.getTime().getTime() > time.getTime()) {
                            if (closestArrivalEvent == null) {
                                closestArrivalEvent = event;
                            } else if (Math.abs(time.getTime() - event.getTime().getTime())
                                    < Math.abs(time.getTime()
                                            - closestArrivalEvent.getTime().getTime())) {
                                closestArrivalEvent = event;
                            }
                        }
                    }
                }
            }
        }
        return closestArrivalEvent;
    }

    public List<IpcArrivalDeparture> addArrivalTimesForVehiclesAtStop(
            String stopid, String vehicleid, Date time, List<Long> arrivalTimes) {

        List<IpcArrivalDeparture> events = new ArrayList<>();
        for (String othervehicle : getOtherVehiclesAtStop(stopid, vehicleid)) {
            IpcArrivalDeparture event = getLastVehicleArrivalEvent(stopid, othervehicle, time);

            if (event != null) {
                arrivalTimes.add(event.getTime().getTime());
                events.add(event);
            }
        }
        arrivalTimes.sort(null);
        events.sort(new IpcArrivalDepartureComparator());
        return events;
    }

    static Long calculateHoldingTime(
            Long current_vehicle_arrival_time, Long last_vehicle_departure_time, Long[] N, int max_predictions) {
        long max_value = -1;

        for (int i = 0; i < N.length && i < max_predictions; i++) {
            long value = (N[i] - last_vehicle_departure_time) / (i + 2);
            if (value > max_value) {
                max_value = value;
            }
        }
        return Math.max(max_value - (current_vehicle_arrival_time - last_vehicle_departure_time), 0);
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
    public HoldingTime generateHoldingTime(VehicleState vehicleState, IpcPrediction arrivalPrediction) {

        if (!HoldingConfig.useArrivalPredictions.getValue()) {
            return null;
        }

        HoldingTime holdingTime = null;
        if (arrivalPrediction.isArrival() && isControlStop(arrivalPrediction.getStopId())) {
            logger.debug("Calling Holding Generator for prediction : {}", arrivalPrediction);

            /* TODO We will also need a holding time based on predictions to inform predictions for later stops. We can swap to arrival one once we have arrival event for vehicle and set a definite hold time.*/
            IpcPrediction forwardDeparturePrediction = getForwardVehicleDeparturePrediction(arrivalPrediction);
            if (forwardDeparturePrediction == null)
                logger.debug("Cannot find last departure prediction for : {}", arrivalPrediction);
            else
                logger.debug("Found last vehicle predicted departure event: {}", forwardDeparturePrediction);

            IpcArrivalDeparture lastDeparture = getLastVehicleDepartureTime(
                    arrivalPrediction.getVehicleId(),
                    arrivalPrediction.getTripId(),
                    arrivalPrediction.getStopId(),
                    new Date(arrivalPrediction.getPredictionTime()));
            if (lastDeparture == null)
                logger.debug("Cannot find last departure for : {}", arrivalPrediction);
            else logger.debug("Found last vehicle departure event: {}", lastDeparture);

            List<IpcPrediction> backwardArrivalPredictions = getBackwardArrivalPredictions(arrivalPrediction);
            if (backwardArrivalPredictions != null)
                logger.debug(
                        "Found {} arrival predictions for stop {}.",
                        backwardArrivalPredictions.size(),
                        arrivalPrediction.getStopId());

            if ((forwardDeparturePrediction != null || lastDeparture != null)
                    && backwardArrivalPredictions != null
                    && !backwardArrivalPredictions.isEmpty()) {
                for (int i = 0; i < backwardArrivalPredictions.size(); i++) {
                    logger.debug(
                            "Prediction for N-{} {}: {} ",
                            i + 1,
                            backwardArrivalPredictions.get(i).getVehicleId(),
                            backwardArrivalPredictions.get(i));
                }
                Long[] N = predictionsToLongArray(backwardArrivalPredictions);
                if (lastDeparture != null && forwardDeparturePrediction != null) {
                    if (lastDeparture.getTime().getTime() > forwardDeparturePrediction.getPredictionTime()) {
                        long holdingTimeValue = calculateHoldingTime(
                                arrivalPrediction.getPredictionTime(),
                                forwardDeparturePrediction.getPredictionTime(),
                                N,
                                HoldingConfig.maxPredictionsForHoldingTimeCalculation.getValue());

                        holdingTime = new HoldingTime(
                                dbConfig.getConfigRev(),
                                new Date(arrivalPrediction.getPredictionTime() + holdingTimeValue),
                                SystemTime.getDate(),
                                arrivalPrediction.getVehicleId(),
                                arrivalPrediction.getStopId(),
                                arrivalPrediction.getTripId(),
                                arrivalPrediction.getRouteId(),
                                true,
                                false,
                                new Date(arrivalPrediction.getPredictionTime()),
                                true,
                                N.length);

                        logger.debug("Holding time for : {} is {}.", arrivalPrediction, holdingTime);
                        if (HoldingConfig.storeHoldingTimes.getValue())
                            dataDbLogger.add(holdingTime);

                    } else if (lastDeparture.getTime().getTime() <= forwardDeparturePrediction.getPredictionTime()) {
                        long holdingTimeValue = calculateHoldingTime(
                                arrivalPrediction.getPredictionTime(),
                                lastDeparture.getTime().getTime(),
                                N,
                                HoldingConfig.maxPredictionsForHoldingTimeCalculation.getValue());

                        holdingTime = new HoldingTime(
                                dbConfig.getConfigRev(),
                                new Date(arrivalPrediction.getPredictionTime() + holdingTimeValue),
                                SystemTime.getDate(),
                                arrivalPrediction.getVehicleId(),
                                arrivalPrediction.getStopId(),
                                arrivalPrediction.getTripId(),
                                arrivalPrediction.getRouteId(),
                                true,
                                false,
                                new Date(arrivalPrediction.getPredictionTime()),
                                true,
                                N.length);

                        logger.debug("Holding time for : {} is {}.", arrivalPrediction, holdingTime);
                        if (HoldingConfig.storeHoldingTimes.getValue())
                            dataDbLogger.add(holdingTime);
                    }
                } else if (forwardDeparturePrediction != null) {
                    long holdingTimeValue = calculateHoldingTime(
                            arrivalPrediction.getPredictionTime(),
                            forwardDeparturePrediction.getPredictionTime(),
                            N,
                            HoldingConfig.maxPredictionsForHoldingTimeCalculation.getValue());

                    holdingTime = new HoldingTime(
                            dbConfig.getConfigRev(),
                            new Date(arrivalPrediction.getPredictionTime() + holdingTimeValue),
                            SystemTime.getDate(),
                            arrivalPrediction.getVehicleId(),
                            arrivalPrediction.getStopId(),
                            arrivalPrediction.getTripId(),
                            arrivalPrediction.getRouteId(),
                            true,
                            false,
                            new Date(arrivalPrediction.getPredictionTime()),
                            true,
                            N.length);
                    logger.debug("Holding time for : {} is {}.", arrivalPrediction, holdingTime);
                    if (HoldingConfig.storeHoldingTimes.getValue()) {
                        dataDbLogger.add(holdingTime);
                    }

                } else if (lastDeparture != null) {
                    long holdingTimeValue = calculateHoldingTime(
                            arrivalPrediction.getPredictionTime(),
                            lastDeparture.getTime().getTime(),
                            N,
                            HoldingConfig.maxPredictionsForHoldingTimeCalculation.getValue());

                    holdingTime = new HoldingTime(
                            dbConfig.getConfigRev(),
                            new Date(arrivalPrediction.getPredictionTime() + holdingTimeValue),
                            SystemTime.getDate(),
                            arrivalPrediction.getVehicleId(),
                            arrivalPrediction.getStopId(),
                            arrivalPrediction.getTripId(),
                            arrivalPrediction.getRouteId(),
                            true,
                            false,
                            new Date(arrivalPrediction.getPredictionTime()),
                            true,
                            N.length);
                    logger.debug("Holding time for : {} is {}.", arrivalPrediction, holdingTime);
                    if (HoldingConfig.storeHoldingTimes.getValue()) {
                        dataDbLogger.add(holdingTime);
                    }
                }

            } else {
                if (forwardDeparturePrediction != null || lastDeparture != null) {
                    Long holdingTimeValue = null;
                    if (lastDeparture != null && forwardDeparturePrediction != null) {
                        holdingTimeValue = calculateHoldingTime(
                                Math.min(
                                        lastDeparture.getTime().getTime(),
                                        forwardDeparturePrediction.getPredictionTime()),
                                lastDeparture.getTime().getTime());
                    } else if (lastDeparture != null && forwardDeparturePrediction == null) {
                        holdingTimeValue = calculateHoldingTime(
                                arrivalPrediction.getPredictionTime(),
                                lastDeparture.getTime().getTime());
                    } else if (lastDeparture == null && forwardDeparturePrediction != null) {
                        holdingTimeValue = calculateHoldingTime(
                                arrivalPrediction.getPredictionTime(), forwardDeparturePrediction.getPredictionTime());
                    }
                    if (holdingTimeValue != null) {
                        holdingTime = new HoldingTime(
                                dbConfig.getConfigRev(),
                                new Date(arrivalPrediction.getPredictionTime() + holdingTimeValue),
                                SystemTime.getDate(),
                                arrivalPrediction.getVehicleId(),
                                arrivalPrediction.getStopId(),
                                arrivalPrediction.getTripId(),
                                arrivalPrediction.getRouteId(),
                                true,
                                false,
                                new Date(arrivalPrediction.getPredictionTime()),
                                true,
                                0);

                        logger.debug("Holding time for : {} is {}.", arrivalPrediction, holdingTime);

                        if (HoldingConfig.storeHoldingTimes.getValue())
                            dataDbLogger.add(holdingTime);

                        return holdingTime;
                    } else {
                        logger.debug(
                                "Did not calucate holding time for some strange reason.",
                                arrivalPrediction.getStopId());
                        holdingTime = new HoldingTime(
                                dbConfig.getConfigRev(),
                                new Date(arrivalPrediction.getPredictionTime()),
                                SystemTime.getDate(),
                                arrivalPrediction.getVehicleId(),
                                arrivalPrediction.getStopId(),
                                arrivalPrediction.getTripId(),
                                arrivalPrediction.getRouteId(),
                                true,
                                true,
                                new Date(arrivalPrediction.getPredictionTime()),
                                false,
                                0);

                        if (HoldingConfig.storeHoldingTimes.getValue())
                            dataDbLogger.add(holdingTime);

                        return holdingTime;
                    }
                } else {
                    logger.debug(
                            "Did not find last vehicle departure for stop {}. This is required to"
                                    + " calculate holding time.",
                            arrivalPrediction.getStopId());
                    long current_vehicle_arrival_time = arrivalPrediction.getPredictionTime();
                    holdingTime = new HoldingTime(
                            dbConfig.getConfigRev(),
                            new Date(current_vehicle_arrival_time),
                            SystemTime.getDate(),
                            arrivalPrediction.getVehicleId(),
                            arrivalPrediction.getStopId(),
                            arrivalPrediction.getTripId(),
                            arrivalPrediction.getRouteId(),
                            true,
                            true,
                            new Date(arrivalPrediction.getPredictionTime()),
                            false,
                            0);

                    if (HoldingConfig.storeHoldingTimes.getValue())
                        dataDbLogger.add(holdingTime);

                    return holdingTime;
                }
            }
        }
        return holdingTime;
    }

    protected IpcPrediction getForwardVehicleDeparturePrediction(IpcPrediction predictionEvent) {
        List<IpcPrediction> predictions = new ArrayList<IpcPrediction>();

        List<IpcPredictionsForRouteStopDest> predictionsForRouteStopDests =
                predictionDataCache.getPredictions(predictionEvent.getRouteId(), predictionEvent.getStopId());

        for (IpcPredictionsForRouteStopDest predictionForRouteStopDest : predictionsForRouteStopDests) {
            for (IpcPrediction prediction : predictionForRouteStopDest.getPredictionsForRouteStop()) {
                predictions.add(prediction);
            }
        }
        Collections.sort(predictions, new PredictionTimeComparator());

        int found = -1;
        IpcPrediction closestPrediction = null;
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
                    closestPrediction = predictions.get(i);

                    return predictions.get(i);
                }
            }
        }

        return null;
    }

    protected List<IpcPrediction> getBackwardArrivalPredictions(IpcPrediction predictionEvent) {
        List<IpcPrediction> predictions = new ArrayList<IpcPrediction>();

        List<IpcPredictionsForRouteStopDest> predictionsForRouteStopDests =
                predictionDataCache.getPredictions(predictionEvent.getRouteId(), predictionEvent.getStopId());

        for (IpcPredictionsForRouteStopDest predictionForRouteStopDest : predictionsForRouteStopDests) {
            for (IpcPrediction prediction : predictionForRouteStopDest.getPredictionsForRouteStop()) {
                // logger.debug(prediction.toString());
                // TODO
                // if(prediction.getPredictionTime()>predictionEvent.getPredictionTime()&&prediction.isArrival())
                // The end of the route it seems to only generate departure predictions which points
                // towards an issue.
                // if(prediction.getPredictionTime()>predictionEvent.getPredictionTime())
                if (prediction.getPredictionTime() > predictionEvent.getPredictionTime()
                        && prediction.isArrival()
                        && !predictionEvent.getVehicleId().equals(prediction.getVehicleId()))
                    predictions.add(prediction);
            }
        }
        Collections.sort(predictions, new PredictionTimeComparator());
        /* TODO get the first two of the same type */
        return predictions;
    }

    @Override
    public List<ControlStop> getControlPointStops() {

        ArrayList<ControlStop> controlStops = new ArrayList<ControlStop>();

        for (String stopEntry : HoldingConfig.controlStopList.getValue()) {
            controlStops.add(new ControlStop(stopEntry));
        }
        return controlStops;
    }

    private Long[] predictionsToLongArray(List<IpcPrediction> predictions) {
        Long[] list = new Long[predictions.size()];

        if (predictions != null) {
            int i = 0;
            for (IpcPrediction prediction : predictions) {
                list[i] = prediction.getPredictionTime();
                i++;
            }
        }
        return list;
    }

    ArrayList<Long> predictionsToLongArrayList(List<IpcPrediction> predictions, ArrayList<Long> list) {
        if (predictions != null) {
            for (IpcPrediction prediction : predictions) {
                list.add(prediction.getPredictionTime());
            }
        }
        Collections.sort(list);
        return list;
    }

    private boolean isControlStop(String stopId) {
        ControlStop controlStop = new ControlStop(null, stopId);
        if (getControlPointStops() != null) {
            for (ControlStop controlStopInList : getControlPointStops()) {
                if (controlStopInList.getStopId().equals(controlStop.getStopId())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void handleDeparture(VehicleState vehicleState, ArrivalDeparture arrivalDeparture) {
        /* if it is a departure from a control stop */
        if (arrivalDeparture.isDeparture() && isControlStop(arrivalDeparture.getStopId())) {
            /* remove holding time once vehicle has left control point. */

            logger.debug(
                    "Removing holding time {} due to departure {}.", vehicleState.getHoldingTime(), arrivalDeparture);
            vehicleState.setHoldingTime(null);

            if (HoldingConfig.regenerateOnDeparture.getValue()) {
                for (HoldingTime holdingTime : getCurrentHoldingTimesForStop(arrivalDeparture.getStopId())) {
                    VehicleState otherState = vehicleStateManager.getVehicleState(holdingTime.getVehicleId());

                    IpcArrivalDeparture lastArrival = getLastVehicleArrivalEvent(
                            arrivalDeparture.getStopId(), otherState.getVehicleId(), arrivalDeparture.getAvlTime());

                    HoldingTime otherHoldingTime = generateHoldingTime(otherState, lastArrival);

                    holdingTimeCache.putHoldingTime(otherHoldingTime);

                    otherState.setHoldingTime(otherHoldingTime);
                }
            }
        }
    }
}
