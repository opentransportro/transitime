/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.transitclock.ipc.data.IpcSchedule;

/**
 * Represents a collection of ApiScheduleVertStops objects for a route. There is one
 * ApiScheduleVertStops for each direction/service for a route. The stops are listed vertically in
 * the matrix. For when there are a good number of stops but not as many trips, such as for commuter
 * rail.
 *
 * @author SkiBu Smith
 */
@XmlRootElement(name = "schedules")
public class ApiSchedulesVertStops {

    @XmlAttribute
    private String routeId;

    @XmlAttribute
    private String routeName;

    @XmlElement(name = "schedule")
    private List<ApiScheduleVertStops> schedules;

    /********************** Member Functions **************************/

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
