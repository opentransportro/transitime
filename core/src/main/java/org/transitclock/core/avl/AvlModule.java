/* (C)2023 */
package org.transitclock.core.avl;

import java.util.Collection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.transitclock.ApplicationProperties;
import org.transitclock.Module;
import org.transitclock.domain.structs.AvlReport;

/**
 * Low-level abstract AVL module class that handles the processing of the data. Uses JMS to queue
 * the data if JMS enabled.
 *
 * @author SkiBu Smith
 */
@Slf4j
abstract class AvlModule extends Module {
    protected final AvlReportProcessor avlReportProcessor;
    protected final ApplicationProperties.Avl avlProperties;

    protected AvlModule(ApplicationProperties.Avl avlProperties, AvlReportProcessor avlReportProcessor) {
        this.avlProperties = avlProperties;
        this.avlReportProcessor = avlReportProcessor;
    }


    /**
     * Processes AVL report read from feed. To be called from the subclass for each AVL report. Can
     * use JMS or bypass it, depending on how configured.
     */
    protected void processAvlReport(AvlReport avlReport) {
        // Use AvlExecutor to actually process the data using a thread executor
        avlReportProcessor.process(avlReport);
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
