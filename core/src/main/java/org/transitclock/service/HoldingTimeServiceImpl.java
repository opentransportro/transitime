/* (C)2023 */
package org.transitclock.service;

import java.rmi.RemoteException;

import lombok.extern.slf4j.Slf4j;
import org.transitclock.core.dataCache.HoldingTimeCache;
import org.transitclock.core.dataCache.HoldingTimeCacheKey;
import org.transitclock.core.dataCache.VehicleDataCache;
import org.transitclock.db.structs.HoldingTime;
import org.transitclock.service.dto.IpcHoldingTime;
import org.transitclock.service.contract.HoldingTimeInterface;

/**
 * @author Sean Og Crudden Server to allow stored travel time predictions to be queried. TODO May
 *     not be set to run by default as really only for analysis of predictions.
 */
@Slf4j
public class HoldingTimeServiceImpl implements HoldingTimeInterface {
    // Should only be accessed as singleton class
    private static HoldingTimeServiceImpl singleton;

    public static HoldingTimeInterface instance() {
        return singleton;
    }

    protected HoldingTimeServiceImpl() {
    }

    /**
     * Starts up the HoldingTimeServer so that RMI calls can be used to query holding times stored
     * in he cache. This will automatically cause the object to continue to run and serve requests.
     *
     * @return the singleton PredictionAnalysisServer object. Usually does not need to used since
     *     the server will be fully running.
     */
    public static HoldingTimeServiceImpl start() {
        if (singleton == null) {
            singleton = new HoldingTimeServiceImpl();
        }
        return singleton;
    }

    @Override
    public IpcHoldingTime getHoldTime(String stopId, String vehicleId, String tripId) throws RemoteException {

        if (tripId == null) {
            if (VehicleDataCache.getInstance().getVehicle(vehicleId) != null) {
                tripId = VehicleDataCache.getInstance().getVehicle(vehicleId).getTripId();
            }
        }
        if (stopId != null && vehicleId != null && tripId != null) {
            HoldingTimeCacheKey key = new HoldingTimeCacheKey(stopId, vehicleId, tripId);
            HoldingTime result = HoldingTimeCache.getInstance().getHoldingTime(key);
            if (result != null) return new IpcHoldingTime(result);
        }
        return null;
    }

    @Override
    public IpcHoldingTime getHoldTime(String stopId, String vehicleId) throws RemoteException {

        String tripId = null;
        if (VehicleDataCache.getInstance().getVehicle(vehicleId) != null) {
            tripId = VehicleDataCache.getInstance().getVehicle(vehicleId).getTripId();
        }
        if (stopId != null && vehicleId != null && tripId != null) {
            HoldingTimeCacheKey key = new HoldingTimeCacheKey(stopId, vehicleId, tripId);
            HoldingTime result = HoldingTimeCache.getInstance().getHoldingTime(key);
            if (result != null) return new IpcHoldingTime(result);
        }
        return null;
    }
}
