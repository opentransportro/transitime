/* (C)2023 */
package org.transitclock.gtfs.readers;

import java.text.ParseException;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.gtfs.GtfsData;
import org.transitclock.gtfs.GtfsFilter;
import org.transitclock.gtfs.model.GtfsStopTime;
import org.transitclock.utils.csv.CsvBaseReader;

/**
 * GTFS reader for the stop_times.txt file
 *
 * @author SkiBu Smith
 */
public class GtfsStopTimesReader extends CsvBaseReader<GtfsStopTime> {
    private final GtfsFilter filter;
    public GtfsStopTimesReader(String dirName, GtfsFilter filter) {
        super(dirName, "stop_times.txt", true, false);
        this.filter = filter;
    }

    @Override
    public GtfsStopTime handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        if (filter.tripNotFiltered(record.get("trip_id")))
            return new GtfsStopTime(record, supplemental, getFileName());

        return null;
    }
}
