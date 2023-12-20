/* (C)2023 */
package org.transitclock.ipc.data;

import java.io.Serializable;

import lombok.Getter;
import lombok.ToString;
import org.transitclock.core.domain.Calendar;

/**
 * A calendar object for IPC via RMI
 *
 * @author SkiBu Smith
 */
@Getter
@ToString
public class IpcCalendar implements Serializable {

    private final String serviceId;
    private final boolean monday;
    private final boolean tuesday;
    private final boolean wednesday;
    private final boolean thursday;
    private final boolean friday;
    private final boolean saturday;
    private final boolean sunday;
    private final String startDate;
    private final String endDate;

    public IpcCalendar(Calendar calendar) {
        this.serviceId = calendar.getServiceId();
        this.monday = calendar.getMonday();
        this.tuesday = calendar.getTuesday();
        this.wednesday = calendar.getWednesday();
        this.thursday = calendar.getThursday();
        this.friday = calendar.getFriday();
        this.saturday = calendar.getSaturday();
        this.sunday = calendar.getSunday();
        this.startDate = calendar.getStartDateStr();
        this.endDate = calendar.getEndDateStr();
    }
}
