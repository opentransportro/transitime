/* (C)2023 */
package org.transitclock.core.avl;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.transitclock.config.data.AgencyConfig;
import org.transitclock.config.data.AvlConfig;
import org.transitclock.core.AvlProcessor;
import org.transitclock.domain.structs.AvlReport;
import org.transitclock.utils.Time;

/**
 * Receives AVL data from the AvlExecutor or JMS, determines if AVL should be filtered, and
 * processes data that doesn't need to be filtered. Can use multiple threads to process data.
 *
 * @author SkiBu Smith
 */
@Getter
@Slf4j
public class AvlReportProcessor implements Runnable {
    // List of current AVL reports by vehicle. Useful for determining last
    // report so can filter out new report if the same as the old one.
    // Keyed on vehicle ID.
    private static final Map<String, AvlReport> avlReports = new HashMap<>();

    // The AVL report being processed
    private final AvlReport avlReport;

    private final AvlProcessor avlProcessor = AvlProcessor.getInstance();

    public AvlReportProcessor(AvlReport avlReport) {
        this.avlReport = avlReport;
    }

    /**
     * Filters out problematic AVL reports (such as for having invalid data, being in the past, or
     * too recent) and processes the ones that are good.
     */
    @Override
    public void run() {
        // Put a try/catch around everything so that if unexpected exception
        // occurs an e-mail is sent and the avl client thread isn't killed.
        try {
            // If the data is bad throw it out
            String errorMsg = avlReport.validateData();
            if (errorMsg != null) {
                logger.error("Throwing away avlReport {} because {}", avlReport, errorMsg);
                return;
            }

            // See if we should filter out reports
            synchronized (avlReports) {
                AvlReport previousReportForVehicle = avlReports.get(avlReport.getVehicleId());

                // If report the same time or older than don't need to process
                // it
                if (previousReportForVehicle != null && avlReport.getTime() <= previousReportForVehicle.getTime()) {
                    logger.debug("Throwing away AVL report because it is same time or older than the previous AVL report for the vehicle. New AVL report is {}. Previous valid AVL report is {}",
                            avlReport,
                            previousReportForVehicle);
                    return;
                }

                // If previous report happened too recently then don't want to
                // process it. This is important for when get AVL data for a vehicle
                // more frequently than is worthwhile, like every couple of seconds.
                if (previousReportForVehicle != null) {
                    long timeBetweenReportsSecs = (avlReport.getTime() - previousReportForVehicle.getTime()) / Time.MS_PER_SEC;
                    if (timeBetweenReportsSecs < AvlConfig.getMinTimeBetweenAvlReportsSecs()) {
                        // Log this but. Since this can happen very frequently
                        // (VTA has hundreds of vehicles reporting every second!)
                        // separated the logging into two statements in case want
                        // to make the first shorter one a warn message but keep
                        // the second more verbose one a debug statement.
                        logger.debug(
                                "AVL report for vehicleId={} for time {} is only {} seconds old which is too recent to previous report so not fully processing it. Just updating the vehicle's location in cache.",
                                avlReport.getVehicleId(),
                                avlReport.getTime(),
                                timeBetweenReportsSecs);
                        logger.debug(
                                "Not processing AVL report because the new "
                                        + "report is too close in time to the previous AVL "
                                        + "report for the vehicle. "
                                        + "transitclock.avl.minTimeBetweenAvlReportsSecs={} "
                                        + "secs. New AVL report is {}. Previous valid AVL "
                                        + "report is {}",
                                AvlConfig.getMinTimeBetweenAvlReportsSecs(),
                                avlReport,
                                previousReportForVehicle);

                        // But still want to update the vehicle cache with the
                        // latest report because doing so is cheap and it allows
                        // vehicles to move on map smoothly
                        avlProcessor.cacheAvlReportWithoutProcessing(avlReport);

                        // Done here since not processing this AVL report
                        return;
                    }
                }

                // Should handle the AVL report. Remember it so can possibly
                // filter the next one
                avlReports.put(avlReport.getVehicleId(), avlReport);
            }

            // Process the report
            avlProcessor.processAvlReport(avlReport);
        } catch (Exception e) {
            // Catch unexpected exceptions so that can continue to use the same
            // AVL thread even if there is an unexpected problem. Only let
            // Errors, such as OutOfMemory errors, through.
            logger.error("Something happened while processing {} for agencyId={}.", avlReport, AgencyConfig.getAgencyId(), e);
        }
    }
}
