/* (C)2023 */
package org.transitclock.api.data;

import java.util.List;

import org.transitclock.service.dto.IpcCalendar;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * List of GTFS calendars
 *
 * @author SkiBu Smith
 */
@Data
public class ApiCalendarsResponse {

    @JsonProperty
    private List<ApiCalendar> data;

    public ApiCalendarsResponse(List<IpcCalendar> ipcCalendars) {
        data = ipcCalendars.stream()
                .map(ApiCalendar::new)
                .toList();
    }
}
