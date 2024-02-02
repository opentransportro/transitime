/* (C)2023 */
package org.transitclock.api.resources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.checkerframework.checker.units.qual.C;
import org.springframework.web.bind.annotation.RestController;
import org.transitclock.api.data.siri.SiriStopMonitoring;
import org.transitclock.api.data.siri.SiriVehiclesMonitoring;
import org.transitclock.api.utils.StandardParameters;
import org.transitclock.api.utils.WebUtils;
import org.transitclock.service.contract.ConfigInterface;
import org.transitclock.service.dto.IpcPrediction;
import org.transitclock.service.dto.IpcPredictionsForRouteStopDest;
import org.transitclock.service.dto.IpcVehicleComplete;
import org.transitclock.service.contract.PredictionsInterface;
import org.transitclock.service.contract.VehiclesInterface;

/**
 * The Siri API
 *
 * @author SkiBu Smith
 */
@RestController
@Path("/key/{key}/agency/{agency}")
public class SiriApi {

    private final VehiclesInterface vehiclesInterface;
    private final PredictionsInterface predictionsInterface;

    private final ConfigInterface configInterface;

    public SiriApi(VehiclesInterface vehiclesInterface, PredictionsInterface predictionsInterface, ConfigInterface configInterface) {
        this.vehiclesInterface = vehiclesInterface;
        this.predictionsInterface = predictionsInterface;
        this.configInterface = configInterface;
    }

    /**
     * Returns vehicleMonitoring vehicle information in SIRI format. Can specify vehicleIds,
     * routeIds, or routeShortNames to get subset of data. If not specified then vehicle information
     * for entire agency is returned.
     *
     * @param stdParameters
     * @param vehicleIds List of vehicle IDs
     * @param routesIdOrShortNames List of routes
     * @return The response
     * @throws WebApplicationException
     */
    @Path("/command/siri/vehicleMonitoring")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Returns vehicleMonitoring vehicle information in SIRI format.",
            description = "It is possible to specify vehicleIds, routeIds, or routeShortNames to get"
                    + " subset of data. If not specified then vehicle information for entire"
                    + " agency is returned.",
            tags = {"SIRI", "feed"})
    public Response getVehicles(
            @BeanParam StandardParameters stdParameters,
            @Parameter(description = "List of vehicles id", required = false) @QueryParam(value = "v")
                    List<String> vehicleIds,
            @Parameter(description = "List of routesId or routeShortName", required = false) @QueryParam(value = "r")
                    List<String> routesIdOrShortNames)
            throws WebApplicationException {
        // Make sure request is valid
        stdParameters.validate();

        try {
            // Get Vehicle data from server
            Collection<IpcVehicleComplete> vehicles;
            if (!routesIdOrShortNames.isEmpty()) {
                vehicles = vehiclesInterface.getCompleteForRoute(routesIdOrShortNames);
            } else if (!vehicleIds.isEmpty()) {
                vehicles = vehiclesInterface.getComplete(vehicleIds);
            } else {
                vehicles = vehiclesInterface.getComplete();
            }

            // Determine and return SiriStopMonitoring response
            SiriVehiclesMonitoring siriVehicles = new SiriVehiclesMonitoring(vehicles, stdParameters.getAgencyId(), configInterface);
            return stdParameters.createResponse(siriVehicles);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    /**
     * Returns stopMonitoring vehicle information in SIRI format. Can specify routeId or
     * routeShortName. Need to also specify stopId. Can optionally specify how many max number of
     * predictions per stop to return.
     *
     * @param stdParameters
     * @param routeIdOrShortName
     * @param stopId
     * @param numberPredictions
     * @return
     * @throws WebApplicationException
     */
    @Path("/command/siri/stopMonitoring")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Returns stopMonitoring vehicle information in SIRI format.",
            description = "It is possible to specify vehicleIds, routeIds, or routeShortNames to get"
                    + " subset of data. It is possible to specify the number of perdictions per"
                    + " stop. If not specified then vehicle information for entire agency is"
                    + " returned.",
            tags = {"SIRI", "feed"})
    public Response getVehicles(
            @BeanParam StandardParameters stdParameters,
            @Parameter(description = "RoutesId or routeShortName", required = true) @QueryParam(value = "r")
                    String routeIdOrShortName,
            @Parameter(description = "StopIds", required = true) @QueryParam(value = "s") String stopId,
            @Parameter(description = "Number of predictions", required = false)
                    @QueryParam(value = "numPreds")
                    @DefaultValue("3")
                    int numberPredictions)
            throws WebApplicationException {
        // Make sure request is valid
        stdParameters.validate();

        try {
            // Get prediction data from server
            List<IpcPredictionsForRouteStopDest> preds = predictionsInterface.get(routeIdOrShortName, stopId, numberPredictions);

            // For each prediction also need corresponding vehicle so can create
            // the absurdly large MonitoredVehicleJourney element.
            List<String> vehicleIds = new ArrayList<>();
            for (IpcPredictionsForRouteStopDest predsForDest : preds) {
                for (IpcPrediction individualPred : predsForDest.getPredictionsForRouteStop()) {
                    vehicleIds.add(individualPred.getVehicleId());
                }
            }
            Collection<IpcVehicleComplete> vehicles = vehiclesInterface.getComplete(vehicleIds);

            // Determine SiriStopMonitoring response
            SiriStopMonitoring siriStopMonitoring =
                    new SiriStopMonitoring(preds, vehicles, stdParameters.getAgencyId(), configInterface);

            // Return SiriStopMonitoring response
            return stdParameters.createResponse(siriStopMonitoring);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }
}
