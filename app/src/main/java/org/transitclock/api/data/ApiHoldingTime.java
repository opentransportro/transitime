/* (C)2023 */
package org.transitclock.api.data;

import java.util.Date;

import org.transitclock.service.dto.IpcHoldingTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ApiHoldingTime {

    @JsonProperty
    private Date holdingTime;

    @JsonProperty
    private Date creationTime;

    @JsonProperty
    private Date currentTime;

    @JsonProperty
    private String vehicleId;

    @JsonProperty
    private String stopId;

    @JsonProperty
    private String tripId;

    @JsonProperty
    private String routeId;

    @JsonProperty
    private boolean arrivalPredictionUsed;

    @JsonProperty
    private boolean arrivalUsed;

    @JsonProperty
    private Date arrivalTime;

    @JsonProperty
    private boolean hasN1;

    @JsonProperty
    private boolean hasN2;

    @JsonProperty
    private boolean hasD1;

    @JsonProperty
    private int numberPredictionsUsed;

    public ApiHoldingTime(IpcHoldingTime ipcHoldingTime) {
        this.holdingTime = ipcHoldingTime.getHoldingTime();
        this.creationTime = ipcHoldingTime.getCreationTime();
        this.vehicleId = ipcHoldingTime.getVehicleId();
        this.tripId = ipcHoldingTime.getTripId();
        this.routeId = ipcHoldingTime.getRouteId();
        this.stopId = ipcHoldingTime.getStopId();
        this.arrivalPredictionUsed = ipcHoldingTime.isArrivalPredictionUsed();
        this.arrivalUsed = ipcHoldingTime.isArrivalUsed();
        this.currentTime = ipcHoldingTime.getCurrentTime();
        this.arrivalTime = ipcHoldingTime.getArrivalTime();
        this.hasD1 = ipcHoldingTime.isHasD1();
        this.numberPredictionsUsed = ipcHoldingTime.getNumberPredictionsUsed();
    }
}
