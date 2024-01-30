/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import org.transitclock.service.dto.IpcCalendar;

/**
 * A GTFS calendar
 *
 * @author SkiBu Smith
 */
public class ApiCalendar {

    @XmlAttribute
    private String serviceId;

    @XmlAttribute
    private boolean monday;

    @XmlAttribute
    private boolean tuesday;

    @XmlAttribute
    private boolean wednesday;

    @XmlAttribute
    private boolean thursday;

    @XmlAttribute
    private boolean friday;

    @XmlAttribute
    private boolean saturday;

    @XmlAttribute
    private boolean sunday;

    @XmlAttribute
    private String startDate;

    @XmlAttribute
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
