/* (C)2023 */
package org.transitclock.gtfs.readers;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.transitclock.gtfs.GtfsStopTime;
import org.transitclock.utils.csv.CsvBaseReader;

import java.text.ParseException;


@Component
public class GtfsStopTimesSupplementReader extends CsvBaseReader<GtfsStopTime> {

    public GtfsStopTimesSupplementReader(String dirName) {
        super(dirName, "stop_times.txt", false, true);
    }

    @Override
    public GtfsStopTime handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        return new GtfsStopTime(record, supplemental, getFileName());
    }
}
