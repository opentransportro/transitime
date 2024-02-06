/* (C)2023 */
package org.transitclock.domain.structs;

import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.transitclock.domain.hibernate.HibernateUtils;
import org.transitclock.domain.structs.QPredictionForStopPath;

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
        name = "stop_path_predictions",
        indexes = {
                @Index(name = "StopPathPredictionTimeIndex", columnList = "trip_id, stop_path_index")
        })
public class PredictionForStopPath implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "creation_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @Column(name = "prediction_time")
    private Double predictionTime;

    @Column(name = "trip_id", length = 60)
    private String tripId;

    @Column(name = "start_time")
    private Integer startTime;

    @Column(name = "algorithm")
    private String algorithm;

    @Column(name = "stop_path_index")
    private Integer stopPathIndex;

    @Column(name = "vehicle_id")
    private String vehicleId;

    @Column(name = "travel_time")
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
