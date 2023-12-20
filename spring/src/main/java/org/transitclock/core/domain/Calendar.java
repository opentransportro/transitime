/* (C)2023 */
package org.transitclock.core.domain;

import lombok.*;
import lombok.experimental.Delegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.transitclock.utils.Time;
import org.transitclock.gtfs.GtfsCalendar;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Contains data from the calendar.txt GTFS file. This class is for reading/writing that data to the
 * db.
 */
@Data
@Document(collection = "Calendars")
public class Calendar implements Serializable {
    @Data
    @Builder(toBuilder = true, setterPrefix = "with")
    public static class Key {
        private final int configRev;
        private final String serviceId;
        private final Date startDate;
        private final Date endDate;
        private final boolean monday;
        private final boolean tuesday;
        private final boolean wednesday;
        private final boolean thursday;
        private final boolean friday;
        private final boolean saturday;
        private final boolean sunday;
    }

    @Id
    @Delegate
    private final Key key;

    // For outputting start and end date as strings
    private static final DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");

    // Logging
    public static final Logger logger = LoggerFactory.getLogger(Calendar.class);

    /**
     * Constructor
     *
     * @param configRev
     * @param gc
     * @param dateFormat
     */
    public Calendar(int configRev, GtfsCalendar gc, DateFormat dateFormat) {
        var keyBuilder = Key.builder()
                .withConfigRev(configRev)
                .withServiceId(gc.getServiceId())
                .withMonday(isSetToTrue(gc.getMonday()))
                .withTuesday(isSetToTrue(gc.getTuesday()))
                .withThursday(isSetToTrue(gc.getThursday()))
                .withWednesday(isSetToTrue(gc.getWednesday()))
                .withFriday(isSetToTrue(gc.getFriday()))
                .withSaturday(isSetToTrue(gc.getSaturday()))
                .withSunday(isSetToTrue(gc.getSunday()));
        // Dealing with dates is complicated because must parse
        Date tempDate;
        try {
            tempDate = dateFormat.parse(gc.getStartDate());
        } catch (ParseException e) {
            logger.error(
                    "Could not parse calendar start_date \"{}\" from " + "line #{} from file {}",
                    gc.getStartDate(),
                    gc.getLineNumber(),
                    gc.getFileName());
            tempDate = new Date();
        }
        keyBuilder.withStartDate(tempDate);

        // For end date parse the specified date and add a day so that
        // the end date will be midnight of the date specified.
        try {
            tempDate = dateFormat.parse(gc.getEndDate());
        } catch (ParseException e) {
            logger.error(
                    "Could not parse calendar end_date \"{}\" from " + "line #{} from file {}",
                    gc.getStartDate(),
                    gc.getLineNumber(),
                    gc.getFileName());
            tempDate = new Date();
        }
        keyBuilder.withEndDate(tempDate);
        this.key = keyBuilder.build();
    }

    /**
     * Returns true if the parameter zeroOrOne is set to "1". Otherwise returns false.
     *
     * @param zeroOrOne
     * @return
     */
    private boolean isSetToTrue(String zeroOrOne) {
        return zeroOrOne != null && zeroOrOne.trim().equals("1");
    }


    /**
     * @return the start date as a formatted string
     */
    public String getStartDateStr() {
        return formatter.format(key.startDate);
    }

    /**
     * End of the last day of service. This means that when an end date is specified the service
     * runs for up to and including that day. Since days start at midnight, this method returns the
     * endDate plus 1 day so that it represents midnight of the configured endDate.
     *
     * @return midnight at the end of the endDate
     */
    public Date getEndDate() {
        return new Date(key.endDate.getTime() + Time.MS_PER_DAY);
    }

    /**
     * @return the end date as a formatted string
     */
    public String getEndDateStr() {
        return formatter.format(key.endDate);
    }
}
