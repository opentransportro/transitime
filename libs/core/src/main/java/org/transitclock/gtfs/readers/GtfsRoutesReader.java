/* (C)2023 */
package org.transitclock.gtfs.readers;

import java.text.ParseException;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.gtfs.GtfsData;
import org.transitclock.gtfs.GtfsFilter;
import org.transitclock.gtfs.model.GtfsRoute;
import org.transitclock.utils.csv.CsvBaseReader;

/**
 * GTFS reader for the route.txt file
 *
 * @author SkiBu Smith
 */
public class GtfsRoutesReader extends CsvBaseReader<GtfsRoute> {
    private final GtfsFilter filter;
    public GtfsRoutesReader(String dirName, GtfsFilter filter) {
        super(dirName, "routes.txt", true, false);
        this.filter = filter;
    }

    @Override
    public GtfsRoute handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        if (filter.routeNotFiltered(record.get("route_id")))
            return new GtfsRoute(record, supplemental, getFileName());
        else return null;
    }
}
