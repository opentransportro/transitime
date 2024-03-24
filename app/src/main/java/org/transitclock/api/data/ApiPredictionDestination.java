/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;

import org.transitclock.service.dto.IpcPrediction;
import org.transitclock.service.dto.IpcPredictionsForRouteStopDest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Contains list of predictions for a particular headsign.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiPredictionDestination {

    @JsonProperty
    private String directionId;

    @JsonProperty
    private String headsign;

    @JsonProperty
    private List<ApiPrediction> predictions;


    public ApiPredictionDestination(IpcPredictionsForRouteStopDest predictionsForRouteStop) {
        directionId = predictionsForRouteStop.getDirectionId();
        headsign = predictionsForRouteStop.getHeadsign();

        predictions = new ArrayList<>();
        for (IpcPrediction prediction : predictionsForRouteStop.getPredictionsForRouteStop()) {
            predictions.add(new ApiPrediction(prediction));
        }
    }
}
