/* (C)2023 */
package org.transitclock.api.data;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.transitclock.api.resources.TransitimeApi.UiMode;
import org.transitclock.domain.structs.Agency;
import org.transitclock.domain.webstructs.WebAgency;
import org.transitclock.service.dto.IpcVehicle;
import org.transitclock.utils.Time;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * For when have list of VehicleDetails. By using this class can control the element name when data
 * is output.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiVehiclesDetailsResponse {

    @JsonProperty(value = "data")
    private List<ApiVehicleDetails> data;

    /**
     * For constructing a ApiVehiclesDetails object from a Collection of Vehicle objects.
     *
     * @param vehicles
     * @param agencyId
     * @param uiTypesForVehicles Specifies how vehicles should be drawn in UI. Can be NORMAL,
     *     SECONDARY, or MINOR
     * @param assigned
     */
    public ApiVehiclesDetailsResponse(Collection<IpcVehicle> vehicles, String agencyId, Map<String, UiMode> uiTypesForVehicles, boolean assigned) {
        // Get Time object based on timezone for agency
        WebAgency webAgency = WebAgency.getCachedWebAgency(agencyId);
        Agency agency = webAgency.getAgency();
        Time timeForAgency = agency != null ? agency.getTime() : new Time((String) null);

        // Process each vehicle
        data = vehicles.stream()
                .filter(vehicle -> !assigned || vehicle.getTripId() != null)
                .map(vehicle -> new ApiVehicleDetails(vehicle, timeForAgency, uiTypesForVehicles.get(vehicle.getId())))
                .toList();
    }
}
