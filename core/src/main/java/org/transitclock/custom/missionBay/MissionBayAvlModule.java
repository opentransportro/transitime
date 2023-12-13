/* (C)2023 */
package org.transitclock.custom.missionBay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.avl.NextBusAvlModule;
import org.transitclock.db.structs.AvlReport;

/**
 * @author SkiBu Smith
 */
public class MissionBayAvlModule extends NextBusAvlModule {

    private static final Logger logger = LoggerFactory.getLogger(MissionBayAvlModule.class);

    /********************** Member Functions **************************/

    /**
     * @param agencyId
     */
    public MissionBayAvlModule(String agencyId) {
        super(agencyId);
    }

    /** Does normal handling of AVL report but also sends data to the SFMTA API. */
    protected void processAvlReport(AvlReport avlReport) {
        // Do the normal handling of the AVL report
        super.processAvlReport(avlReport);

        // Send the data to the SFMTA API
        logger.info("Batching avlReport to send to SFMTA API when have " + "enough reports. {}", avlReport);
        SfmtaApiCaller.postAvlReportWhenAppropriate(avlReport);
    }
}
