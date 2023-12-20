/* (C)2023 */
package org.transitclock.core.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Data
@Document(collection = "StopPathPredictions")
@CompoundIndexes({
    @CompoundIndex(name = "StopPathPredictionTimeIndex", def = "{'tripId':1, 'stopPathIndex': 1}")
})
public class PredictionForStopPath implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private Date creationTime;

    private Double predictionTime;

    private String tripId;

    private Integer startTime;

    private String algorithm;

    private Integer stopPathIndex;

    private String vehicleId;

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
