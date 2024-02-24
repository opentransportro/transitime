/* (C)2023 */
package org.transitclock.core;

import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.transitclock.domain.structs.AvlReport;
import org.transitclock.domain.structs.Block;
import org.transitclock.domain.structs.Trip;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.utils.Time;

/**
 * Singleton class that handles block assignments from AVL feed.
 *
 * @author SkiBu Smith
 */
@Slf4j
@Component
public class BlockAssigner {
    private final DbConfig dbConfig;

    public BlockAssigner(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    /**
     * Gets the appropriate block associated with the AvlReport. If the assignment is a block
     * assignment then first gets the proper serviceIds that are active for the AVL timestamp, and
     * then determines the appropriate block using the serviceIds and the assignment from the AVL
     * report.
     *
     * <p>Works for block assignments, trip assignments, and trip short name assignments. If the
     * assignment not specified in AVL data or the block could not be found for the serviceIds, it
     * could not matched to the trip, or it was a route assignment then null will be returned
     *
     * @param avlReport So can determine the assignment ID, and the time so that so that can
     *     determine the proper service ID.
     * @return Block corresponding to the time and blockId from AVL report, or null if could not
     *     determine block.
     */
    public Block getBlockAssignment(AvlReport avlReport) {
        // If vehicle has assignment...
        if (avlReport != null && avlReport.getAssignmentId() != null) {
            // If using block assignment...
            if (avlReport.isBlockIdAssignmentType()) {
                Collection<String> serviceIds = dbConfig.getServiceUtils().getServiceIds(avlReport.getDate());
                // Go through all current service IDs to find the block
                // that is currently active
                Block activeBlock = null;
                for (String serviceId : serviceIds) {
                    Block blockForServiceId = dbConfig.getBlock(serviceId, avlReport.getAssignmentId());
                    // If there is a block for the current service ID
                    if (blockForServiceId != null) {
                        // If found a best match so far then remember it
                        if (activeBlock == null
                                || blockForServiceId.isActive(dbConfig, avlReport.getTime(), 90 * Time.MIN_IN_SECS)) {
                            activeBlock = blockForServiceId;
                            logger.debug(
                                    "For vehicleId={} and serviceId={} "
                                            + "the active block assignment from the "
                                            + "AVL feed is blockId={}",
                                    avlReport.getVehicleId(),
                                    serviceId,
                                    activeBlock.getId());
                        }
                    }
                }
                if (activeBlock == null) {
                    logger.error(
                            "For vehicleId={} AVL report specifies "
                                    + "blockId={} but block is not valid for "
                                    + "serviceIds={}",
                            avlReport.getVehicleId(),
                            avlReport.getAssignmentId(),
                            serviceIds);
                }
                return activeBlock;
            } else if (avlReport.isTripIdAssignmentType()) {
                // Using trip ID
                Trip trip = dbConfig.getTrip(avlReport.getAssignmentId());
                if (trip != null && trip.getBlock(dbConfig) != null) {
                    Block block = trip.getBlock(dbConfig);
                    logger.debug(
                            "For vehicleId={} the trip assignment from "
                                    + "the AVL feed is tripId={} which corresponds to "
                                    + "blockId={}",
                            avlReport.getVehicleId(),
                            avlReport.getAssignmentId(),
                            block.getId());
                    return block;
                } else {
                    logger.error(
                            "For vehicleId={} AVL report specifies "
                                    + "assignment tripId={} but that trip is not valid.",
                            avlReport.getVehicleId(),
                            avlReport.getAssignmentId());
                }
            } else if (avlReport.isTripShortNameAssignmentType()) {
                // Using trip short name
                String tripShortName = avlReport.getAssignmentId();
                Trip trip = dbConfig.getTripUsingTripShortName(tripShortName);
                if (trip != null) {
                    Block block = trip.getBlock(dbConfig);
                    logger.debug(
                            "For vehicleId={} the trip assignment from "
                                    + "the AVL feed is tripShortName={} which "
                                    + "corresponds to blockId={}",
                            avlReport.getVehicleId(),
                            tripShortName,
                            block.getId());
                    return block;
                } else {
                    logger.error(
                            "For vehicleId={} AVL report specifies "
                                    + "assignment tripShortName={} but that trip is not "
                                    + "valid.",
                            avlReport.getVehicleId(),
                            tripShortName);
                }
            }
        }

        // No valid block so return null
        return null;
    }

    /**
     * Returns the route ID specified in the AVL feed. If no route ID then returns null.
     *
     * @param avlReport
     * @return The route ID or null if none assigned
     */
    public String getRouteIdAssignment(AvlReport avlReport) {
        if (avlReport != null && avlReport.getAssignmentId() != null && avlReport.isRouteIdAssignmentType()) {
            // Route ID specified so return it
            return avlReport.getAssignmentId();
        }
        // No route ID specified in AVL feed so return null
        return null;
    }
}
