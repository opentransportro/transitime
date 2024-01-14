/* (C)2023 */
package org.transitclock.db.structs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
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
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Table(name = "VehicleConfigs")
public class VehicleConfig {

    // ID of vehicle
    @Column(length = 60)
    @Id
    private final String id;

    @Column
    private String name;

    // Same as vehicle type in GTFS
    @Column
    private final Integer type;

    // A more verbose description of the vehicle.
    @Column
    private final String description;

    // Useful for when getting a GPS feed that has a tracker ID, like an IMEI
    // or phone #, instead of a vehicle ID. Allows the corresponding vehicleId
    // to be determined from the VehicleConfig object.
    @Column(length = 60)
    private final String trackerId;

    // Typical capacity of vehicle
    @Column
    private final Integer capacity;

    // Absolute crush capacity of vehicle. Number of people who can be
    // squeezed in.
    @Column
    private final Integer crushCapacity;

    // If true then a non-revenue vehicle.
    @Column
    private final Boolean nonPassengerVehicle;

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
    @SuppressWarnings("unchecked")
    public static List<VehicleConfig> getVehicleConfigs(Session session) throws HibernateException {
        return session
                .createQuery("FROM VehicleConfig", VehicleConfig.class)
                .list();
    }

    /**
     * Reads List of VehicleConfig objects from database
     *
     * @param VehicleConfig, session
     * @throws HibernateException
     */
    public static void updateVehicleConfig(VehicleConfig vehicleConfig, Session session) throws HibernateException {
        // Transaction tx = session.beginTransaction();
        session.update(vehicleConfig);
        // tx.commit();
    }
}
