/* (C)2023 */
package org.transitclock.api.data;

import org.transitclock.service.dto.IpcHoldingTimeCacheKey;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ApiHoldingTimeCacheKey {

    @JsonProperty
    private String stopid;

    @JsonProperty
    private String vehicleId;

    @JsonProperty
    private String tripId;

    public ApiHoldingTimeCacheKey(IpcHoldingTimeCacheKey key) {
        this.stopid = key.getStopid();
        this.vehicleId = key.getVehicleId();
        this.tripId = key.getTripId();
    }

}
