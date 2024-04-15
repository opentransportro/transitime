/* (C)2023 */
package org.transitclock.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.transitclock.core.dataCache.HoldingTimeCache;
import org.transitclock.core.dataCache.HoldingTimeCacheKey;
import org.transitclock.core.dataCache.VehicleDataCache;
import org.transitclock.domain.structs.HoldingTime;
import org.transitclock.service.contract.HoldingTimeInterface;
import org.transitclock.service.dto.IpcHoldingTime;

/**
 * @author Sean Og Crudden Server to allow stored travel time predictions to be queried. TODO May
 *     not be set to run by default as really only for analysis of predictions.
 */
@Slf4j
@Component
public class HoldingTimeServiceImpl implements HoldingTimeInterface {
    @Autowired
    private VehicleDataCache vehicleDataCache;
    @Autowired
    private HoldingTimeCache holdingTimeCache;


    @Override
    public IpcHoldingTime getHoldTime(String stopId, String vehicleId, String tripId) {
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
    public IpcHoldingTime getHoldTime(String stopId, String vehicleId) {

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
