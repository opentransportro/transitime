/* (C)2023 */
package org.transitclock.gtfs.readers;

import java.text.ParseException;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.gtfs.model.GtfsTrip;
import org.transitclock.utils.csv.CsvBaseReader;

/**
 * GTFS reader for supplemental trips.txt file
 *
 * @author SkiBu Smith
 */
public class GtfsTripsSupplementReader extends CsvBaseReader<GtfsTrip> {

    public GtfsTripsSupplementReader(String dirName) {
        super(dirName, "trips.txt", false, true);
    }

    @Override
    public GtfsTrip handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        return new GtfsTrip(record, supplemental, getFileName());
    }
}
