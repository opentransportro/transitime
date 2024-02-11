/* (C)2023 */
package org.transitclock.service.contract;

import org.jvnet.hk2.annotations.Contract;
import org.transitclock.service.dto.IpcHoldingTime;

/**
 * Defines the RMI interface used for obtaining holding time information.
 *
 * @author Sean Og Crudden
 */
@Contract
public interface HoldingTimeInterface {

    IpcHoldingTime getHoldTime(String stopId, String vehicleId, String tripId);

    IpcHoldingTime getHoldTime(String stopId, String vehicleId);
}
