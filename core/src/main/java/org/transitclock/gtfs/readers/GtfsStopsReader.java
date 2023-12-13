/* (C)2023 */
package org.transitclock.gtfs.readers;

import java.text.ParseException;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.gtfs.gtfsStructs.GtfsStop;
import org.transitclock.utils.csv.CsvBaseReader;

/**
 * GTFS reader for the stops.txt file
 *
 * @author SkiBu Smith
 */
public class GtfsStopsReader extends CsvBaseReader<GtfsStop> {

    public GtfsStopsReader(String dirName) {
        super(dirName, "stops.txt", true, false);
    }

    @Override
    public GtfsStop handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        return new GtfsStop(record, supplemental, getFileName());
    }
}
