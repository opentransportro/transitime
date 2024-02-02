/* (C)2023 */
package org.transitclock.service;

import java.rmi.RemoteException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.transitclock.SingletonContainer;
import org.transitclock.core.dataCache.HoldingTimeCache;
import org.transitclock.core.dataCache.HoldingTimeCacheKey;
import org.transitclock.core.dataCache.VehicleDataCache;
import org.transitclock.domain.structs.HoldingTime;
import org.transitclock.service.dto.IpcHoldingTime;
import org.transitclock.service.contract.HoldingTimeInterface;

/**
 * @author Sean Og Crudden Server to allow stored travel time predictions to be queried. TODO May
 *     not be set to run by default as really only for analysis of predictions.
 */
@Service
@Slf4j
public class HoldingTimeServiceImpl implements HoldingTimeInterface {
    private final VehicleDataCache vehicleDataCache;
    private final HoldingTimeCache holdingTimeCache;

    public HoldingTimeServiceImpl(VehicleDataCache vehicleDataCache, HoldingTimeCache holdingTimeCache) {
        this.vehicleDataCache = vehicleDataCache;
        this.holdingTimeCache = holdingTimeCache;
    }


    @Override
    public IpcHoldingTime getHoldTime(String stopId, String vehicleId, String tripId) throws RemoteException {

        if (tripId == null) {
            if (vehicleDataCache.getVehicle(vehicleId) != null) {
                tripId = vehicleDataCache.getVehicle(vehicleId).getTripId();
            }
        }
        if (stopId != null && vehicleId != null && tripId != null) {
            HoldingTimeCacheKey key = new HoldingTimeCacheKey(stopId, vehicleId, tripId);
            HoldingTime result = holdingTimeCache.getHoldingTime(key);
            if (result != null) return new IpcHoldingTime(result);
        }
        return null;
    }

    @Override
    public IpcHoldingTime getHoldTime(String stopId, String vehicleId) throws RemoteException {

        String tripId = null;
        if (vehicleDataCache.getVehicle(vehicleId) != null) {
            tripId = vehicleDataCache.getVehicle(vehicleId).getTripId();
        }
        if (stopId != null && vehicleId != null && tripId != null) {
            HoldingTimeCacheKey key = new HoldingTimeCacheKey(stopId, vehicleId, tripId);
            HoldingTime result = holdingTimeCache.getHoldingTime(key);
            if (result != null) return new IpcHoldingTime(result);
        }
        return null;
    }
}
