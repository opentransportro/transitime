/* (C)2023 */
package org.transitclock.avl.protobuf;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.transitclock.avl.PollUrlAvlModule;
import org.transitclock.config.StringConfigValue;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.feed.gtfsRt.GtfsRtVehiclePositionsReader;

/**
 * For reading in feed of GTFS-realtime AVL data. Is used for both realtime feeds and for when
 * reading in a giant batch of data.
 *
 * @author SkiBu Smith
 */
@Slf4j
public class GtfsRealtimeModule extends PollUrlAvlModule {
    private static final StringConfigValue GTFS_REALTIME_URI = new StringConfigValue(
            "transitclock.avl.gtfsRealtimeFeedURI",
            null,
            "The URI of the GTFS-realtime feed to use.");

    public GtfsRealtimeModule(String projectId) {
        super(projectId);
        // GTFS-realtime is already binary so don't want to get compressed
        // version since that would just be a waste.
        useCompression = false;
    }

    /**
     * Reads and processes the data. Called by AvlModule.run(). Reading GTFS-realtime doesn't use
     * InputSteram so overriding getAndProcessData().
     */
    @Override
    protected void getAndProcessData() {
        String[] urls = GTFS_REALTIME_URI.getValue().split(",");

        for (String urlStr : urls) {
            try {
                logger.info("Reading {}", urlStr);
                List<AvlReport> avlReports = GtfsRtVehiclePositionsReader.getAvlReports(urlStr);
                avlReports.forEach(this::processAvlReport);
                logger.info("Processed {} reports for feed {}", avlReports.size(), urlStr);
            } catch (Exception e) {
                logger.error("Issues processing feed {}", urlStr, e);
            }
        }
    }

    @Override
    protected Collection<AvlReport> processData(InputStream inputStream) throws Exception {
        return null; // we've overridden getAndProcessData so this need not do anything
    }
}
