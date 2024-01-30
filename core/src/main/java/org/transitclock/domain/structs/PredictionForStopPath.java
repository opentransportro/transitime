/* (C)2023 */
package org.transitclock.domain.structs;

import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.transitclock.domain.hibernate.HibernateUtils;
import org.transitclock.db.structs.QPredictionForStopPath;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Sean Og Crudden Store the travel time prediction for a stopPath.
 */
@Entity
@DynamicUpdate
@Data
@Table(
        name = "StopPathPredictions",
        indexes = {
                @Index(name = "StopPathPredictionTimeIndex", columnList = "tripId, stopPathIndex")
        })
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
        EntityManager em = HibernateUtils.getSession();
        JPAQuery<PredictionForStopPath> query = new JPAQuery<>(em);
        var qentity = QPredictionForStopPath.predictionForStopPath;

        query.from(qentity);
        if (algorithm != null && !algorithm.isEmpty()) {
            query.where(qentity.algorithm.eq(algorithm));
        }
        if (tripId != null) {
            query.where(qentity.tripId.eq(tripId));
        }
        if (stopPathIndex != null) {
            query.where(qentity.stopPathIndex.eq(stopPathIndex));
        }
        if (beginTime != null) {
            query.where(qentity.creationTime.gt(beginTime));
        }
        if (endTime != null) {
            query.where(qentity.creationTime.lt(endTime));
        }

        return query.fetch();
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
