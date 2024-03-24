/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.transitclock.service.dto.IpcPredictionForStopPath;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * An ordered list of routes.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiPredictionsForStopPathResponse {

    @JsonProperty
    private List<ApiPredictionForStopPath> data;

    /**
     * Constructs an ApiRouteSummaries using a collection of IpcRouteSummary objects.
     *
     * @param predictions
     */
    public ApiPredictionsForStopPathResponse(Collection<IpcPredictionForStopPath> predictions) {
        data = new ArrayList<>();
        for (IpcPredictionForStopPath prediction : predictions) {
            ApiPredictionForStopPath apiPredictionForStopPath = new ApiPredictionForStopPath(prediction);
            data.add(apiPredictionForStopPath);
        }
    }
}
