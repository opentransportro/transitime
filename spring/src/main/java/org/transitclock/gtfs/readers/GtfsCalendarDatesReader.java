/* (C)2023 */
package org.transitclock.gtfs.readers;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.transitclock.gtfs.GtfsCalendarDate;
import org.transitclock.utils.csv.CsvBaseReader;

import java.text.ParseException;


@Component
public class GtfsCalendarDatesReader extends CsvBaseReader<GtfsCalendarDate> {

    public GtfsCalendarDatesReader(String dirName) {
        super(dirName, "calendar_dates.txt", false, false);
    }

    @Override
    public GtfsCalendarDate handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        return new GtfsCalendarDate(record, supplemental, getFileName());
    }
}
