/* (C)2023 */
package org.transitclock.service.contract;

import org.transitclock.service.dto.IpcHoldingTime;

/**
 * Defines the RMI interface used for obtaining holding time information.
 *
 * @author Sean Og Crudden
 */
public interface HoldingTimeService {

    IpcHoldingTime getHoldTime(String stopId, String vehicleId, String tripId);

    IpcHoldingTime getHoldTime(String stopId, String vehicleId);
}
