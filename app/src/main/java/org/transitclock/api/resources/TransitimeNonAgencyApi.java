/* (C)2023 */
package org.transitclock.api.resources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.transitclock.api.data.*;
import org.transitclock.api.utils.PredsByLoc;
import org.transitclock.api.utils.StandardParameters;
import org.transitclock.api.utils.WebUtils;
import org.transitclock.domain.structs.Agency;
import org.transitclock.domain.structs.Location;
import org.transitclock.domain.webstructs.WebAgency;
import org.transitclock.service.dto.IpcPredictionsForRouteStopDest;
import org.transitclock.service.contract.ConfigInterface;
import org.transitclock.service.contract.PredictionsInterface;
import org.transitclock.service.ConfigServiceImpl;
import org.transitclock.service.PredictionsServiceImpl;

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
@Path("")
public class TransitimeNonAgencyApi {

    /**
     * For "agencies" command. Returns information for all configured agencies.
     *
     * @param stdParameters
     * @return
     * @throws WebApplicationException
     */
    @Path("/command/agencies")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Retrieves all tha agencies managed by the server.",
            description = "Retrieves all tha agencies managed by the server.",
            tags = {"agency"})

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(schema = @Schema(implementation = ApiAgencies.class))
            })
    })
    public Response getAllAgencies(@BeanParam StandardParameters stdParameters) throws WebApplicationException {
        // Make sure request is valid
        stdParameters.validate();

        // For each agency handled by this server create an ApiAgencies
        // and return the list.
        List<ApiAgency> apiAgencyList = new ArrayList<>();
        Collection<WebAgency> webAgencies = WebAgency.getCachedOrderedListOfWebAgencies();
        for (WebAgency webAgency : webAgencies) {
            String agencyId = webAgency.getAgencyId();
            ConfigInterface inter = ConfigServiceImpl.instance();

            // If can't communicate with IPC with that agency then move on
            // to the next one. This is important because some agencies
            // might be declared in the web db but they might not actually
            // be running.
            if (inter == null) {
                // Should really log something here to explain that skipping
                // agency

                continue;
            }

            List<Agency> agencies = inter.getAgencies();
            for (Agency agency : agencies) {
                apiAgencyList.add(new ApiAgency(agencyId, agency));
            }
        }
        ApiAgencies apiAgencies = new ApiAgencies(apiAgencyList);
        return stdParameters.createResponse(apiAgencies);
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
     * @throws WebApplicationException
     */
    @Path("/command/bounded-predictions")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Gets predictions from server by location",
            description = "Gets a list of prediction by location for all agencies managed by the api.",
            tags = {"prediction"})

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(schema = @Schema(implementation = ApiNearbyPredictionsForAgencies.class))
            })
    })
    public Response getBoundedPredictions(
            @BeanParam StandardParameters stdParameters,
            @QueryParam(value = "lat") Double lat,
            @QueryParam(value = "lon") Double lon,
            @QueryParam(value = "maxDistance") @DefaultValue("1500.0") double maxDistance,
            @QueryParam(value = "numPreds") @DefaultValue("3") int numberPredictions)
            throws WebApplicationException {
        // Make sure request is valid
        stdParameters.validate();

        if (maxDistance > PredsByLoc.MAX_MAX_DISTANCE)
            throw WebUtils.badRequestException("Maximum maxDistance parameter "
                    + "is "
                    + PredsByLoc.MAX_MAX_DISTANCE
                    + "m but "
                    + maxDistance
                    + "m was specified in the request.");

        try {
            ApiNearbyPredictionsForAgencies predsForAgencies = new ApiNearbyPredictionsForAgencies();

            // For each nearby agency...
            List<String> nearbyAgencies = PredsByLoc.getNearbyAgencies(lat, lon, maxDistance);
            for (String agencyId : nearbyAgencies) {
                // Get predictions by location for the agency
                PredictionsInterface predictionsInterface = PredictionsServiceImpl.instance();
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
            return stdParameters.createResponse(predsForAgencies);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }
}
