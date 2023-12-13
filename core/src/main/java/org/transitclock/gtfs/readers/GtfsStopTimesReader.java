/* (C)2023 */
package org.transitclock.gtfs.readers;

import java.text.ParseException;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.gtfs.GtfsData;
import org.transitclock.gtfs.gtfsStructs.GtfsStopTime;
import org.transitclock.utils.csv.CsvBaseReader;

/**
 * GTFS reader for the stop_times.txt file
 *
 * @author SkiBu Smith
 */
public class GtfsStopTimesReader extends CsvBaseReader<GtfsStopTime> {

    public GtfsStopTimesReader(String dirName) {
        super(dirName, "stop_times.txt", true, false);
    }

    @Override
    public GtfsStopTime handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        if (GtfsData.tripNotFiltered(record.get("trip_id")))
            return new GtfsStopTime(record, supplemental, getFileName());
        else return null;
    }
}
