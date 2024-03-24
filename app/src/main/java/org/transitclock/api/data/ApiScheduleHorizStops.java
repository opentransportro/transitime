/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;

import org.transitclock.service.dto.IpcSchedTime;
import org.transitclock.service.dto.IpcSchedTrip;
import org.transitclock.service.dto.IpcSchedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents a schedule for a route for a specific direction and service class. Stops are listed
 * horizontally in the matrix.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiScheduleHorizStops {

    @JsonProperty
    private String serviceId;

    @JsonProperty
    private String serviceName;

    @JsonProperty
    private String directionId;

    @JsonProperty
    private String routeId;

    @JsonProperty
    private String routeName;

    @JsonProperty
    private List<ApiScheduleStop> stops;

    @JsonProperty
    private List<ApiScheduleTimesForTrip> timesForTrip;

    public ApiScheduleHorizStops(IpcSchedule ipcSched) {
        serviceId = ipcSched.getServiceId();
        serviceName = ipcSched.getServiceName();
        directionId = ipcSched.getDirectionId();
        routeId = ipcSched.getRouteId();
        routeName = ipcSched.getRouteName();

        // Create the list of stops to be first row of output
        stops = new ArrayList<>();
        IpcSchedTrip firstIpcSchedTrip = ipcSched.getIpcSchedTrips().get(0);
        for (IpcSchedTime ipcSchedTime : firstIpcSchedTrip.getSchedTimes()) {
            stops.add(new ApiScheduleStop(ipcSchedTime.getStopId(), ipcSchedTime.getStopName()));
        }

        // Create the schedule row for each trip
        timesForTrip = new ArrayList<>();
        for (IpcSchedTrip ipcSchedTrip : ipcSched.getIpcSchedTrips()) {
            timesForTrip.add(new ApiScheduleTimesForTrip(ipcSchedTrip));
        }
    }
}
