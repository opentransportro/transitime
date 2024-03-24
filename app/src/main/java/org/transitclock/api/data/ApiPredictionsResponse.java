/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;

import org.transitclock.service.dto.IpcPredictionsForRouteStopDest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Contains predictions for multiple routes/stops. Can also contain info for the agency.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiPredictionsResponse {

    // Optional additional info. Needed for when providing predictions for
    // multiple agencies, such as when getting predictions by location
    @JsonProperty
    private String agencyId;

    // Optional additional info. Needed for when providing predictions for
    // multiple agencies, such as when getting predictions by location
    @JsonProperty
    private String agencyName;

    // The actual predictions, by route & stop
    @JsonProperty
    private List<ApiPredictionRouteStop> data;

    public ApiPredictionsResponse(List<IpcPredictionsForRouteStopDest> predsForRouteStopDestinations) {
        data = new ArrayList<>();

        // Get all the PredictionsForRouteStopDest that are for the same
        // route/stop and create a PredictionsRouteStopData object for each
        // route/stop.
        List<IpcPredictionsForRouteStopDest> predsForRouteStop = null;
        String previousRouteStopStr = "";
        for (IpcPredictionsForRouteStopDest predsForRouteStopDest : predsForRouteStopDestinations) {
            // If this is a new route/stop...
            String currentRouteStopStr = predsForRouteStopDest.getRouteId() + predsForRouteStopDest.getStopId();
            if (!currentRouteStopStr.equals(previousRouteStopStr)) {
                // This is a new route/stop
                if (predsForRouteStop != null && !predsForRouteStop.isEmpty()) {
                    // create PredictionsRouteStopData object for this route/stop
                    ApiPredictionRouteStop predictionsForRouteStopData = new ApiPredictionRouteStop(predsForRouteStop);
                    data.add(predictionsForRouteStopData);
                }
                predsForRouteStop = new ArrayList<>();
                previousRouteStopStr = currentRouteStopStr;
            }
            predsForRouteStop.add(predsForRouteStopDest);
        }

        // Add the last set of route/stop data
        ApiPredictionRouteStop predictionsForRouteStopData = new ApiPredictionRouteStop(predsForRouteStop);
        data.add(predictionsForRouteStopData);
    }

    /**
     * For setting info about the agency. Needed for when providing predictions for multiple
     * agencies, such as when getting predictions by location
     *
     * @param agencyId
     * @param agencyName
     */
    public void set(String agencyId, String agencyName) {
        this.agencyId = agencyId;
        this.agencyName = agencyName;
    }
}
