/* (C)2023 */
package org.transitclock.api.rootResources;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import jakarta.ws.rs.WebApplicationException;

public class DateParam {

    private LocalDate date;

    public DateParam(String in) throws WebApplicationException {
        try {
            date = LocalDate.parse(in, DateTimeFormatter.ISO_DATE);

        } catch (Exception exception) {
            throw new WebApplicationException(400);
        }
    }

    public LocalDate getDate() {
        return date;
    }

    public String format() {
        return date.toString();
    }
}
