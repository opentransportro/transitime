/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import org.transitclock.service.dto.IpcSchedTime;
import org.transitclock.service.dto.IpcSchedTrip;
import org.transitclock.service.dto.IpcSchedule;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a schedule for a route for a specific direction and service class. Stops are listed
 * horizontally in the matrix.
 *
 * @author SkiBu Smith
 */
public class ApiScheduleHorizStops {

    @XmlAttribute
    private String serviceId;

    @XmlAttribute
    private String serviceName;

    @XmlAttribute
    private String directionId;

    @XmlAttribute
    private String routeId;

    @XmlAttribute
    private String routeName;

    @XmlElement(name = "stop")
    private List<ApiScheduleStop> stops;

    @XmlElement
    private List<ApiScheduleTimesForTrip> timesForTrip;


    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiScheduleHorizStops() {}

    public ApiScheduleHorizStops(IpcSchedule ipcSched) {
        serviceId = ipcSched.getServiceId();
        serviceName = ipcSched.getServiceName();
        directionId = ipcSched.getDirectionId();
        routeId = ipcSched.getRouteId();
        routeName = ipcSched.getRouteName();

        // Create the list of stops to be first row of output
        stops = new ArrayList<ApiScheduleStop>();
        IpcSchedTrip firstIpcSchedTrip = ipcSched.getIpcSchedTrips().get(0);
        for (IpcSchedTime ipcSchedTime : firstIpcSchedTrip.getSchedTimes()) {
            stops.add(new ApiScheduleStop(ipcSchedTime.getStopId(), ipcSchedTime.getStopName()));
        }

        // Create the schedule row for each trip
        timesForTrip = new ArrayList<ApiScheduleTimesForTrip>();
        for (IpcSchedTrip ipcSchedTrip : ipcSched.getIpcSchedTrips()) {
            timesForTrip.add(new ApiScheduleTimesForTrip(ipcSchedTrip));
        }
    }
}
