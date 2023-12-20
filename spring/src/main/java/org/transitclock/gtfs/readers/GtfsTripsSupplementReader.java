/* (C)2023 */
package org.transitclock.gtfs.readers;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.transitclock.gtfs.GtfsTrip;
import org.transitclock.utils.csv.CsvBaseReader;

import java.text.ParseException;


@Component
public class GtfsTripsSupplementReader extends CsvBaseReader<GtfsTrip> {

    public GtfsTripsSupplementReader(String dirName) {
        super(dirName, "trips.txt", false, true);
    }

    @Override
    public GtfsTrip handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        return new GtfsTrip(record, supplemental, getFileName());
    }
}
