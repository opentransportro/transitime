/* (C)2023 */
package org.transitclock.api.resources;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.transitclock.api.data.ApiAgencies;
import org.transitclock.api.data.ApiAgency;
import org.transitclock.api.data.ApiNearbyPredictionsForAgencies;
import org.transitclock.api.data.ApiPredictions;
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
@RequestMapping("/api/v1/key/{key}")
public class TransitimeNonAgencyApi extends BaseApiResource {
    /**
     * For "agencies" command. Returns information for all configured agencies.
     *
     * @param stdParameters
     * @return
     */
    @GetMapping(
        value = "/command/agencies",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @Operation(
            summary = "Rerives all tha agencies managed by the server.",
            description = "Rerives all tha agencies managed by the server.",
            tags = {"base data", "agency"})
    public ResponseEntity<ApiAgencies> getAgencies(StandardParameters stdParameters) {
        // Make sure request is valid
        validate(stdParameters);

        // For each agency handled by this server create an ApiAgencies
        // and return the list.
        List<ApiAgency> apiAgencyList = new ArrayList<>();
        Collection<WebAgency> webAgencies = WebAgency.getCachedOrderedListOfWebAgencies();
        for (WebAgency webAgency : webAgencies) {
            String agencyId = webAgency.getAgencyId();

            // If can't communicate with IPC with that agency then move on
            // to the next one. This is important because some agencies
            // might be declared in the web db but they might not actually
            // be running.
            if (configInterface == null) {
                // Should really log something here to explain that skipping
                // agency

                continue;
            }

            List<Agency> agencies = configInterface.getAgencies();
            for (Agency agency : agencies) {
                apiAgencyList.add(new ApiAgency(agencyId, agency));
            }
        }
        ApiAgencies apiAgencies = new ApiAgencies(apiAgencyList);
        return ResponseEntity.ok(apiAgencies);
    }

    /**
     * For "predictionsByLoc" command when want to return data for any agency instead of a single
     * specific one.
     *
     * @param stdParameters StdParametersBean that gets the standard parameters from the URI, query
     *     string, and headers.
     * @param lat latitude in decimal degrees
     * @param lon longitude in decimal degrees
     * @param maxDistance How far away a stop can be from the lat/lon. Default is 1,500 m.
     * @param numberPredictions Maximum number of predictions to return. Default value is 3.
     * @return
     */
    @GetMapping(
        value = "/command/predictionsByLoc",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @Operation(
            summary = "Gets predictions from server by location",
            description = "Gets a list of prediction by location for all angencies managed by the api.",
            tags = {"prediction"})
    public ResponseEntity<ApiNearbyPredictionsForAgencies> getPredictions(
            StandardParameters stdParameters,
            @RequestParam(name = "lat") Double lat,
            @RequestParam(name = "lon") Double lon,
            @RequestParam(name = "maxDistance", defaultValue = "1500.0") double maxDistance,
            @RequestParam(name = "numPreds", defaultValue = "3") int numberPredictions) {
        // Make sure request is valid
        validate(stdParameters);

        if (maxDistance > PredsByLoc.MAX_MAX_DISTANCE)
            throw new RuntimeException("Maximum maxDistance parameter "
                + "is "
                + PredsByLoc.MAX_MAX_DISTANCE
                + "m but "
                + maxDistance
                + "m was specified in the request.");

        ApiNearbyPredictionsForAgencies predsForAgencies = new ApiNearbyPredictionsForAgencies();

        // For each nearby agency...
        List<String> nearbyAgencies = PredsByLoc.getNearbyAgencies(configInterface, lat, lon, maxDistance);
        for (String agencyId : nearbyAgencies) {
            // Get predictions by location for the agency
            List<IpcPredictionsForRouteStopDest> predictions =
                predictionsInterface.get(new Location(lat, lon), maxDistance, numberPredictions);

            // Convert predictions to API object
            ApiPredictions predictionsData = new ApiPredictions(predictions);

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
