/* (C)2023 */
package org.transitclock.gtfs.readers;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.transitclock.gtfs.GtfsTrip;
import org.transitclock.utils.csv.CsvBaseReader;

import java.text.ParseException;


@Component
public class GtfsTripsReader extends CsvBaseReader<GtfsTrip> {

    public GtfsTripsReader(String dirName) {
        super(dirName, "trips.txt", true, false);
    }

    @Override
    public GtfsTrip handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        return new GtfsTrip(record, supplemental, getFileName());
    }
}
