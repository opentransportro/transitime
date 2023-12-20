/* (C)2023 */
package org.transitclock.core.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * For storing static configuration information for a vehicle.
 *
 * @author SkiBu Smith
 */
@Data
@Document(collection = "VehicleConfigs")
public class VehicleConfig {

    // ID of vehicle
    @Id
    private final String id;

    private String name;

    // Same as vehicle type in GTFS
    private final Integer type;

    // A more verbose description of the vehicle.
    private final String description;

    // Useful for when getting a GPS feed that has a tracker ID, like an IMEI
    // or phone #, instead of a vehicle ID. Allows the corresponding vehicleId
    // to be determined from the VehicleConfig object.
    private final String trackerId;

    // Typical capacity of vehicle
    private final Integer capacity;

    // Absolute crush capacity of vehicle. Number of people who can be  squeezed in.
    private final Integer crushCapacity;

    // If true then a non-revenue vehicle.
    private final Boolean nonPassengerVehicle;

    /**
     * Constructor for when new vehicle encountered and automatically adding it to the db.
     *
     * @param id vehicle ID
     */
    public VehicleConfig(String id) {
        this.id = id;
        type = null;
        description = null;
        trackerId = null;
        capacity = null;
        crushCapacity = null;
        nonPassengerVehicle = null;
        name = null;
    }

    /**
     * Constructor for when new vehicle encountered and automatically adding it to the db.
     *
     * @param id vehicle ID
     * @param name vehicle name
     */
    public VehicleConfig(String id, String name) {
        this.id = id;
        type = null;
        description = null;
        trackerId = null;
        capacity = null;
        crushCapacity = null;
        nonPassengerVehicle = null;
        this.name = name;
    }
}
