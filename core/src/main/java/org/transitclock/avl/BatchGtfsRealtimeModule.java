/* (C)2023 */
package org.transitclock.avl;

import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.transitclock.Module;
import org.transitclock.applications.Core;
import org.transitclock.config.StringListConfigValue;
import org.transitclock.core.AvlProcessor;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.Location;
import org.transitclock.feed.gtfsRt.GtfsRtVehiclePositionsReader;
import org.transitclock.utils.Time;

/**
 * For reading in a batch of GTFS-realtime data and processing it. It only reads a single batch of
 * data, unlike the usual AVL modules that continuously read data. This module was created for the
 * World Bank project so that could determine actual arrival times based on batched GPS data and
 * then output more accurate schedule times for the GTFS stop_times.txt file.
 *
 * <p>The AVL data is processed directly by this class by it calling
 * AvlProcessor.processAvlReport(avlReport). The messages do not go through the JMS server and JMS
 * server does not need to be running.
 *
 * <p>Note: the URL for the GTFS-realtime feed is obtained in GtfsRealtimeModule from
 * CoreConfig.getGtfsRealtimeURI(). This means it can be set in the config file or as a Java
 * property on the command line.
 *
 * @author SkiBu Smith
 */
@Slf4j
public class BatchGtfsRealtimeModule extends Module {
    public static List<String> getGtfsRealtimeURIs() {
        return gtfsRealtimeURIs.getValue();
    }

    private static StringListConfigValue gtfsRealtimeURIs = new StringListConfigValue(
            "transitclock.avl.gtfsRealtimeFeedURIs",
            null,
            "Semicolon separated list of URIs of the GTFS-realtime data to read in");

    public BatchGtfsRealtimeModule(String agencyId) {
        super(agencyId);
    }

    /**
     * Zhengzhou had trouble providing valid GTFS-RT data. So this method can be used for Zhengzhou
     * to add logging info so that can see how many valid reports there are. Also filters out AVL
     * reports that are not within 15km of downtown Zhengzhou. It is only for debugging.
     */
    private void debugZhengzhou() {
        for (String uri : getGtfsRealtimeURIs()) {
            Collection<AvlReport> avlReports = GtfsRtVehiclePositionsReader.getAvlReports(uri);

            // Location of downtown Zhengzhou
            Location downtown = new Location(34.75, 113.65);

            // logger.info("The following AVL reports are within 15km of Zhengzhou");
            List<AvlReport> zhengzhouAvlReports = new ArrayList<AvlReport>();
            Set<String> zhengzhouVehicles = new HashSet<String>();
            Set<String> zhengzhouRoutes = new HashSet<String>();
            long earliestTime = Long.MAX_VALUE;
            long latestTime = Long.MIN_VALUE;
            for (AvlReport avlReport : avlReports) {
                if (avlReport.getLocation().distance(downtown) < 15000) {
                    // logger.info("Zhengzhou avlReport={}", avlReport);
                    zhengzhouAvlReports.add(avlReport);

                    zhengzhouVehicles.add(avlReport.getVehicleId());
                    zhengzhouRoutes.add(avlReport.getAssignmentId());

                    if (avlReport.getTime() < earliestTime) earliestTime = avlReport.getTime();
                    if (avlReport.getTime() > latestTime) latestTime = avlReport.getTime();
                }
            }
            logger.info(
                    "For Zhengzhou got {} AVl reports out of total of {}.",
                    zhengzhouAvlReports.size(),
                    avlReports.size());
            logger.info("For Zhengzhou found {} vehicles={}", zhengzhouVehicles.size(), zhengzhouVehicles);
            logger.info("For Zhengzhou found {} routes={}", zhengzhouRoutes.size(), zhengzhouRoutes);
            logger.info("Earliest AVL time was {} and latest was {}", new Date(earliestTime), new Date(latestTime));
        }
    }

    /*
     * Reads in AVL reports from GTFS-realtime file and processes them.
     *
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        // Zhengzhou had trouble providing valid GPS reports so this method
        // can be used to log debugging info and to filter out reports that
        // are not actually in Zhengzhou
        if (agencyId.equals("zhengzhouXX")) {
            debugZhengzhou();
        }

        // Process the VehiclePosition reports from the GTFS-realtime files
        for (String uri : getGtfsRealtimeURIs()) {
            Collection<AvlReport> avlReports = GtfsRtVehiclePositionsReader.getAvlReports(uri);
            for (AvlReport avlReport : avlReports) {
                // Update the Core SystemTime to use this AVL time
                Core.getInstance().setSystemTime(avlReport.getTime());

                // Actually process the AvlReport
                AvlProcessor.getInstance().processAvlReport(avlReport);
            }
        }

        // Done processing the batch data. Wait a bit more to make sure system
        // has chance to log all data to the database. Then exit.
        Time.sleep(5000);
        System.exit(0);
    }
}
