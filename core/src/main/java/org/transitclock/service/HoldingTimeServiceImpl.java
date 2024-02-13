/* (C)2023 */
package org.transitclock.service;

import lombok.extern.slf4j.Slf4j;
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
public class HoldingTimeServiceImpl implements HoldingTimeInterface {
    // Should only be accessed as singleton class
    private static HoldingTimeServiceImpl singleton;

    public static HoldingTimeInterface instance() {
        return singleton;
    }

    public static HoldingTimeServiceImpl start() {
        if (singleton == null) {
            singleton = new HoldingTimeServiceImpl();
        }
        return singleton;
    }

    public HoldingTimeServiceImpl() {

    }

    @Override
    public IpcHoldingTime getHoldTime(String stopId, String vehicleId, String tripId) {

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
    public IpcHoldingTime getHoldTime(String stopId, String vehicleId) {

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
