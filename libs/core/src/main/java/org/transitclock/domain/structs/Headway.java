/* (C)2023 */
package org.transitclock.domain.structs;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@Setter
@ToString
@DynamicUpdate
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
            int configRev,
            long headway,
            Date creationTime,
            String vehicleId,
            String otherVehicleId,
            String stopId,
            String tripId,
            String routeId,
            Date firstDeparture,
            Date secondDeparture) {

        this.configRev = configRev;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Headway headway1)) return false;
        return id == headway1.id && configRev == headway1.configRev && Double.compare(headway, headway1.headway) == 0 && Double.compare(average, headway1.average) == 0 && Double.compare(variance, headway1.variance) == 0 && Double.compare(coefficientOfVariation, headway1.coefficientOfVariation) == 0 && numVehicles == headway1.numVehicles && Objects.equals(creationTime, headway1.creationTime) && Objects.equals(vehicleId, headway1.vehicleId) && Objects.equals(otherVehicleId, headway1.otherVehicleId) && Objects.equals(stopId, headway1.stopId) && Objects.equals(tripId, headway1.tripId) && Objects.equals(routeId, headway1.routeId) && Objects.equals(firstDeparture, headway1.firstDeparture) && Objects.equals(secondDeparture, headway1.secondDeparture);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, configRev, headway, average, variance, coefficientOfVariation, numVehicles, creationTime, vehicleId, otherVehicleId, stopId, tripId, routeId, firstDeparture, secondDeparture);
    }
}
