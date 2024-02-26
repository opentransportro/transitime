/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.transitclock.service.dto.IpcPredictionsForRouteStopDest;
import org.transitclock.utils.MathUtils;

/**
 * List of ApiPredictionDestination objects along with supporting information. Used to output
 * predictions for a particular stop where the predictions are grouped by headsign.
 *
 * @author SkiBu Smith
 */
@XmlRootElement
public class ApiPredictionRouteStop {

    @XmlAttribute
    private String routeShortName;

    @XmlAttribute
    private String routeName;

    @XmlAttribute
    private String routeId;

    @XmlAttribute
    private String stopId;

    @XmlAttribute
    private String stopName;

    @XmlAttribute
    private Integer stopCode;

    // Using String so that it will not be output if not showing predictions
    // by location because then this value will be null. Also, can this way
    // format it to desired number of digits of precision.
    @XmlAttribute
    private Double distanceToStop;

    @XmlElement(name = "dest")
    private List<ApiPredictionDestination> destinations;


    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiPredictionRouteStop() {}

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

        destinations = new ArrayList<ApiPredictionDestination>();
        for (IpcPredictionsForRouteStopDest destinationInfo : predictionsForRouteStop) {
            destinations.add(new ApiPredictionDestination(destinationInfo));
        }
    }
}
