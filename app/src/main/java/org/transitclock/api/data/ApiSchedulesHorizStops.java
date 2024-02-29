/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.transitclock.service.dto.IpcSchedule;

import java.util.ArrayList;
import java.util.List;
@Data
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
