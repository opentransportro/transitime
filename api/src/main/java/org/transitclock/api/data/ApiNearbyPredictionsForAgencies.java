/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Contains predictions for multiple stops. Has information for agency as well since intended to be
 * used when providing predictions by location for multiple agencies.
 *
 * @author Michael
 */
@XmlRootElement(name = "preds")
public class ApiNearbyPredictionsForAgencies {

    @XmlElement(name = "agencies")
    private List<ApiPredictions> predictionsForAgency;

    /** Constructor. Method addPredictionsForAgency() called to actually add data. */
    public ApiNearbyPredictionsForAgencies() {
        predictionsForAgency = new ArrayList<ApiPredictions>();
    }

    /**
     * Adds predictions for an agency.
     *
     * @param apiPreds
     */
    public void addPredictionsForAgency(ApiPredictions apiPreds) {
        predictionsForAgency.add(apiPreds);
    }
}
