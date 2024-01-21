/* (C)2023 */
package org.transitclock.core;

import org.apache.commons.lang3.time.DateUtils;
import org.transitclock.applications.Core;
import org.transitclock.configData.PredictionConfig;
import org.transitclock.core.dataCache.*;
import org.transitclock.core.predictiongenerator.datafilter.TravelTimeDataFilter;
import org.transitclock.core.predictiongenerator.datafilter.TravelTimeFilterFactory;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.PredictionEvent;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.ipc.data.IpcPrediction;

import java.util.*;

/**
 * Defines the interface for generating predictions. To create predictions using an alternate method
 * simply implement this interface and configure PredictionGeneratorFactory to instantiate the new
 * class when a PredictionGenerator is needed.
 *
 * @author SkiBu Smith
 */
public abstract class PredictionGenerator {

    /**
     * Generates and returns the predictions for the vehicle.
     *
     * @param vehicleState Contains the new match for the vehicle that the predictions are to be
     *     based on.
     */
    public abstract List<IpcPrediction> generate(VehicleState vehicleState);



    protected TravelTimeDetails getLastVehicleTravelTime(VehicleState currentVehicleState, Indices indices) throws Exception {

        StopArrivalDepartureCacheKey nextStopKey = new StopArrivalDepartureCacheKey(
                indices.getStopPath().getStopId(),
                new Date(currentVehicleState.getMatch().getAvlTime()));

        /* TODO how do we handle the the first stop path. Where do we get the first stop id. */
        if (!indices.atBeginningOfTrip()) {
            String currentStopId = indices.getPreviousStopPath().getStopId();

            StopArrivalDepartureCacheKey currentStopKey = new StopArrivalDepartureCacheKey(
                    currentStopId, new Date(currentVehicleState.getMatch().getAvlTime()));

            List<IpcArrivalDeparture> currentStopList =
                    StopArrivalDepartureCacheFactory.getInstance().getStopHistory(currentStopKey);

            List<IpcArrivalDeparture> nextStopList =
                    StopArrivalDepartureCacheFactory.getInstance().getStopHistory(nextStopKey);

            if (currentStopList != null && nextStopList != null) {
                // lists are already sorted when put into cache.
                for (IpcArrivalDeparture currentArrivalDeparture : currentStopList) {

                    if (currentArrivalDeparture.isDeparture()
                            && !currentArrivalDeparture.getVehicleId().equals(currentVehicleState.getVehicleId())
                            && (currentVehicleState.getTrip().getDirectionId() == null
                                    || currentVehicleState
                                            .getTrip()
                                            .getDirectionId()
                                            .equals(currentArrivalDeparture.getDirectionId()))) {
                        IpcArrivalDeparture found;

                        if ((found = findMatchInList(nextStopList, currentArrivalDeparture)) != null) {
                            TravelTimeDetails travelTimeDetails = new TravelTimeDetails(currentArrivalDeparture, found);
                            if (travelTimeDetails.getTravelTime() > 0) {
                                return travelTimeDetails;

                            } else {
                                String description = found + " : " + currentArrivalDeparture;
                                PredictionEvent.create(
                                        currentVehicleState.getAvlReport(),
                                        currentVehicleState.getMatch(),
                                        PredictionEvent.TRAVELTIME_EXCEPTION,
                                        description,
                                        travelTimeDetails.getArrival().getStopId(),
                                        travelTimeDetails.getDeparture().getStopId(),
                                        travelTimeDetails.getArrival().getVehicleId(),
                                        travelTimeDetails.getArrival().getTime(),
                                        travelTimeDetails.getDeparture().getTime());
                                return null;
                            }
                        } else {
                            return null;
                        }
                    }
                }
            }
        }
        return null;
    }

    protected Indices getLastVehicleIndices(VehicleState currentVehicleState, Indices indices) {

        StopArrivalDepartureCacheKey nextStopKey = new StopArrivalDepartureCacheKey(
                indices.getStopPath().getStopId(),
                new Date(currentVehicleState.getMatch().getAvlTime()));

        /* TODO how do we handle the the first stop path. Where do we get the first stop id. */
        if (!indices.atBeginningOfTrip()) {
            String currentStopId = indices.getPreviousStopPath().getStopId();

            StopArrivalDepartureCacheKey currentStopKey = new StopArrivalDepartureCacheKey(
                    currentStopId, new Date(currentVehicleState.getMatch().getAvlTime()));

            List<IpcArrivalDeparture> currentStopList =
                    StopArrivalDepartureCacheFactory.getInstance().getStopHistory(currentStopKey);

            List<IpcArrivalDeparture> nextStopList =
                    StopArrivalDepartureCacheFactory.getInstance().getStopHistory(nextStopKey);

            if (currentStopList != null && nextStopList != null) {
                // lists are already sorted when put into cache.
                for (IpcArrivalDeparture currentArrivalDeparture : currentStopList) {

                    if (currentArrivalDeparture.isDeparture()
                            && !currentArrivalDeparture.getVehicleId().equals(currentVehicleState.getVehicleId())
                            && (currentVehicleState.getTrip().getDirectionId() == null
                                    || currentVehicleState
                                            .getTrip()
                                            .getDirectionId()
                                            .equals(currentArrivalDeparture.getDirectionId()))) {
                        IpcArrivalDeparture found;

                        if ((found = findMatchInList(nextStopList, currentArrivalDeparture)) != null) {
                            if (found.getTime().getTime()
                                            - currentArrivalDeparture.getTime().getTime()
                                    > 0) {
                                Block currentBlock = null;
                                /* block is transient in arrival departure so when read from database need to get from dbconfig. */

                                DbConfig dbConfig = Core.getInstance().getDbConfig();

                                currentBlock = dbConfig.getBlock(
                                        currentArrivalDeparture.getServiceId(), currentArrivalDeparture.getBlockId());

                                if (currentBlock != null)
                                    return new Indices(
                                            currentBlock,
                                            currentArrivalDeparture.getTripIndex(),
                                            found.getStopPathIndex(),
                                            0);
                            } else {
                                // must be going backwards
                                return null;
                            }
                        } else {
                            return null;
                        }
                    }
                }
            }
        }
        return null;
    }

    /* TODO could also make it a requirement that it is on the same route as the one we are generating prediction for */
    protected IpcArrivalDeparture findMatchInList(
            List<IpcArrivalDeparture> nextStopList, IpcArrivalDeparture currentArrivalDeparture) {
        for (IpcArrivalDeparture nextStopArrivalDeparture : nextStopList) {
            if (currentArrivalDeparture.getVehicleId().equals(nextStopArrivalDeparture.getVehicleId())
                    && currentArrivalDeparture.getTripId().equals(nextStopArrivalDeparture.getTripId())
                    && currentArrivalDeparture.isDeparture()
                    && nextStopArrivalDeparture.isArrival()) {
                return nextStopArrivalDeparture;
            }
        }
        return null;
    }

    protected VehicleState getClosetVechicle(
            List<VehicleState> vehiclesOnRoute, Indices indices, VehicleState currentVehicleState) {

        Map<String, List<String>> stopsByDirection =
                currentVehicleState.getTrip().getRoute().getOrderedStopsByDirection();

        List<String> routeStops =
                stopsByDirection.get(currentVehicleState.getTrip().getDirectionId());

        int closest = 100;

        VehicleState result = null;

        for (VehicleState vehicle : vehiclesOnRoute) {

            Integer numAfter = numAfter(
                    routeStops,
                    vehicle.getMatch().getStopPath().getStopId(),
                    currentVehicleState.getMatch().getStopPath().getStopId());
            if (numAfter != null && numAfter > PredictionConfig.closestVehicleStopsAhead.getValue() && numAfter < closest) {
                closest = numAfter;
                result = vehicle;
            }
        }
        return result;
    }

    boolean isAfter(List<String> stops, String stop1, String stop2) {
        if (stops != null && stop1 != null && stop2 != null) {
            if (stops.contains(stop1) && stops.contains(stop2)) {
                return stops.indexOf(stop1) > stops.indexOf(stop2);
            }
        }
        return false;
    }

    protected Integer numAfter(List<String> stops, String stop1, String stop2) {
        if (stops != null && stop1 != null && stop2 != null)
            if (stops.contains(stop1) && stops.contains(stop2)) return stops.indexOf(stop1) - stops.indexOf(stop2);

        return null;
    }

    protected List<TravelTimeDetails> lastDaysTimes(
            TripDataHistoryCacheInterface cache,
            String tripId,
            String direction,
            int stopPathIndex,
            Date startDate,
            Integer startTime,
            int num_days_look_back,
            int num_days) {

        List<IpcArrivalDeparture> results;
        List<TravelTimeDetails> times = new ArrayList<>();
        int num_found = 0;
        /*
         * TODO This could be smarter about the dates it looks at by looking at
         * which services use this trip and only 1ook on day service is
         * running
         */
        for (int i = 0; i < num_days_look_back && num_found < num_days; i++) {
            Date nearestDay = DateUtils.truncate(DateUtils.addDays(startDate, (i + 1) * -1), Calendar.DAY_OF_MONTH);

            TripKey tripKey = new TripKey(tripId, nearestDay, startTime);

            results = cache.getTripHistory(tripKey);

            if (results != null) {

                IpcArrivalDeparture arrival = getArrival(stopPathIndex, results);

                if (arrival != null) {
                    IpcArrivalDeparture departure =
                            TripDataHistoryCacheFactory.getInstance().findPreviousDepartureEvent(results, arrival);

                    if (arrival != null && departure != null) {

                        TravelTimeDetails travelTimeDetails = new TravelTimeDetails(departure, arrival);

                        if (travelTimeDetails.getTravelTime() != -1) {
                            TravelTimeDataFilter travelTimefilter = TravelTimeFilterFactory.getInstance();
                            if (!travelTimefilter.filter(
                                    travelTimeDetails.getDeparture(), travelTimeDetails.getArrival())) {
                                times.add(travelTimeDetails);
                                num_found++;
                            }
                        }
                    }
                }
            }
        }
        return times;
    }

    protected IpcArrivalDeparture getArrival(int stopPathIndex, List<IpcArrivalDeparture> results) {
        for (IpcArrivalDeparture result : results) {
            if (result.isArrival() && result.getStopPathIndex() == stopPathIndex) {
                return result;
            }
        }
        return null;
    }

    protected long timeBetweenStops(ArrivalDeparture ad1, ArrivalDeparture ad2) {
        return Math.abs(ad2.getTime() - ad1.getTime());
    }

    protected static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
        return iterable == null ? Collections.emptyList() : iterable;
    }
}
