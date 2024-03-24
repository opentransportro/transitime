/* (C)2023 */
package org.transitclock.api.data;

import java.util.Date;

import org.transitclock.service.dto.IpcArrivalDeparture;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ApiArrivalDeparture {
    @JsonProperty
    private String stopId;

    @JsonProperty
    private String vehicleId;

    @JsonProperty
    private Date time;

    @JsonProperty
    private Date scheduledTime;

    @JsonProperty
    private boolean isArrival;

    @JsonProperty
    private String tripId;

    @JsonProperty
    private String routeId;

    @JsonProperty
    private Integer stopPathIndex;

    public ApiArrivalDeparture(IpcArrivalDeparture ipcArrivalDeparture) {
        this.vehicleId = ipcArrivalDeparture.getVehicleId();
        this.time = ipcArrivalDeparture.getTime();
        this.routeId = ipcArrivalDeparture.getRouteId();
        this.tripId = ipcArrivalDeparture.getTripId();
        this.isArrival = ipcArrivalDeparture.isArrival();
        this.stopId = ipcArrivalDeparture.getStopId();
        this.stopPathIndex = ipcArrivalDeparture.getStopPathIndex();

        // TODO
        // this.scheduledTime=ipcArrivalDeparture.getScheduledTime();
    }
}
