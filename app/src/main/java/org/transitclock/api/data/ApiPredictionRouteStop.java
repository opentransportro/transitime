/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;

import org.transitclock.service.dto.IpcPredictionsForRouteStopDest;
import org.transitclock.utils.MathUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * List of ApiPredictionDestination objects along with supporting information. Used to output
 * predictions for a particular stop where the predictions are grouped by headsign.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiPredictionRouteStop {

    @JsonProperty
    private String routeShortName;

    @JsonProperty
    private String routeName;

    @JsonProperty
    private String routeId;

    @JsonProperty
    private String stopId;

    @JsonProperty
    private String stopName;

    @JsonProperty
    private Integer stopCode;

    // Using String so that it will not be output if not showing predictions
    // by location because then this value will be null. Also, can this way
    // format it to desired number of digits of precision.
    @JsonProperty
    private Double distanceToStop;

    @JsonProperty
    private List<ApiPredictionDestination> destinations;

    public ApiPredictionRouteStop(List<IpcPredictionsForRouteStopDest> predictionsForRouteStop) {
        if (predictionsForRouteStop == null || predictionsForRouteStop.isEmpty()) return;

        IpcPredictionsForRouteStopDest routeStopInfo = predictionsForRouteStop.get(0);
        routeShortName = routeStopInfo.getRouteShortName();
        routeName = routeStopInfo.getRouteName();
        routeId = routeStopInfo.getRouteId();
        stopId = routeStopInfo.getStopId();
        stopName = routeStopInfo.getStopName();
        stopCode = routeStopInfo.getStopCode();
        distanceToStop = Double.isNaN(routeStopInfo.getDistanceToStop())
                ? null
                : MathUtils.round(routeStopInfo.getDistanceToStop(), 1);

        destinations = new ArrayList<>();
        for (IpcPredictionsForRouteStopDest destinationInfo : predictionsForRouteStop) {
            destinations.add(new ApiPredictionDestination(destinationInfo));
        }
    }
}
