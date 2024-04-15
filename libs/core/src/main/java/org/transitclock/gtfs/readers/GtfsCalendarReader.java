/* (C)2023 */
package org.transitclock.gtfs.readers;

import java.text.ParseException;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.gtfs.model.GtfsCalendar;
import org.transitclock.utils.csv.CsvBaseReader;

/**
 * @author SkiBu Smith
 */
public class GtfsCalendarReader extends CsvBaseReader<GtfsCalendar> {

    public GtfsCalendarReader(String dirName) {
        super(dirName, "calendar.txt", false, false);
    }

    @Override
    public GtfsCalendar handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        return new GtfsCalendar(record, supplemental, getFileName());
    }
}
