package org.transitclock.core.holdingmethod;

import org.transitclock.core.VehicleStatus;
import org.transitclock.domain.structs.ArrivalDeparture;
import org.transitclock.domain.structs.HoldingTime;
import org.transitclock.service.dto.IpcArrivalDeparture;
import org.transitclock.service.dto.IpcPrediction;

import java.util.List;

public class DummyHoldingTimeGeneratorImpl implements HoldingTimeGenerator {
    @Override
    public List<ControlStop> getControlPointStops() {
        return List.of();
    }

    @Override
    public HoldingTime generateHoldingTime(VehicleStatus vehicleStatus, IpcArrivalDeparture event) {
        return new HoldingTime();
    }

    @Override
    public HoldingTime generateHoldingTime(VehicleStatus vehicleStatus, IpcPrediction arrivalPrediction) {
        return new HoldingTime();
    }

    @Override
    public void handleDeparture(VehicleStatus vehicleStatus, ArrivalDeparture arrivalDeparture) {
        // nothing to do
    }
}
