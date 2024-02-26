/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.transitclock.service.dto.IpcVehicleConfig;

/**
 * Contains config data for single vehicle.
 *
 * @author SkiBu Smith
 */
@XmlRootElement(name = "vehicleConfig")
@XmlType(propOrder = {"id", "type", "description", "capacity", "crushCapacity", "nonPassengerVehicle", "name"})
public class ApiVehicleConfig {

    @XmlAttribute
    private String id;

    @XmlAttribute
    private Integer type;

    @XmlAttribute
    private String description;

    @XmlAttribute
    private Integer capacity;

    @XmlAttribute
    private Integer crushCapacity;

    @XmlAttribute
    private Boolean nonPassengerVehicle;

    @XmlAttribute
    private String name;


    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiVehicleConfig() {}

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
