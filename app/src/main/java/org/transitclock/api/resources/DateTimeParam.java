/* (C)2023 */
package org.transitclock.api.resources;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import jakarta.ws.rs.WebApplicationException;

public class DateTimeParam {

    private LocalDateTime date;

    public DateTimeParam(String in) throws WebApplicationException {
        try {
            date = LocalDateTime.parse(in, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        } catch (Exception exception) {
            throw new WebApplicationException(400);
        }
    }

    public LocalDateTime getDate() {
        return date;
    }

    public long getTimeStamp() {
        return date.toEpochSecond(ZoneOffset.UTC) * 1000L;
    }

    public String format() {
        return date.toString();
    }
}
