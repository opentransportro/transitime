/* (C)2023 */
package org.transitclock.api.data;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.transitclock.service.dto.IpcVehicleConfig;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * For when have collection of ApiVehicleConfig
 *
 * @author SkiBu Smith
 */
@Data
public class ApiVehicleConfigsResponse {

    // Need to use @XmlElementRef so that the element name used for each
    // ApiVehicle object will be what is specified in the ApiVehicle class.
    @JsonProperty("data")
    private List<ApiVehicleConfig> data;


    /**
     * Constructs a ApiVehicleConfigs object using IpcVehicleConfig data obtained via IPC.
     *
     * @param vehicles
     */
    public ApiVehicleConfigsResponse(Collection<IpcVehicleConfig> vehicles) {
        data = vehicles.stream()
                .sorted(Comparator.comparing(IpcVehicleConfig::getId))
                .map(ApiVehicleConfig::new)
                .toList();
    }
}
