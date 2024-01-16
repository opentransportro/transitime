/* (C)2023 */
package org.transitclock.gtfs.readers;

import java.text.ParseException;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.gtfs.model.GtfsStopTime;
import org.transitclock.utils.csv.CsvBaseReader;

/**
 * GTFS reader for supplemental stop_times.txt file
 *
 * @author Michael Smith
 */
public class GtfsStopTimesSupplementReader extends CsvBaseReader<GtfsStopTime> {

    public GtfsStopTimesSupplementReader(String dirName) {
        super(dirName, "stop_times.txt", false, true);
    }

    @Override
    public GtfsStopTime handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        return new GtfsStopTime(record, supplemental, getFileName());
    }
}
