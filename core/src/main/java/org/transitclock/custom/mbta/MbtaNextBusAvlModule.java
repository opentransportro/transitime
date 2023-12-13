/* (C)2023 */
package org.transitclock.custom.mbta;

import org.transitclock.avl.NextBusAvlModule;

/**
 * For mbta the block assignments from the feed don't match to the GTFS data. Therefore this module
 * must be used for the mbta feed.
 *
 * @author SkiBu Smith
 */
public class MbtaNextBusAvlModule extends NextBusAvlModule {

    /**
     * @param projectId
     */
    public MbtaNextBusAvlModule(String projectId) {
        super(projectId);
    }

    /**
     * At least for mbta the feed uses '_' characters in the block assignment but the GTFS data
     * instead uses '-' characters. Therefore need to process the block IDs.
     *
     * @param originalBlockIdFromFeed the block ID to be modified
     * @return the modified block ID that corresponds to the GTFS data
     */
    @Override
    protected String processBlockId(String originalBlockIdFromFeed) {
        if (originalBlockIdFromFeed == null) return null;
        else return originalBlockIdFromFeed.replace('_', '-');
    }
}
