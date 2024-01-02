/* (C)2023 */
package org.transitclock.feed.gtfsRt;

import java.util.ArrayList;
import java.util.List;
import org.transitclock.db.structs.AvlReport;

/**
 * Reads in GTFS-realtime Vehicle Positions file and converts them into List of AvlReport objects.
 * This class should be inherited from such that handleAvlReport() of the superclass will process
 * the AVL data one report at a time. This way don't have to fill up memory with a giant list of
 * AvlReports.
 *
 * @author SkiBu Smith
 */
public class GtfsRtVehiclePositionsReader extends GtfsRtVehiclePositionsReaderBase {

    private final List<AvlReport> avlReports = new ArrayList<>();

    /**
     * Simple constructor. Protected because should access this class through getAvlReports().
     *
     * @param urlString
     */
    protected GtfsRtVehiclePositionsReader(String urlString) {
        super(urlString);
    }

    /** Called for each AvlReport processed. Adds the report to the list of AvlReports. */
    @Override
    protected void handleAvlReport(AvlReport avlReport) {
        avlReports.add(avlReport);
    }

    /**
     * Returns list of AvlReports read from GTFS-realtime file specified by urlString.
     *
     * @param urlString URL of GTFS-realtime file
     * @return List of AvlReports
     */
    public static List<AvlReport> getAvlReports(String urlString) {
        GtfsRtVehiclePositionsReader reader = new GtfsRtVehiclePositionsReader(urlString);

        reader.process();

        return reader.avlReports;
    }
}
