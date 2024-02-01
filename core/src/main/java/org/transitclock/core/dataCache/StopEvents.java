/* (C)2023 */
package org.transitclock.core.dataCache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.transitclock.service.dto.IpcArrivalDeparture;

public class StopEvents implements Serializable {
    public List<IpcArrivalDeparture> events;

    public List<IpcArrivalDeparture> getEvents() {
        return events;
    }

    public void setEvents(List<IpcArrivalDeparture> events) {
        this.events = events;
        this.events.sort(new IpcArrivalDepartureComparator());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((events == null) ? 0 : events.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        StopEvents other = (StopEvents) obj;
        if (events == null) {
            return other.events == null;
        } else return events.equals(other.events);
    }

    public StopEvents() {
        super();
    }

    public StopEvents(List<IpcArrivalDeparture> events) {
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
