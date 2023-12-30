/* (C)2023 */
package org.transitclock.core.holdingmethod;

import java.util.List;
import org.transitclock.core.VehicleState;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.HoldingTime;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.ipc.data.IpcPrediction;

/**
 * @author Sean Óg Crudden
 */
public interface HoldingTimeGenerator {
    List<ControlStop> getControlPointStops();

    HoldingTime generateHoldingTime(VehicleState vehicleState, IpcArrivalDeparture event);

    HoldingTime generateHoldingTime(VehicleState vehicleState, IpcPrediction arrivalPrediction);

    void handleDeparture(VehicleState vehicleState, ArrivalDeparture arrivalDeparture);
}
