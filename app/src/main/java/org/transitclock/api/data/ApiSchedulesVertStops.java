/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.transitclock.service.dto.IpcSchedule;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a collection of ApiScheduleVertStops objects for a route. There is one
 * ApiScheduleVertStops for each direction/service for a route. The stops are listed vertically in
 * the matrix. For when there are a good number of stops but not as many trips, such as for commuter
 * rail.
 *
 * @author SkiBu Smith
 */@Data
@XmlRootElement(name = "schedules")
public class ApiSchedulesVertStops {

    @XmlAttribute
    private String routeId;

    @XmlAttribute
    private String routeName;

    @XmlElement(name = "schedule")
    private List<ApiScheduleVertStops> schedules;


    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiSchedulesVertStops() {}

    public ApiSchedulesVertStops(List<IpcSchedule> schedules) {
        this.routeId = schedules.get(0).getRouteId();
        this.routeName = schedules.get(0).getRouteName();

        this.schedules = new ArrayList<ApiScheduleVertStops>(schedules.size());
        for (IpcSchedule ipcSchedule : schedules) {
            this.schedules.add(new ApiScheduleVertStops(ipcSchedule));
        }
    }
}
