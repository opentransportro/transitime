/* (C)2023 */
package org.transitclock.domain.structs;

import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.transitclock.Core;

@Entity
@DynamicUpdate
@Data
@NoArgsConstructor
@Table(
    name = "headways",
    indexes = {
        @Index(name = "HeadwayIndex", columnList = "creation_time")
    })
public class Headway implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    // The revision of the configuration data that was being used
    @Column(name = "config_rev")
    private int configRev;

    @Column(name = "headway")
    private double headway;

    @Column(name = "average")
    private double average;

    @Column(name = "variance")
    private double variance;

    @Column(name = "coefficient_of_variation")
    private double coefficientOfVariation;

    @Column(name = "num_vehicles")
    private int numVehicles;

    // The time the AVL data was processed and the headway was created.
    @Column(name = "creation_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @Column(name = "vehicle_id", length = 60)
    private String vehicleId;

    @Column(name = "other_vehicle_id", length = 60)
    private String otherVehicleId;

    @Column(name = "stop_id", length = 60)
    private String stopId;

    @Column(name = "trip_id", length = 60)
    private String tripId;

    @Column(name = "route_id", length = 60)
    private String routeId;

    @Column(name = "first_departure")
    @Temporal(TemporalType.TIMESTAMP)
    private Date firstDeparture;

    @Column(name = "second_departure")
    @Temporal(TemporalType.TIMESTAMP)
    private Date secondDeparture;

    public Headway(
            long headway,
            Date creationTime,
            String vehicleId,
            String otherVehicleId,
            String stopId,
            String tripId,
            String routeId,
            Date firstDeparture,
            Date secondDeparture) {

        this.configRev = Core.getInstance().getDbConfig().getConfigRev();
        this.headway = headway;
        this.creationTime = creationTime;
        this.vehicleId = vehicleId;
        this.stopId = stopId;
        this.tripId = tripId;
        this.routeId = routeId;
        this.average = 0;
        this.variance = 0;
        this.coefficientOfVariation = 0;
        this.numVehicles = 0;
        this.otherVehicleId = otherVehicleId;
        this.firstDeparture = firstDeparture;
        this.secondDeparture = secondDeparture;
    }
}
