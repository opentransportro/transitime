/* (C)2023 */
package org.transitclock.api.data;

import org.transitclock.service.dto.IpcVehicleConfig;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Contains config data for single vehicle.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiVehicleConfig {

    @JsonProperty
    private String id;

    @JsonProperty
    private Integer type;

    @JsonProperty
    private String description;

    @JsonProperty
    private Integer capacity;

    @JsonProperty
    private Integer crushCapacity;

    @JsonProperty
    private Boolean nonPassengerVehicle;

    @JsonProperty
    private String name;


    public ApiVehicleConfig(IpcVehicleConfig vehicle) {
        this.id = vehicle.getId();
        this.type = vehicle.getType();
        this.description = vehicle.getDescription();
        this.capacity = vehicle.getCapacity();
        this.crushCapacity = vehicle.getCrushCapacity();
        this.nonPassengerVehicle = vehicle.isNonPassengerVehicle();
        this.name = vehicle.getName();
    }
}
