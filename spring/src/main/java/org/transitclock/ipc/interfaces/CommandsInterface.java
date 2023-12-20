/* (C)2023 */
package org.transitclock.ipc.interfaces;

import org.transitclock.ipc.data.IpcAvl;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;

/**
 * Defines the RMI interface for sending commands or data to the server (as opposed to for
 * requesting data).
 *
 * @author Michael
 */
public interface CommandsInterface extends Remote {

    /**
     * Sends AVL data to server.
     *
     * @param avlData
     * @return If error then contains error message string, otherwise null
     * @throws RemoteException
     */
    public String pushAvl(IpcAvl avlData) throws RemoteException;

    /**
     * Sends collection of AVL data to server.
     *
     * @param avlData collection of data
     * @return If error then contains error message string, otherwise null
     * @throws RemoteException
     */
    public String pushAvl(Collection<IpcAvl> avlData) throws RemoteException;

    /*
     * WIP This is to give a means of manually setting a vehicle unpredictable and unassigned so it will be reassigned quickly.
     */
    public void setVehicleUnpredictable(String vehicleId) throws RemoteException;

    /*
     * Cancel a trip. It should exists in current predictions.
     * Retruns null on success
     */
    public String cancelTrip(String tripId, LocalDateTime at) throws RemoteException;

    /*
     * Enable a canceled trip. It should exists in current predictions.
     * Retruns null on success
     */
    String reenableTrip(String tripId, LocalDateTime startTripTime) throws RemoteException;

    /*
     * Add vehicle to Block to predictions.
     * Returns null on success
     */
    public String addVehicleToBlock(
            String vehicleId, String blockId, String tripId, Date assignmentDate, Date validFrom, Date validTo)
            throws RemoteException;

    /*
     * Add remove vehicle to block.
     * Returns null on success
     */
    public String removeVehicleToBlock(long id) throws RemoteException;
}
