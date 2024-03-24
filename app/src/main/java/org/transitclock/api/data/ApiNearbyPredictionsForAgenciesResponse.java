/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Contains predictions for multiple stops. Has information for agency as well since intended to be
 * used when providing predictions by location for multiple agencies.
 *
 * @author Michael
 */
@Data
public class ApiNearbyPredictionsForAgenciesResponse {

    @JsonProperty
    private List<ApiPredictionsResponse> data;

    public ApiNearbyPredictionsForAgenciesResponse() {
        data = new ArrayList<>();
    }

    /**
     * Adds predictions for an agency.
     *
     * @param apiPreds
     */
    public void addPredictionsForAgency(ApiPredictionsResponse apiPreds) {
        data.add(apiPreds);
    }
}
