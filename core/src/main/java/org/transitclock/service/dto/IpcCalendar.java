/* (C)2023 */
package org.transitclock.service.dto;

import java.io.Serializable;
import org.transitclock.db.structs.Calendar;

/**
 * A calendar object for IPC via RMI
 *
 * @author SkiBu Smith
 */
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
        super();
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

    @Override
    public String toString() {
        return "IpcCalendar ["
                + "serviceId="
                + serviceId
                + ", monday="
                + monday
                + ", tuesday="
                + tuesday
                + ", wednesday="
                + wednesday
                + ", thursday="
                + thursday
                + ", friday="
                + friday
                + ", saturday="
                + saturday
                + ", sunday="
                + sunday
                + ", startDate="
                + startDate
                + ", endDate="
                + endDate
                + "]";
    }

    public String getServiceId() {
        return serviceId;
    }

    public boolean isMonday() {
        return monday;
    }

    public boolean isTuesday() {
        return tuesday;
    }

    public boolean isWednesday() {
        return wednesday;
    }

    public boolean isThursday() {
        return thursday;
    }

    public boolean isFriday() {
        return friday;
    }

    public boolean isSaturday() {
        return saturday;
    }

    public boolean isSunday() {
        return sunday;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }
}
