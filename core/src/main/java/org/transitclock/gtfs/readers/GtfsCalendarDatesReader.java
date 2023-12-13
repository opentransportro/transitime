/* (C)2023 */
package org.transitclock.gtfs.readers;

import java.text.ParseException;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.gtfs.gtfsStructs.GtfsCalendarDate;
import org.transitclock.utils.csv.CsvBaseReader;

/**
 * GTFS reader for the calendar_dates.txt file
 *
 * @author SkiBu Smith
 */
public class GtfsCalendarDatesReader extends CsvBaseReader<GtfsCalendarDate> {

    public GtfsCalendarDatesReader(String dirName) {
        super(dirName, "calendar_dates.txt", false, false);
    }

    @Override
    public GtfsCalendarDate handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        return new GtfsCalendarDate(record, supplemental, getFileName());
    }
}
