/* (C)2023 */
package org.transitclock.core.holdingmethod;

import java.util.List;
import org.transitclock.core.VehicleState;
import org.transitclock.core.domain.ArrivalDeparture;
import org.transitclock.core.domain.HoldingTime;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.ipc.data.IpcPrediction;

/**
 * @author Sean Óg Crudden
 */
public interface HoldingTimeGenerator {
    public List<ControlStop> getControlPointStops();

    public HoldingTime generateHoldingTime(VehicleState vehicleState, IpcArrivalDeparture event);

    public HoldingTime generateHoldingTime(VehicleState vehicleState, IpcPrediction arrivalPrediction);

    public void handleDeparture(VehicleState vehicleState, ArrivalDeparture arrivalDeparture);
}
