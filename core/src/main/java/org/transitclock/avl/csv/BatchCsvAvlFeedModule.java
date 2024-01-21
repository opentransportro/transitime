/* (C)2023 */
package org.transitclock.avl.csv;

import lombok.extern.slf4j.Slf4j;
import org.transitclock.Module;
import org.transitclock.applications.Core;
import org.transitclock.configData.AvlConfig;
import org.transitclock.core.AvlProcessor;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.utils.Time;

import java.util.List;

/**
 * For reading in a batch of AVL data in CSV format and processing it. It only reads a single batch
 * of data, unlike the usual AVL modules that continuously read data. This module is useful for
 * debugging because can relatively easily create a plain text CSV file of AVL data and see what the
 * code does.
 *
 * <p>CSV columns include vehicleId, time (in epoch msec or as date string as in "9-14-2015
 * 12:53:01"), latitude, longitude, speed (optional), heading (optional), assignmentId, and
 * assignmentType (optional, but can be BLOCK_ID, ROUTE_ID, TRIP_ID, or TRIP_SHORT_NAME).
 *
 * @author SkiBu Smith
 */
@Slf4j
public class BatchCsvAvlFeedModule extends Module {

    // For running in real time
    private long lastAvlReportTimestamp = -1;


    /**
     * @param projectId
     */
    public BatchCsvAvlFeedModule(String projectId) {
        super(projectId);
    }

    /**
     * If configured to process data in real time them delay the appropriate amount of time
     *
     * @param avlReport
     */
    private void delayIfRunningInRealTime(AvlReport avlReport) {
        if (AvlConfig.processInRealTime.getValue()) {
            long delayLength = 0;

            if (lastAvlReportTimestamp > 0) {
                delayLength = avlReport.getTime() - lastAvlReportTimestamp;
                lastAvlReportTimestamp = avlReport.getTime();
            } else {
                lastAvlReportTimestamp = avlReport.getTime();
            }

            if (delayLength > 0) Time.sleep(delayLength);
        }
    }

    /*
     * Reads in AVL reports from CSV file and processes them.
     *
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        List<AvlReport> avlReports = (new AvlCsvReader(AvlConfig.getCsvAvlFeedFileName())).get();

        // Process the AVL Reports read in.
        for (AvlReport avlReport : avlReports) {

            logger.info("Processing avlReport={}", avlReport);

            // If configured to process data in real time them delay
            // the appropriate amount of time
            delayIfRunningInRealTime(avlReport);

            // Use the AVL report time as the current system time
            Core.getInstance().setSystemTime(avlReport.getTime());

            // Actually process the AVL report
            AvlProcessor.getInstance().processAvlReport(avlReport);

            // Post process if neccessary
            if (avlPostProcessor != null) avlPostProcessor.postProcess(avlReport);
        }

        // Kill off the whole program because done processing the AVL data
        String integrationTest = System.getProperty("transitclock.core.integrationTest");
        if (integrationTest != null) {
            System.setProperty("transitclock.core.csvImported", "true");
        } else {
            System.exit(0);
        }
    }

    private AvlPostProcessor avlPostProcessor = null;

    public void setAvlPostProcessor(AvlPostProcessor avlPostProcessor) {
        this.avlPostProcessor = avlPostProcessor;
    }

    public interface AvlPostProcessor {
        void postProcess(AvlReport avlReport);
    }
}
