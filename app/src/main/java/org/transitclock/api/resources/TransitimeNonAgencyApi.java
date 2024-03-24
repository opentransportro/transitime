package org.transitclock.api.resources;

import org.transitclock.api.data.ApiAgenciesResponse;
import org.transitclock.api.data.ApiNearbyPredictionsForAgenciesResponse;
import org.transitclock.api.utils.StandardParameters;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/v1/key/{key}")
public interface TransitimeNonAgencyApi {
    /**
     * For "agencies" command. Returns information for all configured agencies.
     *
     * @param stdParameters
     *
     * @return
     */
    @GetMapping(
            value = "/command/agencies",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @Operation(
            summary = "Rerives all tha agencies managed by the server.",
            description = "Retrieves all tha agencies managed by the server.",
            tags = {"base data", "agency"})
    ResponseEntity<ApiAgenciesResponse> getAgencies(StandardParameters stdParameters);

    /**
     * For "predictionsByLoc" command when want to return data for any agency instead of a single
     * specific one.
     *
     * @param stdParameters     StdParametersBean that gets the standard parameters from the URI, query
     *                          string, and headers.
     * @param lat               latitude in decimal degrees
     * @param lon               longitude in decimal degrees
     * @param maxDistance       How far away a stop can be from the lat/lon. Default is 1,500 m.
     * @param numberPredictions Maximum number of predictions to return. Default value is 3.
     *
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
    ResponseEntity<ApiNearbyPredictionsForAgenciesResponse> getPredictions(
            StandardParameters stdParameters,
            @RequestParam(name = "lat") Double lat,
            @RequestParam(name = "lon") Double lon,
            @RequestParam(name = "maxDistance", defaultValue = "1500.0") double maxDistance,
            @RequestParam(name = "numPreds", defaultValue = "3") int numberPredictions);
}
