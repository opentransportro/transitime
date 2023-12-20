/* (C)2023 */
package org.transitclock.ipc.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import org.transitclock.core.domain.Trip;

/**
 * For IPC for obtaining currently active blocks. Contains both block information plus info on
 * vehicle assigned to the block.
 *
 * @author SkiBu Smith
 */
@ToString
public class IpcActiveBlock implements Serializable {
    @Getter
    private final IpcBlock block;
    @Getter
    private final int activeTripIndex;
    @Getter
    private final Collection<IpcVehicle> vehicles;

    // For sorting a collection of IpcActiveBlock objects. Sorting to be
    // done on server side only so marked as transient.
    @Getter(AccessLevel.PRIVATE)
    private final transient Trip tripForSorting;

    public IpcActiveBlock(
            IpcBlock ipcBlock, int activeTripIndex, Collection<IpcVehicle> ipcVehicles, Trip tripForSorting) {
        this.block = ipcBlock;
        this.activeTripIndex = activeTripIndex;
        this.vehicles = ipcVehicles;

        this.tripForSorting = tripForSorting;
    }

    /**
     * For sorting a collection of IpcActiveBlock objects such that ordered by route order and then
     * by block start time.
     */
    private static final Comparator<IpcActiveBlock> blockComparator = new Comparator<>() {
        /**
         * Returns negative if b1<b2, zero if b1=b2, and positive if b1>b2
         */
        @Override
        public int compare(IpcActiveBlock b1, IpcActiveBlock b2) {
            if (b1 == null && b2 == null) return 0;
            if (b1 == null
                    || b1.getTripForSorting() == null
                    || b1.getTripForSorting().getRoute() == null
                    || b1.getTripForSorting().getRoute().getRouteOrder() == null) return -1;
            if (b2 == null
                    || b2.getTripForSorting() == null
                    || b2.getTripForSorting().getRoute() == null
                    || b2.getTripForSorting().getRoute().getRouteOrder() == null) return 1;

            int routeOrder1 = b1.getTripForSorting().getRoute().getRouteOrder();
            int routeOrder2 = b2.getTripForSorting().getRoute().getRouteOrder();

            if (routeOrder1 < routeOrder2) return -1;
            if (routeOrder1 > routeOrder2) return 1;

            // Route order is the same so order by trip start time
            int blockStartTime1 = b1.getTripForSorting().getBlock().getStartTime();
            int blockStartTime2 = b2.getTripForSorting().getBlock().getStartTime();
            if (blockStartTime1 < blockStartTime2) return -1;
            if (blockStartTime1 > blockStartTime2) return 1;
            return 0;
        }
    };

    /**
     * Sorts the list of IpcActiveBlock objects by route and then by trip start time
     *
     * @param activeBlocks
     */
    public static void sort(List<IpcActiveBlock> activeBlocks) {
        activeBlocks.sort(blockComparator);
    }
}
