/* (C)2023 */
package org.transitclock.avl;

import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.Module;
import org.transitclock.db.structs.AvlReport;

/**
 * Low-level abstract AVL module class that handles the processing of the data. Uses JMS to queue
 * the data if JMS enabled.
 *
 * @author SkiBu Smith
 */
public abstract class AvlModule extends Module {
    private static final Logger logger = LoggerFactory.getLogger(AvlModule.class);

    /********************** Member Functions **************************/

    /**
     * Constructor
     *
     * @param agencyId
     */
    protected AvlModule(String agencyId) {
        super(agencyId);
    }

    /**
     * Processes AVL report read from feed. To be called from the subclass for each AVL report. Can
     * use JMS or bypass it, depending on how configured.
     */
    protected void processAvlReport(AvlReport avlReport) {
        processAvlReportWithoutJms(avlReport);
    }

    /**
     * Processes entire collection of AVL reports read from feed by calling processAvlReport() on
     * each one.
     *
     * @param avlReports
     */
    protected void processAvlReports(Collection<AvlReport> avlReports) {
        for (AvlReport avlReport : avlReports) {
            processAvlReport(avlReport);
        }
    }

    /**
     * Instead of writing AVL report to JMS topic this method directly processes it. By doing this
     * one can bypass the need for a JMS server. Uses a thread executor so that can both use
     * multiple threads and queue up requests. This is especially important if getting a dump of AVL
     * data from an AVL feed hitting the Transitime web server and the AVL data getting then pushed
     * to the core system in batches.
     *
     * @param avlReport The AVL report to be processed
     */
    private void processAvlReportWithoutJms(AvlReport avlReport) {
        // Use AvlExecutor to actually process the data using a thread executor
        AvlExecutor.getInstance().processAvlReport(avlReport);
    }
}
