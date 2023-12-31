/* (C)2023 */
package org.transitclock.ipc.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import org.transitclock.ipc.data.IpcActiveBlock;
import org.transitclock.ipc.data.IpcVehicle;
import org.transitclock.ipc.data.IpcVehicleComplete;
import org.transitclock.ipc.data.IpcVehicleConfig;
import org.transitclock.ipc.data.IpcVehicleGtfsRealtime;
import org.transitclock.ipc.data.IpcVehicleToBlockConfig;

/**
 * Defines the RMI interface used for obtaining vehicle information.
 *
 * @author SkiBu Smith
 */
public interface VehiclesInterface extends Remote {

    /**
     * Gets from the server IpcActiveBlocks for blocks that are currently active without vehicle
     * data.
     *
     * @param routeIds List of routes that want data for. Can also be null or empty.
     * @param allowableBeforeTimeSecs How much before the block time the block is considered to be
     *     active
     * @return Collection of blocks that are active
     * @throws RemoteException
     */
    Collection<IpcActiveBlock> getActiveBlocksWithoutVehicles(
            Collection<String> routeIds, int allowableBeforeTimeSecs) throws RemoteException;

    /**
     * Gets from the server IpcActiveBlocks for blocks that are currently active with vehicle data
     * for a particular route.
     *
     * @param routeId Route that want data for. Can also be null or empty.
     * @param allowableBeforeTimeSecs How much before the block time the block is considered to be
     *     active
     * @return Collection of blocks that are active
     * @throws RemoteException
     */
    Collection<IpcActiveBlock> getActiveBlocksAndVehiclesByRouteId(String routeId, int allowableBeforeTimeSecs)
            throws RemoteException;

    /**
     * Gets from the server IpcActiveBlocks for blocks that are currently active with vehicle data
     * for all routes with given route name.
     *
     * @param routeName Route name that want data for. Can also be null or empty.
     * @param allowableBeforeTimeSecs How much before the block time the block is considered to be
     *     active
     * @return Collection of blocks that are active
     * @throws RemoteException
     */
    Collection<IpcActiveBlock> getActiveBlocksAndVehiclesByRouteName(
            String routeName, int allowableBeforeTimeSecs) throws RemoteException;

    /**
     * For getting configuration information for all vehicles. Useful for determining IDs of all
     * vehicles in system
     *
     * @return Collection of IpcVehicleConfig objects
     * @throws RemoteException
     */
    Collection<IpcVehicleConfig> getVehicleConfigs() throws RemoteException;

    /**
     * Gets from server IpcVehicle info for all vehicles.
     *
     * @return Collection of IpcVehicle objects
     * @throws RemoteException
     */
    Collection<IpcVehicle> get() throws RemoteException;

    /**
     * Gets from server IpcCompleteVehicle info for all vehicles.
     *
     * @return Collection of Vehicle objects
     * @throws RemoteException
     */
    Collection<IpcVehicleComplete> getComplete() throws RemoteException;

    /**
     * Gets from server IpcCompleteVehicle info for all vehicles.
     *
     * @return Collection of Vehicle objects
     * @throws RemoteException
     */
    Collection<IpcVehicleGtfsRealtime> getGtfsRealtime() throws RemoteException;

    /**
     * Gets from server IpcVehicle info for specified vehicle.
     *
     * @param vehicleId ID of vehicle to get data for
     * @return info for specified vehicle
     * @throws RemoteException
     */
    IpcVehicle get(String vehicleId) throws RemoteException;

    /**
     * Gets from server IpcCompleteVehicle info for specified vehicle.
     *
     * @param vehicleId ID of vehicle to get data for
     * @return info for specified vehicle
     * @throws RemoteException
     */
    IpcVehicleComplete getComplete(String vehicleId) throws RemoteException;

    /**
     * Gets from server IpcVehicle info for vehicles specified by vehicles parameter.
     *
     * @param vehicleIds Collection of vehicle IDs to get Vehicle data for.
     * @return Collection of Vehicle objects
     * @throws RemoteException
     */
    Collection<IpcVehicle> get(Collection<String> vehicleIds) throws RemoteException;

    /**
     * Gets from server IpcCompleteVehicle info for vehicles specified by vehicles parameter.
     *
     * @param vehicleIds Collection of vehicle IDs to get Vehicle data for.
     * @return Collection of Vehicle objects
     * @throws RemoteException
     */
    Collection<IpcVehicleComplete> getComplete(Collection<String> vehicleIds) throws RemoteException;

    /**
     * Gets from server IpcVehicle info for all vehicles currently associated with route.
     *
     * @param routeIdOrShortName Specifies which route to get Vehicle data for
     * @return Collection of Vehicle objects
     * @throws RemoteException
     */
    Collection<IpcVehicle> getForRoute(String routeIdOrShortName) throws RemoteException;

    /**
     * Gets from server IpcVehicle info for all vehicles currently associated with a block. Each
     * block should have a IpcVehicle in the returned collection.
     *
     * @return Collection of Vehicle objects
     * @throws RemoteException
     */
    Collection<IpcVehicle> getVehiclesForBlocks() throws RemoteException;

    /**
     * Gets from server IpcCompleteVehicle info for all vehicles currently. associated with route.
     *
     * @param routeIdOrShortName Specifies which route to get Vehicle data for
     * @return Collection of Vehicle objects
     * @throws RemoteException
     */
    Collection<IpcVehicleComplete> getCompleteForRoute(String routeIdOrShortName) throws RemoteException;

    /**
     * Gets from server IpcVehicle info for all vehicles currently. associated with route.
     *
     * @param routeIdsOrShortNames Specifies which routes to get Vehicle data for
     * @return Collection of Vehicle objects
     * @throws RemoteException
     */
    Collection<IpcVehicle> getForRoute(Collection<String> routeIdsOrShortNames) throws RemoteException;

    /**
     * Gets from server IpcCompleteVehicle info for all vehicles currently. associated with route.
     *
     * @param routeIdsOrShortNames Specifies which routes to get Vehicle data for
     * @return Collection of Vehicle objects
     * @throws RemoteException
     */
    Collection<IpcVehicleComplete> getCompleteForRoute(Collection<String> routeIdsOrShortNames)
            throws RemoteException;

    /**
     * Gets from the server IpcActiveBlocks for blocks that are currently active.
     *
     * @param routeIds List of routes that want data for. Can also be null or empty.
     * @param allowableBeforeTimeSecs How much before the block time the block is considered to be
     *     active
     * @return Collection of blocks that are active
     * @throws RemoteException
     */
    Collection<IpcActiveBlock> getActiveBlocks(Collection<String> routeIds, int allowableBeforeTimeSecs)
            throws RemoteException;

    /**
     * Gets from the server the number of blocks that are currently active.
     *
     * @param routeIds List of routes that want data for. Can also be null or empty.
     * @param allowableBeforeTimeSecs How much before the block time the block is considered to be
     *     active
     * @return Number of blocks that are active.
     * @throws RemoteException
     */
    int getNumActiveBlocks(Collection<String> routeIds, int allowableBeforeTimeSecs) throws RemoteException;

    Collection<IpcVehicleToBlockConfig> getVehicleToBlockConfig(String blockId) throws RemoteException;

    Collection<IpcVehicleToBlockConfig> getVehicleToBlockConfigByVehicleId(String vehicleId)
            throws RemoteException;
}
