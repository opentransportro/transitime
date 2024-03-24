/* (C)2023 */
package org.transitclock.api.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.transitclock.domain.webstructs.WebAgency;
import org.transitclock.service.dto.IpcActiveBlock;
import org.transitclock.service.dto.IpcBlock;
import org.transitclock.service.dto.IpcTrip;
import org.transitclock.utils.Time;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author SkiBu Smith
 */
@Data
public class ApiActiveBlock implements Serializable {

    @JsonProperty
    private String id;

    @JsonProperty
    private String serviceId;

    @JsonProperty
    private String startTime;

    @JsonProperty
    private String endTime;

    @JsonProperty("trip")
    private ApiTripTerse trip;

    @JsonProperty("vehicles")
    private List<ApiVehicleDetails> vehicles;


    public ApiActiveBlock(IpcActiveBlock ipcActiveBlock, String agencyId) {
        IpcBlock block = ipcActiveBlock.getBlock();
        id = block.getId();
        serviceId = block.getServiceId();
        startTime = Time.timeOfDayStr(block.getStartTime());
        endTime = Time.timeOfDayStr(block.getEndTime());

        List<IpcTrip> trips = block.getTrips();
        IpcTrip ipcTrip = trips.get(ipcActiveBlock.getActiveTripIndex());
        trip = new ApiTripTerse(ipcTrip);

        // Get Time object based on timezone for agency
        WebAgency webAgency = WebAgency.getCachedWebAgency(agencyId);
        Time timeForAgency = webAgency.getAgency().getTime();

        vehicles = new ArrayList<>();
        vehicles = ipcActiveBlock.getVehicles()
                .stream()
                .map(v -> new ApiVehicleDetails(v, timeForAgency))
                .toList();
    }
}
