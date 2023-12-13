/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.transitclock.ipc.data.IpcSchedule;

@XmlRootElement(name = "schedules")
public class ApiSchedulesHorizStops {

    @XmlAttribute
    private String routeId;

    @XmlAttribute
    private String routeName;

    @XmlElement(name = "schedule")
    private List<ApiScheduleHorizStops> schedules;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiSchedulesHorizStops() {}

    public ApiSchedulesHorizStops(List<IpcSchedule> schedules) {
        this.routeId = schedules.get(0).getRouteId();
        this.routeName = schedules.get(0).getRouteName();

        this.schedules = new ArrayList<ApiScheduleHorizStops>(schedules.size());
        for (IpcSchedule ipcSchedule : schedules) {
            this.schedules.add(new ApiScheduleHorizStops(ipcSchedule));
        }
    }
}
