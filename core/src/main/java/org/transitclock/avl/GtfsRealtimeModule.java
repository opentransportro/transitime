/* (C)2023 */
package org.transitclock.avl;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.transitclock.Module;
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

    // If debugging feed and want to not actually process
    // AVL reports to generate predictions and such then
    // set shouldProcessAvl to false;
    protected static boolean shouldProcessAvl = true;

    /*********** Configurable Parameters for this module ***********/
    public static String getGtfsRealtimeURI() {
        return gtfsRealtimeURI.getValue();
    }

    private static StringConfigValue gtfsRealtimeURI = new StringConfigValue(
            "transitclock.avl.gtfsRealtimeFeedURI",
            "file:///C:/Users/Mike/gtfsRealtimeData",
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

        String[] urls = getGtfsRealtimeURI().split(",");

        for (String urlStr : urls) {
            try {
                logger.info("reading {}", urlStr);
                List<AvlReport> avlReports = GtfsRtVehiclePositionsReader.getAvlReports(urlStr);
                logger.info("read complete");
                for (AvlReport avlReport : avlReports) {
                    processAvlReport(avlReport);
                }
                logger.info("processed {} reports for feed {}", avlReports.size(), urlStr);
            } catch (Exception any) {
                logger.error("issues processing feed {}:{}", urlStr, any, any);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.transitclock.avl.AvlModule#processData(java.io.InputStream)
     */
    @Override
    protected Collection<AvlReport> processData(InputStream inputStream) throws Exception {
        return null; // we've overriden getAndProcessData so this need not do anything
    }

    /** Just for debugging */
    public static void main(String[] args) {
        // Create a GtfsRealtimeModule for testing
        Module.start("org.transitclock.avl.GtfsRealtimeModule");
    }
}
