/* (C)2023 */
package org.transitclock.api.data;

import org.transitclock.service.dto.IpcCalendar;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * A GTFS calendar
 *
 * @author SkiBu Smith
 */
@Data
public class ApiCalendar {

    @JsonProperty
    private String serviceId;

    @JsonProperty
    private boolean monday;

    @JsonProperty
    private boolean tuesday;

    @JsonProperty
    private boolean wednesday;

    @JsonProperty
    private boolean thursday;

    @JsonProperty
    private boolean friday;

    @JsonProperty
    private boolean saturday;

    @JsonProperty
    private boolean sunday;

    @JsonProperty
    private String startDate;

    @JsonProperty
    private String endDate;

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiCalendar() {}

    public ApiCalendar(IpcCalendar ipcCalendar) {
        this.serviceId = ipcCalendar.getServiceId();
        this.monday = ipcCalendar.isMonday();
        this.tuesday = ipcCalendar.isTuesday();
        this.wednesday = ipcCalendar.isWednesday();
        this.thursday = ipcCalendar.isThursday();
        this.friday = ipcCalendar.isFriday();
        this.saturday = ipcCalendar.isSaturday();
        this.sunday = ipcCalendar.isSunday();
        this.startDate = ipcCalendar.getStartDate();
        this.endDate = ipcCalendar.getEndDate();
    }
}
