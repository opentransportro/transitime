/* (C)2023 */
package org.transitclock.api.data;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.transitclock.service.dto.IpcArrivalDeparture;

/**
 * An ordered list of routes.
 *
 * @author SkiBu Smith
 */
@XmlRootElement(name = "arrivalDepartures")
public class ApiArrivalDepartures {

    @XmlElement(name = "arrivalDeparture")
    private List<ApiArrivalDeparture> arrivalDeparturesData;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiArrivalDepartures() {}

    /**
     * Constructs an ApiRouteSummaries using a collection of IpcRouteSummary objects.
     *
     * @param routes
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public ApiArrivalDepartures(Collection<IpcArrivalDeparture> arrivalDepartures)
            throws IllegalAccessException, InvocationTargetException {
        arrivalDeparturesData = new ArrayList<ApiArrivalDeparture>();
        for (IpcArrivalDeparture arrivalDeparture : arrivalDepartures) {
            ApiArrivalDeparture apiArrivalDeparture = new ApiArrivalDeparture(arrivalDeparture);
            arrivalDeparturesData.add(apiArrivalDeparture);
        }
    }
}
