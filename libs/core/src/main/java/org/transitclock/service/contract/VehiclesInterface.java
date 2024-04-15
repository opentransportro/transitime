/* (C)2023 */
package org.transitclock.service.contract;

import org.transitclock.service.dto.IpcActiveBlock;
import org.transitclock.service.dto.IpcVehicle;
import org.transitclock.service.dto.IpcVehicleComplete;
import org.transitclock.service.dto.IpcVehicleConfig;
import org.transitclock.service.dto.IpcVehicleGtfsRealtime;
import org.transitclock.service.dto.IpcVehicleToBlockConfig;

import java.util.Collection;

/**
 * Defines the RMI interface used for obtaining vehicle information.
 *
 * @author SkiBu Smith
 */
public interface VehiclesInterface {

    /**
     * Gets from the server IpcActiveBlocks for blocks that are currently active without vehicle
     * data.
     *
     * @param routeIds List of routes that want data for. Can also be null or empty.
     * @param allowableBeforeTimeSecs How much before the block time the block is considered to be
     *     active
     * @return Collection of blocks that are active
     */
    Collection<IpcActiveBlock> getActiveBlocksWithoutVehicles(Collection<String> routeIds, int allowableBeforeTimeSecs);

    /**
     * Gets from the server IpcActiveBlocks for blocks that are currently active with vehicle data
     * for a particular route.
     *
     * @param routeId Route that want data for. Can also be null or empty.
     * @param allowableBeforeTimeSecs How much before the block time the block is considered to be
     *     active
     * @return Collection of blocks that are active
     */
    Collection<IpcActiveBlock> getActiveBlocksAndVehiclesByRouteId(String routeId, int allowableBeforeTimeSecs)
           ;

    /**
     * Gets from the server IpcActiveBlocks for blocks that are currently active with vehicle data
     * for all routes with given route name.
     *
     * @param routeName Route name that want data for. Can also be null or empty.
     * @param allowableBeforeTimeSecs How much before the block time the block is considered to be
     *     active
     * @return Collection of blocks that are active
     */
    Collection<IpcActiveBlock> getActiveBlocksAndVehiclesByRouteName(String routeName, int allowableBeforeTimeSecs)
           ;

    /**
     * For getting configuration information for all vehicles. Useful for determining IDs of all
     * vehicles in system
     *
     * @return Collection of IpcVehicleConfig objects
     */
    Collection<IpcVehicleConfig> getVehicleConfigs();

    /**
     * Gets from server IpcVehicle info for all vehicles.
     *
     * @return Collection of IpcVehicle objects
     */
    Collection<IpcVehicle> get();

    /**
     * Gets from server IpcCompleteVehicle info for all vehicles.
     *
     * @return Collection of Vehicle objects
     */
    Collection<IpcVehicleComplete> getComplete();

    /**
     * Gets from server IpcCompleteVehicle info for all vehicles.
     *
     * @return Collection of Vehicle objects
     */
    Collection<IpcVehicleGtfsRealtime> getGtfsRealtime();

    /**
     * Gets from server IpcVehicle info for specified vehicle.
     *
     * @param vehicleId ID of vehicle to get data for
     * @return info for specified vehicle
     */
    IpcVehicle get(String vehicleId);

    /**
     * Gets from server IpcCompleteVehicle info for specified vehicle.
     *
     * @param vehicleId ID of vehicle to get data for
     * @return info for specified vehicle
     */
    IpcVehicleComplete getComplete(String vehicleId);

    /**
     * Gets from server IpcVehicle info for vehicles specified by vehicles parameter.
     *
     * @param vehicleIds Collection of vehicle IDs to get Vehicle data for.
     * @return Collection of Vehicle objects
     */
    Collection<IpcVehicle> get(Collection<String> vehicleIds);

    /**
     * Gets from server IpcCompleteVehicle info for vehicles specified by vehicles parameter.
     *
     * @param vehicleIds Collection of vehicle IDs to get Vehicle data for.
     * @return Collection of Vehicle objects
     */
    Collection<IpcVehicleComplete> getComplete(Collection<String> vehicleIds);

    /**
     * Gets from server IpcVehicle info for all vehicles currently associated with route.
     *
     * @param routeIdOrShortName Specifies which route to get Vehicle data for
     * @return Collection of Vehicle objects
     */
    Collection<IpcVehicle> getForRoute(String routeIdOrShortName);

    /**
     * Gets from server IpcVehicle info for all vehicles currently associated with a block. Each
     * block should have a IpcVehicle in the returned collection.
     *
     * @return Collection of Vehicle objects
     */
    Collection<IpcVehicle> getVehiclesForBlocks();

    /**
     * Gets from server IpcCompleteVehicle info for all vehicles currently. associated with route.
     *
     * @param routeIdOrShortName Specifies which route to get Vehicle data for
     * @return Collection of Vehicle objects
     */
    Collection<IpcVehicleComplete> getCompleteForRoute(String routeIdOrShortName);

    /**
     * Gets from server IpcVehicle info for all vehicles currently. associated with route.
     *
     * @param routeIdsOrShortNames Specifies which routes to get Vehicle data for
     * @return Collection of Vehicle objects
     */
    Collection<IpcVehicle> getForRoute(Collection<String> routeIdsOrShortNames);

    /**
     * Gets from server IpcCompleteVehicle info for all vehicles currently. associated with route.
     *
     * @param routeIdsOrShortNames Specifies which routes to get Vehicle data for
     * @return Collection of Vehicle objects
     */
    Collection<IpcVehicleComplete> getCompleteForRoute(Collection<String> routeIdsOrShortNames);

    /**
     * Gets from the server IpcActiveBlocks for blocks that are currently active.
     *
     * @param routeIds List of routes that want data for. Can also be null or empty.
     * @param allowableBeforeTimeSecs How much before the block time the block is considered to be
     *     active
     * @return Collection of blocks that are active
     */
    Collection<IpcActiveBlock> getActiveBlocks(Collection<String> routeIds, int allowableBeforeTimeSecs)
           ;

    /**
     * Gets from the server the number of blocks that are currently active.
     *
     * @param routeIds List of routes that want data for. Can also be null or empty.
     * @param allowableBeforeTimeSecs How much before the block time the block is considered to be
     *     active
     * @return Number of blocks that are active.
     */
    int getNumActiveBlocks(Collection<String> routeIds, int allowableBeforeTimeSecs);

    Collection<IpcVehicleToBlockConfig> getVehicleToBlockConfig(String blockId);

    Collection<IpcVehicleToBlockConfig> getVehicleToBlockConfigByVehicleId(String vehicleId);
}
