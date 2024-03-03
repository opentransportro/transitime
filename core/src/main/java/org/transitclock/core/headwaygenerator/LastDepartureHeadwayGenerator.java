/* (C)2023 */
package org.transitclock.core.headwaygenerator;

import org.transitclock.core.VehicleStatus;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheInterface;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheKey;
import org.transitclock.core.dataCache.VehicleDataCache;
import org.transitclock.core.dataCache.VehicleStatusManager;
import org.transitclock.domain.structs.Headway;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.service.dto.IpcArrivalDeparture;
import org.transitclock.service.dto.IpcVehicleComplete;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Sean Óg Crudden
 *     <p>This is a first pass at generating a Headway value. It will find the last departure time
 *     at the last stop for the vehicle and then get the vehicle ahead of it and check when it
 *     departed the same stop. The difference will be used as the headway.
 *     <p>This is a WIP
 *     <p>Maybe should be a list and have a predicted headway at each stop along the route. So key
 *     for headway could be (stop, vehicle, trip, start_time).
 */
class LastDepartureHeadwayGenerator implements HeadwayGenerator {
    private final VehicleDataCache vehicleDataCache;
    private final VehicleStatusManager vehicleStatusManager;
    private final StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface;
    private final DbConfig dbConfig;

    public LastDepartureHeadwayGenerator(VehicleDataCache vehicleDataCache, VehicleStatusManager vehicleStatusManager, StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface, DbConfig dbConfig) {
        this.vehicleDataCache = vehicleDataCache;
        this.vehicleStatusManager = vehicleStatusManager;
        this.stopArrivalDepartureCacheInterface = stopArrivalDepartureCacheInterface;
        this.dbConfig = dbConfig;
    }

    @Override
    public Headway generate(VehicleStatus vehicleStatus) {

        try {
            String stopId =
                    vehicleStatus.getMatch().getMatchAtPreviousStop().getAtStop().getStopId();
            long date = vehicleStatus.getMatch().getAvlTime();
            String vehicleId = vehicleStatus.getVehicleId();
            StopArrivalDepartureCacheKey key = new StopArrivalDepartureCacheKey(stopId, new Date(date));

            List<IpcArrivalDeparture> stopList = stopArrivalDepartureCacheInterface.getStopHistory(key);
            int lastStopArrivalIndex = -1;
            int previousVehicleArrivalIndex = -1;

            if (stopList != null) {
                for (int i = 0; i < stopList.size() && previousVehicleArrivalIndex == -1; i++) {
                    IpcArrivalDeparture arrivalDepature = stopList.get(i);
                    if (arrivalDepature.isDeparture()
                            && arrivalDepature.getStopId().equals(stopId)
                            && arrivalDepature.getVehicleId().equals(vehicleId)
                            && (vehicleStatus.getTrip().getDirectionId() == null
                                    || vehicleStatus
                                            .getTrip()
                                            .getDirectionId()
                                            .equals(arrivalDepature.getDirectionId()))) {
                        // This the arrival of this vehicle now the next arrival in the list will be
                        // the previous vehicle (The arrival of the vehicle ahead).
                        lastStopArrivalIndex = i;
                    }
                    if (lastStopArrivalIndex > -1
                            && arrivalDepature.isDeparture()
                            && arrivalDepature.getStopId().equals(stopId)
                            && !arrivalDepature.getVehicleId().equals(vehicleId)
                            && (vehicleStatus.getTrip().getDirectionId() == null
                                    || vehicleStatus
                                            .getTrip()
                                            .getDirectionId()
                                            .equals(arrivalDepature.getDirectionId()))) {
                        previousVehicleArrivalIndex = i;
                    }
                }
                if (previousVehicleArrivalIndex != -1 && lastStopArrivalIndex != -1) {
                    long headwayTime = Math.abs(
                            stopList.get(lastStopArrivalIndex).getTime().getTime()
                                    - stopList.get(previousVehicleArrivalIndex)
                                            .getTime()
                                            .getTime());

                    Headway headway = new Headway(
                            dbConfig.getConfigRev(),
                            headwayTime,
                            new Date(date),
                            vehicleId,
                            stopList.get(previousVehicleArrivalIndex).getVehicleId(),
                            stopId,
                            vehicleStatus.getTrip().getId(),
                            vehicleStatus.getTrip().getRouteId(),
                            new Date(
                                    stopList.get(lastStopArrivalIndex).getTime().getTime()),
                            new Date(stopList.get(previousVehicleArrivalIndex)
                                    .getTime()
                                    .getTime()));
                    // TODO Core.getInstance().getDbLogger().add(headway);

                    // remove rubish data from departure sfrom t
                    if (Math.abs(headway.getCreationTime().getTime()
                                            - headway.getFirstDeparture().getTime())
                                    > 1200000
                            || lastStopArrivalIndex > 5) {
                        headway = null;
                        vehicleStatus.setHeadway(null);
                        return null;
                    }
                    if (headway != null) {
                        if (vehicleStatus.getHeadway() == null
                                || !vehicleStatus.getHeadway().equals(headway)) {
                            vehicleStatus.setHeadway(headway);
                            setSystemVariance(headway);
                            return headway;
                        } else {
                            return null;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private void setSystemVariance(Headway headway) {
        List<Headway> headways = new ArrayList<>();

        int total_with_headway = 0;
        int total_vehicles = 0;
        boolean error = false;

        for (IpcVehicleComplete currentVehicle : vehicleDataCache.getVehicles()) {
            VehicleStatus vehicleStatus = vehicleStatusManager.getStatus(currentVehicle.getId());
            if (vehicleStatus.getHeadway() != null) {
                headways.add(vehicleStatus.getHeadway());
                total_with_headway++;
            }
            total_vehicles++;
        }
        // ONLY SET IF HAVE VALES FOR ALL VEHICLES ON ROUTE.
        if (vehicleDataCache.getVehicles().size() == headways.size()
                && total_vehicles == total_with_headway) {
            headway.setAverage(average(headways));
            headway.setVariance(variance(headways));
            headway.setCoefficientOfVariation(coefficientOfVariance(headways));
            headway.setNumVehicles(headways.size());
        } else {
            headway.setAverage(-1);
            headway.setVariance(-1);
            headway.setCoefficientOfVariation(-1);
            headway.setNumVehicles(total_with_headway);
        }
    }

    private double average(List<Headway> headways) {
        double total = 0;
        for (Headway headway : headways) {
            total = total + headway.getHeadway();
        }
        return total / headways.size();
    }

    private double variance(List<Headway> headways) {
        double topline = 0;
        double average = average(headways);
        for (Headway headway : headways) {
            topline = topline + ((headway.getHeadway() - average) * (headway.getHeadway() - average));
        }
        return topline / headways.size();
    }

    private double coefficientOfVariance(List<Headway> headways) {
        double variance = variance(headways);
        double average = average(headways);

        return variance / (average * average);
    }
}
