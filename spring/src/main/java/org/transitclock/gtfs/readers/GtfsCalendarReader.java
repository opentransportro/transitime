/* (C)2023 */
package org.transitclock.gtfs.readers;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.transitclock.gtfs.GtfsCalendar;
import org.transitclock.utils.csv.CsvBaseReader;

import java.text.ParseException;


@Component
public class GtfsCalendarReader extends CsvBaseReader<GtfsCalendar> {

    public GtfsCalendarReader(String dirName) {
        super(dirName, "calendar.txt", false, false);
    }

    @Override
    public GtfsCalendar handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        return new GtfsCalendar(record, supplemental, getFileName());
    }
}
