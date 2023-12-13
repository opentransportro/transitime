/* (C)2023 */
package org.transitclock.custom.irishrail;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.avl.GtfsRealtimeModule;
import org.transitclock.config.StringConfigValue;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.feed.gtfsRt.GtfsRtVehiclePositionsReader;

/**
 * This is a seperate module to read the GTFS realtime realtime data form the GTFS-RT vehicle
 * location url. This is to allow for the less accurate realtime info be read from else where.
 *
 * @author Sean Og Crudden
 */
public class GtfsRtAvlModule extends GtfsRealtimeModule {

    private static final Logger logger = LoggerFactory.getLogger(GtfsRtAvlModule.class);

    /*********** Configurable Parameters for this module ***********/
    public static String getGtfsRealtimeURI() {
        return gtfsRealtimeFeedURI.getValue();
    }

    private static StringConfigValue gtfsRealtimeFeedURI = new StringConfigValue(
            "transitclock.avl.gtfsRealtimeFeedURI",
            "http://developer.onebusaway.org/wmata-gtfsr/vehiclePositions",
            "The URI of the GTFS-realtime feed generated form the gtfs-realtime data.");

    /********************** Member Functions **************************/

    /**
     * @param projectId
     */
    public GtfsRtAvlModule(String projectId) {
        super(projectId);
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
                    avlReport.setSource("gtfsrt");
                    processAvlReport(avlReport);
                }
                logger.info("processed {} avl reports for feed {}", avlReports.size(), urlStr);
            } catch (Exception any) {
                logger.error("issues processing feed {}:{}", urlStr, any, any);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.transitclock.avl.AvlModule#getUrl()
     */
    @Override
    protected String getUrl() {
        return getGtfsRealtimeURI();
    }

    /* Not needed since all processing for this class is done in
     * the overridden getAndProcessData().
     * (non-Javadoc)
     * @see org.transitclock.avl.AvlModule#processData(java.io.InputStream)
     */
    @Override
    protected Collection<AvlReport> processData(InputStream in) throws Exception {
        return null; // we've override getAndProcessData so this need not do anything
    }
}
