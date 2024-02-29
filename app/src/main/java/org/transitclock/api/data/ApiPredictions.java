/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.transitclock.service.dto.IpcPredictionsForRouteStopDest;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains predictions for multiple routes/stops. Can also contain info for the agency.
 *
 * @author SkiBu Smith
 */@Data
@XmlRootElement(name = "preds")
public class ApiPredictions {

    // Optional additional info. Needed for when providing predictions for
    // multiple agencies, such as when getting predictions by location
    @XmlAttribute
    private String agencyId = null;

    // Optional additional info. Needed for when providing predictions for
    // multiple agencies, such as when getting predictions by location
    @XmlAttribute
    private String agencyName = null;

    // The actual predictions, by route & stop
    @XmlElement(name = "predictions")
    private List<ApiPredictionRouteStop> predictionsForRouteStop;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    public ApiPredictions() {}

    /**
     * For constructing a ApiPredictions object from a List of IpcPredictionsForRouteStopDest
     * objects.
     *
     * @param vehicles
     */
    public ApiPredictions(List<IpcPredictionsForRouteStopDest> predsForRouteStopDestinations) {
        predictionsForRouteStop = new ArrayList<ApiPredictionRouteStop>();

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
                    // create PredictionsRouteStopData object for this
                    // route/stop
                    ApiPredictionRouteStop predictionsForRouteStopData = new ApiPredictionRouteStop(predsForRouteStop);
                    predictionsForRouteStop.add(predictionsForRouteStopData);
                }
                predsForRouteStop = new ArrayList<IpcPredictionsForRouteStopDest>();
                previousRouteStopStr = currentRouteStopStr;
            }
            predsForRouteStop.add(predsForRouteStopDest);
        }

        // Add the last set of route/stop data
        ApiPredictionRouteStop predictionsForRouteStopData = new ApiPredictionRouteStop(predsForRouteStop);
        predictionsForRouteStop.add(predictionsForRouteStopData);
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
