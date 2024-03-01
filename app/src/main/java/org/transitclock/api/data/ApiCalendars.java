/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.transitclock.service.dto.IpcCalendar;

import java.util.ArrayList;
import java.util.List;

/**
 * List of GTFS calendars
 *
 * @author SkiBu Smith
 */
@Data
@XmlRootElement(name = "calendars")
public class ApiCalendars {

    @XmlElement
    private List<ApiCalendar> calendars;


    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiCalendars() {}

    public ApiCalendars(List<IpcCalendar> ipcCalendars) {
        calendars = new ArrayList<ApiCalendar>();
        for (IpcCalendar ipcCalendar : ipcCalendars) calendars.add(new ApiCalendar(ipcCalendar));
    }
}
