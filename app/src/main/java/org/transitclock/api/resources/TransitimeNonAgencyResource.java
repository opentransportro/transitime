/* (C)2023 */
package org.transitclock.api.resources;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.transitclock.api.data.ApiAgenciesResponse;
import org.transitclock.api.data.ApiAgency;
import org.transitclock.api.data.ApiNearbyPredictionsForAgenciesResponse;
import org.transitclock.api.data.ApiPredictionsResponse;
import org.transitclock.api.utils.PredsByLoc;
import org.transitclock.api.utils.StandardParameters;
import org.transitclock.domain.structs.Agency;
import org.transitclock.domain.structs.Location;
import org.transitclock.domain.webstructs.WebAgency;
import org.transitclock.service.dto.IpcPredictionsForRouteStopDest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Contains the API commands for the Transitime API for system wide commands, such as determining
 * all agencies. The intent of this feed is to provide what is needed for creating a user interface
 * application, such as a smartphone application.
 *
 * <p>The data output can be in either JSON or XML. The output format is specified by the accept
 * header or by using the query string parameter "format=json" or "format=xml".
 *
 * @author SkiBu Smith
 */
@RestController
public class TransitimeNonAgencyResource extends BaseApiResource implements TransitimeNonAgencyApi {
    @Override
    public ResponseEntity<ApiAgenciesResponse> getAgencies(StandardParameters stdParameters) {
        // Make sure request is valid
        validate(stdParameters);

        List<ApiAgency> apiAgencyList = new ArrayList<>();
        List<WebAgency> webAgencies = WebAgency.getCachedOrderedListOfWebAgencies();

        for (WebAgency webAgency : webAgencies) {
            String agencyId = webAgency.getAgencyId();
            List<Agency> agencies = configInterface.getAgencies();

            for (Agency agency : agencies) {
                apiAgencyList.add(new ApiAgency(agencyId, agency));
            }
        }
        ApiAgenciesResponse apiAgencies = new ApiAgenciesResponse(apiAgencyList);
        return ResponseEntity.ok(apiAgencies);
    }

    @Override
    public ResponseEntity<ApiNearbyPredictionsForAgenciesResponse> getPredictions(
            StandardParameters stdParameters,
            Double lat,
            Double lon,
            double maxDistance,
            int numberPredictions) {
        // Make sure request is valid
        validate(stdParameters);

        if (maxDistance > PredsByLoc.MAX_MAX_DISTANCE)
            throw new RuntimeException("Maximum maxDistance parameter "
                + "is "
                + PredsByLoc.MAX_MAX_DISTANCE
                + "m but "
                + maxDistance
                + "m was specified in the request.");

        ApiNearbyPredictionsForAgenciesResponse predsForAgencies = new ApiNearbyPredictionsForAgenciesResponse();

        // For each nearby agency...
        List<String> nearbyAgencies = PredsByLoc.getNearbyAgencies(configInterface, lat, lon, maxDistance);
        for (String agencyId : nearbyAgencies) {
            // Get predictions by location for the agency
            List<IpcPredictionsForRouteStopDest> predictions =
                predictionsInterface.get(new Location(lat, lon), maxDistance, numberPredictions);

            // Convert predictions to API object
            ApiPredictionsResponse predictionsData = new ApiPredictionsResponse(predictions);

            // Add additional agency related info so can describe the
            // agency in the API.
            WebAgency webAgency = WebAgency.getCachedWebAgency(agencyId);
            String agencyName = webAgency.getAgencyName();
            predictionsData.set(agencyId, agencyName);

            // Add the predictions for the agency to the predictions to
            // be returned
            predsForAgencies.addPredictionsForAgency(predictionsData);
        }
        return ResponseEntity.ok(predsForAgencies);
    }
}
