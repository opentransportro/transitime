/* (C)2023 */
package org.transitclock.db.structs;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;
import org.transitclock.applications.Core;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@DynamicUpdate
@Getter @Setter
@EqualsAndHashCode
@ToString
@Table(
        name = "Headway",
        indexes = {@Index(name = "HeadwayIndex", columnList = "creationTime")})
public class Headway implements Serializable {
    public Headway() {}

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    // The revision of the configuration data that was being used
    @Column
    private int configRev;

    @Column
    private double headway;

    @Column
    private double average;

    @Column
    private double variance;

    @Column
    private double coefficientOfVariation;

    @Column
    private int numVehicles;

    // The time the AVL data was processed and the headway was created.
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @Column(length = 60)
    private String vehicleId;

    @Column(length = 60)
    private String otherVehicleId;

    @Column(length = 60)
    private String stopId;

    @Column(length = 60)
    private String tripId;

    @Column(length = 60)
    private String routeId;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date firstDeparture;

    @Column
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
