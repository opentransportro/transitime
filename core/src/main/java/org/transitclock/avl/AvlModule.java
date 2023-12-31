/* (C)2023 */
package org.transitclock.avl;

import java.util.Collection;
import org.transitclock.Module;
import org.transitclock.db.structs.AvlReport;

/**
 * Low-level abstract AVL module class that handles the processing of the data. Uses JMS to queue
 * the data if JMS enabled.
 *
 * @author SkiBu Smith
 */
public abstract class AvlModule extends Module {
    protected AvlModule(String agencyId) {
        super(agencyId);
    }

    /**
     * Processes AVL report read from feed. To be called from the subclass for each AVL report. Can
     * use JMS or bypass it, depending on how configured.
     */
    protected void processAvlReport(AvlReport avlReport) {
        // Use AvlExecutor to actually process the data using a thread executor
        AvlExecutor.getInstance().processAvlReport(avlReport);
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
}
