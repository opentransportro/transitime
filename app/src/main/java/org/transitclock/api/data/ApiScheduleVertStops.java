/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import org.transitclock.service.dto.IpcSchedTime;
import org.transitclock.service.dto.IpcSchedTrip;
import org.transitclock.service.dto.IpcSchedule;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a schedule for a route for a specific direction and service class. Stops are listed
 * vertically in the matrix. For when there are a good number of stops but not as many trips, such
 * as for commuter rail.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiScheduleVertStops {

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

    @XmlElement(name = "trip")
    private List<ApiScheduleTrip> trips;

    @XmlElement
    private List<ApiScheduleTimesForStop> timesForStop;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiScheduleVertStops() {}

    public ApiScheduleVertStops(IpcSchedule ipcSched) {
        serviceId = ipcSched.getServiceId();
        serviceName = ipcSched.getServiceName();
        directionId = ipcSched.getDirectionId();
        routeId = ipcSched.getRouteId();
        routeName = ipcSched.getRouteName();

        // Create the trips element which contains list of all the trips
        // for the schedule for the route/direction/service
        trips = new ArrayList<ApiScheduleTrip>();
        for (IpcSchedTrip ipcSchedTrip : ipcSched.getIpcSchedTrips()) {
            trips.add(new ApiScheduleTrip(ipcSchedTrip));
        }

        // Determine all the times for each stop
        timesForStop = new ArrayList<ApiScheduleTimesForStop>();
        // Use first trip to determine which stops are covered
        List<IpcSchedTime> schedTimesForFirstTrip =
                ipcSched.getIpcSchedTrips().get(0).getSchedTimes();
        // For each stop. Uses schedule times for first trip to determine
        // the list of stops since each trip has same number of stops
        // in IpcSchedule (not every stop is actually visited though).
        for (int stopIndexInTrip = 0; stopIndexInTrip < schedTimesForFirstTrip.size(); ++stopIndexInTrip) {
            IpcSchedTime firstTripSchedTime = schedTimesForFirstTrip.get(stopIndexInTrip);
            String stopId = firstTripSchedTime.getStopId();
            String stopName = firstTripSchedTime.getStopName();
            ApiScheduleTimesForStop apiSchedTimesForStop = new ApiScheduleTimesForStop(stopId, stopName);
            timesForStop.add(apiSchedTimesForStop);
            // For each trip find the time for the current stop...
            for (IpcSchedTrip ipcSchedTrip : ipcSched.getIpcSchedTrips()) {
                // For the current trip find the time for the current stop...
                IpcSchedTime ipcSchedTime = ipcSchedTrip.getSchedTimes().get(stopIndexInTrip);
                apiSchedTimesForStop.add(ipcSchedTime.getTimeOfDay());
            }
        }
    }
}
