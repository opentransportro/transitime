/* (C)2023 */
package org.transitclock.domain.structs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import java.util.Objects;

import lombok.*;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;

/**
 * For storing static configuration information for a vehicle.
 *
 * @author SkiBu Smith
 */
@Entity
@DynamicUpdate
@Getter @Setter @ToString
@Table(name = "vehicle_configs")
public class VehicleConfig {

    // ID of vehicle
    @Id
    @Column(name = "id", length = 60)
    private final String id;

    @Column(name = "name")
    private String name;

    // Same as vehicle type in GTFS
    @Column(name = "type")
    private final Integer type;

    // A more verbose description of the vehicle.
    @Column(name = "description")
    private final String description;

    // Useful for when getting a GPS feed that has a tracker ID, like an IMEI
    // or phone #, instead of a vehicle ID. Allows the corresponding vehicleId
    // to be determined from the VehicleConfig object.
    @Column(name = "tracker_id", length = 60)
    private final String trackerId;

    // Typical capacity of vehicle
    @Column(name = "capacity")
    private final Integer capacity;

    // Absolute crush capacity of vehicle. Number of people who can be
    // squeezed in.
    @Column(name = "crush_capacity")
    private final Integer crushCapacity;

    // If true then a non-revenue vehicle.
    @Column(name = "non_passenger_vehicle")
    private final Boolean nonPassengerVehicle;

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

    /** Needed because Hibernate requires no-arg constructor */
    @SuppressWarnings("unused")
    protected VehicleConfig() {
        id = null;
        type = null;
        description = null;
        trackerId = null;
        capacity = null;
        crushCapacity = null;
        nonPassengerVehicle = null;
        name = null;
    }

    /**
     * Reads List of VehicleConfig objects from database
     *
     * @param session
     * @return List of VehicleConfig objects
     * @throws HibernateException
     */
    public static List<VehicleConfig> getVehicleConfigs(Session session) throws HibernateException {
        return session
                .createQuery("FROM VehicleConfig", VehicleConfig.class)
                .list();
    }

    /**
     * Reads List of VehicleConfig objects from database
     *
     * @param vehicleConfig, session
     * @throws HibernateException
     */
    public static void updateVehicleConfig(VehicleConfig vehicleConfig, Session session) throws HibernateException {
        // Transaction tx = session.beginTransaction();
        session.merge(vehicleConfig);
        // tx.commit();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VehicleConfig that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(type, that.type) && Objects.equals(description, that.description) && Objects.equals(trackerId, that.trackerId) && Objects.equals(capacity, that.capacity) && Objects.equals(crushCapacity, that.crushCapacity) && Objects.equals(nonPassengerVehicle, that.nonPassengerVehicle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, description, trackerId, capacity, crushCapacity, nonPassengerVehicle);
    }
}
