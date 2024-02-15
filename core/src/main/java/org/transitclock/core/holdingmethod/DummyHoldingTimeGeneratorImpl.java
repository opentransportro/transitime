package org.transitclock.core.holdingmethod;

import org.transitclock.core.VehicleState;
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
    public HoldingTime generateHoldingTime(VehicleState vehicleState, IpcArrivalDeparture event) {
        return new HoldingTime();
    }

    @Override
    public HoldingTime generateHoldingTime(VehicleState vehicleState, IpcPrediction arrivalPrediction) {
        return new HoldingTime();
    }

    @Override
    public void handleDeparture(VehicleState vehicleState, ArrivalDeparture arrivalDeparture) {
        // nothing to do
    }
}
