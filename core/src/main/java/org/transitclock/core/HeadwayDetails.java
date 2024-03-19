/* (C)2023 */
package org.transitclock.core;

import java.util.concurrent.TimeUnit;
import org.transitclock.domain.structs.ArrivalDeparture;
import org.transitclock.service.dto.IpcPrediction;

/**
 * @author Sean Óg Crudden Class to hold details of a single headway.
 */
public class HeadwayDetails {

    private IpcPrediction vehicleAheadPrediction = null;
    private ArrivalDeparture vehicleAheadArrival = null;
    private final IpcPrediction vehicleBehindPrediction = null;
    Long arrivalPrediction = null;

    public IpcPrediction getVehicleAheadPrediction() {
        return vehicleAheadPrediction;
    }

    public IpcPrediction getVehicleBehindPrediction() {
        return vehicleBehindPrediction;
    }

    public HeadwayDetails(Long arrivalPrediction, IpcPrediction vehicleAheadPrediction) {
        this.vehicleAheadPrediction = vehicleAheadPrediction;
        this.arrivalPrediction = arrivalPrediction;
    }

    public HeadwayDetails(Long arrivalPrediction, ArrivalDeparture vehicleAheadArrival) {
        this.vehicleAheadArrival = vehicleAheadArrival;
        this.arrivalPrediction = arrivalPrediction;
    }

    @Override
    public String toString() {
        return "HeadwayDetails [headway="
                + TimeUnit.MILLISECONDS.toMinutes(getHeadway())
                + " mins, vehicleId="
                + getOtherVehicleId()
                + ", prediction="
                + basedOnPrediction()
                + "]";
    }

    public Long getHeadway() {
        if (vehicleBehindPrediction != null && vehicleAheadPrediction != null)
            return vehicleBehindPrediction.getPredictionTime() - vehicleAheadPrediction.getPredictionTime();
        if (vehicleBehindPrediction != null && vehicleAheadArrival != null)
            return vehicleBehindPrediction.getPredictionTime() - vehicleAheadArrival.getTime();
        if (arrivalPrediction != null && vehicleAheadArrival != null)
            return arrivalPrediction - vehicleAheadArrival.getTime();
        if (arrivalPrediction != null && vehicleAheadPrediction != null)
            return arrivalPrediction - vehicleAheadPrediction.getPredictionTime();

        return null;
    }

    public String getOtherVehicleId() {
        if (vehicleAheadPrediction != null) return vehicleAheadPrediction.getVehicleId();
        if (vehicleAheadArrival != null) return vehicleAheadArrival.getVehicleId();
        return null;
    }

    public boolean basedOnPrediction() {
        return vehicleAheadPrediction != null;
    }
}
