/* (C)2023 */
package org.transitclock.core.dataCache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.EqualsAndHashCode;
import org.transitclock.service.dto.IpcArrivalDeparture;

@EqualsAndHashCode
public class StopEvents implements Serializable {
    public List<IpcArrivalDeparture> events;

    public StopEvents() {
    }

    public StopEvents(List<IpcArrivalDeparture> events) {
        this.events = events;
        this.events.sort(new IpcArrivalDepartureComparator());
    }

    public List<IpcArrivalDeparture> getEvents() {
        return events;
    }

    public void setEvents(List<IpcArrivalDeparture> events) {
        this.events = events;
        this.events.sort(new IpcArrivalDepartureComparator());
    }

    public void addEvent(IpcArrivalDeparture event) {
        if (this.events == null) {
            events = new ArrayList<>();
        }

        events.add(event);
        this.events.sort(new IpcArrivalDepartureComparator());
    }
}
