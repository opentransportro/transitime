/* (C)2023 */
package org.transitclock.custom.lametro;

import org.transitclock.avl.NextBusAvlModule;
import org.transitclock.config.StringConfigValue;

/**
 * For lametro agency using two separate AVL feeds so need a separate agency name for feed for rail.
 *
 * @author SkiBu Smith
 */
public class LametroRailNextBusAvlModule extends NextBusAvlModule {

    private static StringConfigValue agencyNameForFeed = new StringConfigValue(
            "transitclock.custom.lametro.agencyNameForLametroRailFeed",
            "If set then specifies the agency name to use for the "
                    + "feed. If not set then the transitclock.core.agencyId "
                    + "is used.");

    @Override
    protected String getAgencyNameForFeed() {
        return agencyNameForFeed.getValue();
    }

    /**
     * @param agencyId
     */
    public LametroRailNextBusAvlModule(String agencyId) {
        super(agencyId);
    }
}
