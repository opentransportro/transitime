/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.transitclock.service.dto.IpcPrediction;
import org.transitclock.service.dto.IpcPredictionsForRouteStopDest;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains list of predictions for a particular headsign.
 *
 * @author SkiBu Smith
 */@Data
@XmlRootElement
public class ApiPredictionDestination {

    @XmlAttribute(name = "dir")
    private String directionId;

    @XmlAttribute
    private String headsign;

    @XmlElement(name = "pred")
    private List<ApiPrediction> predictions;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiPredictionDestination() {}

    public ApiPredictionDestination(IpcPredictionsForRouteStopDest predictionsForRouteStop) {
        directionId = predictionsForRouteStop.getDirectionId();
        headsign = predictionsForRouteStop.getHeadsign();

        predictions = new ArrayList<ApiPrediction>();
        for (IpcPrediction prediction : predictionsForRouteStop.getPredictionsForRouteStop()) {
            predictions.add(new ApiPrediction(prediction));
        }
    }
}
