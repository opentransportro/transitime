/* (C)2023 */
package org.transitclock.domain.structs;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * For storing a measured arrival time so that can see if measured arrival time via GPS is accurate.
 *
 * @author Michael Smith
 */
@Entity
@DynamicUpdate
@Getter
@Setter
@ToString
@Table(
    name = "measured_arrival_times",
    indexes = {
        @Index(name = "MeasuredArrivalTimesIndex", columnList = "time")
    }
)
public class MeasuredArrivalTime implements Serializable {
    @Id
    @Column(name = "time")
    @Temporal(TemporalType.TIMESTAMP)
    private final Date time;

    @Id
    @Column(name = "stop_id", length = 60)
    private String stopId;

    @Column(name = "route_id", length = 60)
    private String routeId;

    @Column(name = "route_short_name", length = 60)
    private String routeShortName;

    @Column(name = "direction_id", length = 60)
    private String directionId;

    @Column(name = "headsign", length = 60)
    private String headsign;

    public MeasuredArrivalTime(
            Date time, String stopId, String routeId, String routeShortName, String directionId, String headsign) {
        this.time = time;
        this.stopId = stopId;
        this.routeId = routeId;
        this.routeShortName = routeShortName;
        this.directionId = directionId;
        this.headsign = headsign;
    }

    /** Hibernate requires a no-arg constructor for reading objects from database. */
    protected MeasuredArrivalTime() {
        this.time = null;
        this.stopId = null;
        this.routeId = null;
        this.routeShortName = null;
        this.directionId = null;
        this.headsign = null;
    }

    /**
     * Returns the SQL to save the object into database. Usually Hibernate is used because such data
     * is stored by the core system. But MeasuredArrivalTime objects are written by the website,
     * which doesn't use Hibernate to write objects since it has to be able to talk with any db.
     *
     * @return SQL to store the object
     */
    public String getUpdateSql() {
        return "INSERT INTO MeasuredArrivalTimes ("
                + "time, stop_id, route_id, route_short_name, direction_id, headsign) "
                + "VALUES('"
                + time
                + "', '"
                + stopId
                + "', '"
                + routeId
                + "', '"
                + routeShortName
                + "', '"
                + directionId
                + "', '"
                + headsign
                + "'"
                + ");";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MeasuredArrivalTime that)) return false;
        return Objects.equals(time, that.time) && Objects.equals(stopId, that.stopId) && Objects.equals(routeId, that.routeId) && Objects.equals(routeShortName, that.routeShortName) && Objects.equals(directionId, that.directionId) && Objects.equals(headsign, that.headsign);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, stopId, routeId, routeShortName, directionId, headsign);
    }
}
