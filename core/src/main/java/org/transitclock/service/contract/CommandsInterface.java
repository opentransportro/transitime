/* (C)2023 */
package org.transitclock.service.contract;

import org.jvnet.hk2.annotations.Contract;
import org.transitclock.service.dto.IpcAvl;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;

/**
 * Defines the RMI interface for sending commands or data to the server (as opposed to for
 * requesting data).
 *
 * @author Michael
 */
@Contract
public interface CommandsInterface {

    /**
     * Sends AVL data to server.
     *
     * @param avlData
     * @return If error then contains error message string, otherwise null
     */
    String pushAvl(IpcAvl avlData);

    /**
     * Sends collection of AVL data to server.
     *
     * @param avlData collection of data
     * @return If error then contains error message string, otherwise null
     */
    String pushAvl(Collection<IpcAvl> avlData);

    /*
     * WIP This is to give a means of manually setting a vehicle unpredictable and unassigned so it will be reassigned quickly.
     */
    void setVehicleUnpredictable(String vehicleId);

    /*
     * Cancel a trip. It should exists in current predictions.
     * Retruns null on success
     */
    String cancelTrip(String tripId, LocalDateTime at);

    /*
     * Enable a canceled trip. It should exists in current predictions.
     * Retruns null on success
     */
    String reenableTrip(String tripId, LocalDateTime startTripTime);

    /*
     * Add vehicle to Block to predictions.
     * Returns null on success
     */
    String addVehicleToBlock(String vehicleId, String blockId, String tripId, Date assignmentDate, Date validFrom, Date validTo);

    /*
     * Add remove vehicle to block.
     * Returns null on success
     */
    String removeVehicleToBlock(long id);
}
