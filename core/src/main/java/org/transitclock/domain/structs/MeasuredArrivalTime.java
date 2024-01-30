/* (C)2023 */
package org.transitclock.domain.structs;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;

/**
 * For storing a measured arrival time so that can see if measured arrival time via GPS is accurate.
 *
 * @author Michael Smith
 */
@Entity
@DynamicUpdate
@Data
@Table(
        name = "MeasuredArrivalTimes",
        indexes = {@Index(name = "MeasuredArrivalTimesIndex", columnList = "time")})
public class MeasuredArrivalTime implements Serializable {
    @Id
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private final Date time;

    @Id
    @Column(length = 60)
    private String stopId;

    @Column(length = 60)
    private String routeId;

    @Column(length = 60)
    private String routeShortName;

    @Column(length = 60)
    private String directionId;

    @Column(length = 60)
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
                + "time, stopId, routeId, routeShortName, directionId, headsign) "
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
}
