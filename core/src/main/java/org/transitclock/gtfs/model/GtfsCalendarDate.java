/* (C)2023 */
package org.transitclock.gtfs.model;

import java.text.ParseException;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.utils.csv.CsvBase;

/**
 * A GTFS calendar_dates object.
 *
 * @author SkiBu Smith
 */
@ToString
@Getter
public class GtfsCalendarDate extends CsvBase {

    private final String serviceId;
    private final String date;
    private final String exceptionType;

    /**
     * Creates a GtfsRoute object by reading the data from the CSVRecord.
     *
     * @param record
     * @param supplemental
     * @param fileName for logging errors
     */
    public GtfsCalendarDate(CSVRecord record, boolean supplemental, String fileName) throws ParseException {
        super(record, supplemental, fileName);

        serviceId = getRequiredValue(record, "service_id");
        date = getRequiredValue(record, "date");
        exceptionType = getRequiredValue(record, "exception_type");
    }
}
