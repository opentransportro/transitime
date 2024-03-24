/* (C)2023 */
package org.transitclock.api.data;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.transitclock.api.resources.TransitimeApi.UiMode;
import org.transitclock.service.dto.IpcVehicle;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * For when have list of Vehicles. By using this class can control the element name when data is
 * output.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiVehiclesResponse {

    @JsonProperty
    private List<ApiVehicle> data;


    /**
     * For constructing a ApiVehicles object from a Collection of Vehicle objects.
     *
     * @param vehicles
     * @param uiTypesForVehicles Specifies how vehicles should be drawn in UI. Can be NORMAL,
     *     SECONDARY, or MINOR
     */
    public ApiVehiclesResponse(Collection<IpcVehicle> vehicles, Map<String, UiMode> uiTypesForVehicles) {
        data = vehicles.stream()
                .map(vehicle -> new ApiVehicle(vehicle, uiTypesForVehicles.get(vehicle.getId())))
                .toList();
    }

    /**
     * For constructing a ApiVehicles object from a Collection of Vehicle objects. Sets UiMode to
     * UiMode.NORMAL.
     *
     * @param vehicles
     */
    public ApiVehiclesResponse(Collection<IpcVehicle> vehicles) {
        data = vehicles.stream()
                .map(vehicle -> new ApiVehicle(vehicle, UiMode.NORMAL))
                .toList();
    }
}
