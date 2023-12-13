/* (C)2023 */
package org.transitclock.gtfs.readers;

import java.text.ParseException;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.gtfs.GtfsData;
import org.transitclock.gtfs.gtfsStructs.GtfsRoute;
import org.transitclock.utils.csv.CsvBaseReader;

/**
 * GTFS reader for the route.txt file
 *
 * @author SkiBu Smith
 */
public class GtfsRoutesReader extends CsvBaseReader<GtfsRoute> {

    public GtfsRoutesReader(String dirName) {
        super(dirName, "routes.txt", true, false);
    }

    @Override
    public GtfsRoute handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        if (GtfsData.routeNotFiltered(record.get("route_id")))
            return new GtfsRoute(record, supplemental, getFileName());
        else return null;
    }
}
