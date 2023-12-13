/* (C)2023 */
package org.transitclock.custom.lametro;

import org.transitclock.avl.NextBusAvlModule;

/**
 * For lametro agency the block assignments from the feed don't match to the GTFS data. Therefore
 * this module must be used for the sfmta AVL feed.
 *
 * @author SkiBu Smith
 */
public class LametroNextBusAvlModule extends NextBusAvlModule {

    /**
     * @param agencyId
     */
    public LametroNextBusAvlModule(String agencyId) {
        super(agencyId);
    }

    /**
     * At least for sfmta agency they don't use a leading 0 in the block ID in the GTFS data.
     * Therefore to match strip out leading zeros from the block ID here.
     *
     * @param originalBlockIdFromFeed the block ID to be modified
     * @return the modified block ID that corresponds to the GTFS data
     */
    @Override
    protected String processBlockId(String originalBlockIdFromFeed) {
        String block = originalBlockIdFromFeed;
        while (block != null && block.startsWith("0")) block = block.substring(1);
        return block;
    }
}
