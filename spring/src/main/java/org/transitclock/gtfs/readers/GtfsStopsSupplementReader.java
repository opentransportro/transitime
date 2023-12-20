/* (C)2023 */
package org.transitclock.gtfs.readers;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.transitclock.gtfs.GtfsStop;
import org.transitclock.utils.csv.CsvBaseReader;

import java.text.ParseException;


@Component
public class GtfsStopsSupplementReader extends CsvBaseReader<GtfsStop> {

    public GtfsStopsSupplementReader(String dirName) {
        super(dirName, "stops.txt", false, true);
    }

    @Override
    public GtfsStop handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        return new GtfsStop(record, supplemental, getFileName());
    }
}
