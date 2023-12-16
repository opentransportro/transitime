/* (C)2023 */
package org.transitclock.db.structs;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.criterion.Restrictions;
import org.transitclock.db.hibernate.HibernateUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Sean Og Crudden Store the travel time prediction for a stopPath.
 */
@Entity
@DynamicUpdate
@Getter @Setter
@EqualsAndHashCode
@ToString
@Table(
        name = "StopPathPredictions",
        indexes = {@Index(name = "StopPathPredictionTimeIndex", columnList = "tripId, stopPathIndex")})
public class PredictionForStopPath implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @Column
    private Double predictionTime;

    @Column(length = 60)
    private String tripId;

    @Column
    private Integer startTime;

    @Column
    private String algorithm;

    @Column
    private Integer stopPathIndex;

    @Column
    private String vehicleId;

    @Column
    private boolean travelTime;

    public PredictionForStopPath(
            String vehicleId,
            Date creationTime,
            Double predictionTimeMilliseconds,
            String tripId,
            Integer stopPathIndex,
            String algorithm,
            boolean travelTime,
            Integer startTime) {
        this.creationTime = creationTime;
        this.predictionTime = predictionTimeMilliseconds;
        this.tripId = tripId;
        this.stopPathIndex = stopPathIndex;
        this.algorithm = algorithm;
        this.vehicleId = vehicleId;
        this.travelTime = travelTime;
        this.startTime = startTime;
    }

    @SuppressWarnings("unchecked")
    public static List<PredictionForStopPath> getPredictionForStopPathFromDB(
            Date beginTime, Date endTime, String algorithm, String tripId, Integer stopPathIndex) {
        Session session = HibernateUtils.getSession();
        Criteria criteria = session.createCriteria(PredictionForStopPath.class);

        if (algorithm != null && !algorithm.isEmpty()) criteria.add(Restrictions.eq("algorithm", algorithm));
        if (tripId != null) criteria.add(Restrictions.eq("tripId", tripId));
        if (stopPathIndex != null) criteria.add(Restrictions.eq("stopPathIndex", stopPathIndex));
        if (beginTime != null) criteria.add(Restrictions.gt("creationTime", beginTime));
        if (endTime != null) criteria.add(Restrictions.lt("creationTime", endTime));

        return (List<PredictionForStopPath>) criteria.list();
    }

    public PredictionForStopPath() {
        this.creationTime = null;
        this.predictionTime = null;
        this.tripId = null;
        this.stopPathIndex = null;
        this.algorithm = null;
        this.travelTime = true;
        this.startTime = null;
    }
}
