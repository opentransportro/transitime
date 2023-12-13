/* (C)2023 */
package org.transitclock.gtfs.gtfsStructs;

import java.text.ParseException;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.utils.csv.CsvBase;

/**
 * A GTFS calendar_dates object.
 *
 * @author SkiBu Smith
 */
public class GtfsCalendarDate extends CsvBase {

    private final String serviceId;
    private final String date;
    private final String exceptionType;

    /********************** Member Functions **************************/

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

    public String getServiceId() {
        return serviceId;
    }

    public String getDate() {
        return date;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    @Override
    public String toString() {
        return "GtfsCalendarDates [lineNumber="
                + lineNumber
                + ", serviceId="
                + serviceId
                + ", date="
                + date
                + ", exceptionType="
                + exceptionType
                + "]";
    }
}
