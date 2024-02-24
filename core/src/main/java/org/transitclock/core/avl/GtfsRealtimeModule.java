/* (C)2023 */
package org.transitclock.core.avl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.transitclock.config.StringConfigValue;
import org.transitclock.config.data.GtfsConfig;
import org.transitclock.domain.structs.AvlReport;
import org.transitclock.gtfs.realtime.GtfsRtVehiclePositionsReader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * For reading in feed of GTFS-realtime AVL data. Is used for both realtime feeds and for when
 * reading in a giant batch of data.
 *
 * @author SkiBu Smith
 */
@Slf4j
@Component
public class GtfsRealtimeModule extends PollUrlAvlModule {
    public GtfsRealtimeModule() {
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
        String[] urls = GtfsConfig.GTFS_REALTIME_URI.getValue().split(",");

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
        return new ArrayList<>(); // we've overridden getAndProcessData so this need not do anything
    }
}