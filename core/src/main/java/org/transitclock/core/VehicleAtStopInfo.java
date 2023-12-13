/* (C)2023 */
package org.transitclock.core;

import org.transitclock.applications.Core;
import org.transitclock.configData.CoreConfig;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.Trip;

/**
 * So that VehicleState can keep track of whether vehicle is matched to a stop.
 *
 * @author SkiBu Smith
 */
public class VehicleAtStopInfo extends Indices {

    /********************** Member Functions **************************/

    /**
     * @param block
     * @param tripIndex
     * @param stopPathIndex
     */
    public VehicleAtStopInfo(Block block, int tripIndex, int stopPathIndex) {
        super(block, tripIndex, stopPathIndex, 0); // segment index
    }

    /**
     * @param indices
     */
    public VehicleAtStopInfo(Indices indices) {
        super(indices.getBlock(), indices.getTripIndex(), indices.getStopPathIndex(), 0); // segment index
    }

    /**
     * Returns the stop ID of the stop
     *
     * @return
     */
    public String getStopId() {
        return getStopPath().getStopId();
    }

    @Override
    public String toString() {
        return "Indices ["
                + "blockId="
                + getBlock().getId()
                + ", tripIndex="
                + getTripIndex()
                + ", stopPathIndex="
                + getStopPathIndex()
                + ", stopId="
                + getStopId()
                + "]";
    }

    /**
     * For schedule based assignment returns true if tripIndex and stopPathIndex are for the very
     * last trip, path, segment for the block assignment. Had to override Indices.atEndOfBlock()
     * because this class doesn't use the segment index while Indices does.
     *
     * <p>But for no schedule based assignment then can't just look to see if at last stop because
     * since the vehicle will loop it will often be at the last stop. Therefore for no schedule
     * assignments this method looks at the current time to see if the block is still active.
     *
     * @return true if vehicle at end of block
     */
    @Override
    public boolean atEndOfBlock() {
        Block block = getBlock();
        if (block.isNoSchedule()) {
            // frequency based blocks last until the last trip completes
            Trip trip = getTrip();
            int tripDuration = trip.getEndTime() - trip.getStartTime();
            int blockDuration = block.getEndTime() - block.getStartTime();
            int secondsBeforeTrip = CoreConfig.getAllowableEarlySeconds();
            int secondsAfterTrip = CoreConfig.getAllowableLateSeconds();
            return !block.isActive(
                    Core.getInstance().getSystemDate(),
                    secondsBeforeTrip,
                    blockDuration + tripDuration + secondsAfterTrip);
        } else {
            return getTripIndex() == block.numTrips() - 1
                    && getStopPathIndex() == block.numStopPaths(getTripIndex()) - 1;
        }
    }
}
