/* (C)2023 */
package org.transitclock.core.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Getter @Setter
@EqualsAndHashCode
@ToString
@Document(collection = "Headway")
public class Headway implements Serializable {
    public Headway() {}

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private int configRev;

    private double headway;

    private double average;

    private double variance;

    private double coefficientOfVariation;

    private int numVehicles;

    @Indexed(name = "HeadwayIndex")
    private Date creationTime;

    private String vehicleId;

    private String otherVehicleId;

    private String stopId;

    private String tripId;

    private String routeId;

    private Date firstDeparture;

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
}
