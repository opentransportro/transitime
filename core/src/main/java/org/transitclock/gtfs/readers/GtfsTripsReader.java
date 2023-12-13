/* (C)2023 */
package org.transitclock.gtfs.readers;

import java.text.ParseException;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.gtfs.GtfsData;
import org.transitclock.gtfs.gtfsStructs.GtfsTrip;
import org.transitclock.utils.csv.CsvBaseReader;

/**
 * GTFS reader for the trips.txt file
 *
 * @author SkiBu Smith
 */
public class GtfsTripsReader extends CsvBaseReader<GtfsTrip> {

    public GtfsTripsReader(String dirName) {
        super(dirName, "trips.txt", true, false);
    }

    @Override
    public GtfsTrip handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        if (GtfsData.tripNotFiltered(record.get("trip_id")) && GtfsData.routeNotFiltered(record.get("route_id")))
            return new GtfsTrip(record, supplemental, getFileName());
        else return null;
    }
}
