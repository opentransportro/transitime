/* (C)2023 */
package org.transitclock.service.dto;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.transitclock.domain.structs.Trip;

/**
 * For IPC for obtaining currently active blocks. Contains both block information plus info on
 * vehicle assigned to the block.
 *
 * @author SkiBu Smith
 */
public class IpcActiveBlock implements Serializable {
    private final IpcBlock block;
    private final int activeTripIndex;
    private final Collection<IpcVehicle> vehicles;

    // For sorting a collection of IpcActiveBlock objects. Sorting to be
    // done on server side only so marked as transient.
    private final transient Trip tripForSorting;

    /**
     * Constructor
     *
     * @param ipcBlock
     * @param activeTripIndex
     * @param ipcVehicles
     */
    public IpcActiveBlock(
            IpcBlock ipcBlock, int activeTripIndex, Collection<IpcVehicle> ipcVehicles, Trip tripForSorting) {
        this.block = ipcBlock;
        this.activeTripIndex = activeTripIndex;
        this.vehicles = ipcVehicles;

        this.tripForSorting = tripForSorting;
    }

    @Override
    public String toString() {
        return "IpcBlockAndVehicle ["
                + "block="
                + block
                + ", activeTripIndex="
                + activeTripIndex
                + ", vehicles="
                + vehicles
                + "]";
    }

    public IpcBlock getBlock() {
        return block;
    }

    public int getActiveTripIndex() {
        return activeTripIndex;
    }

    public Collection<IpcVehicle> getVehicles() {
        return vehicles;
    }

    /**
     * This is a transient member so only valid on the server side. Intended for sorting collection
     * of IpcAtiveBlock objects.
     *
     * @return The current trip
     */
    public Trip getTripForSorting() {
        return tripForSorting;
    }

}
