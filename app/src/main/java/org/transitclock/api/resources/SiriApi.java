/* (C)2023 */
package org.transitclock.api.resources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.transitclock.api.data.siri.SiriStopMonitoring;
import org.transitclock.api.data.siri.SiriVehiclesMonitoring;
import org.transitclock.api.utils.StandardParameters;
import org.transitclock.service.dto.IpcPrediction;
import org.transitclock.service.dto.IpcPredictionsForRouteStopDest;
import org.transitclock.service.dto.IpcVehicleComplete;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The Siri API
 *
 * @author SkiBu Smith
 */
@RestController
@RequestMapping("/api/v1/key/{key}/agency/{agency}")
public class SiriApi extends BaseApiResource {

    /**
     * Returns vehicleMonitoring vehicle information in SIRI format. Can specify vehicleIds,
     * routeIds, or routeShortNames to get subset of data. If not specified then vehicle information
     * for entire agency is returned.
     *
     * @param stdParameters
     * @param vehicleIds List of vehicle IDs
     * @param routesIdOrShortNames List of routes
     * @return The response
     */
    @GetMapping(
        value = "/command/siri/vehicleMonitoring",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @Operation(
            summary = "Returns vehicleMonitoring vehicle information in SIRI format.",
            description = "It is possible to specify vehicleIds, routeIds, or routeShortNames to get"
                    + " subset of data. If not specified then vehicle information for entire"
                    + " agency is returned.",
            tags = {"SIRI", "feed"})
    public ResponseEntity<SiriVehiclesMonitoring> getVehicles(
            StandardParameters stdParameters,
            @Parameter(description = "List of vehicles id", required = false)
            @RequestParam(value = "v", required = false) List<String> vehicleIds,
            @Parameter(description = "List of routesId or routeShortName", required = false)
            @RequestParam(value = "r", required = false) List<String> routesIdOrShortNames) {
        // Make sure request is valid
        validate(stdParameters);

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
        SiriVehiclesMonitoring siriVehicles = new SiriVehiclesMonitoring(vehicles, stdParameters.getAgencyId());
        return ResponseEntity.ok(siriVehicles);
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
     */
    @GetMapping(
        value = "/command/siri/stopMonitoring",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @Operation(
            summary = "Returns stopMonitoring vehicle information in SIRI format.",
            description = "It is possible to specify vehicleIds, routeIds, or routeShortNames to get"
                    + " subset of data. It is possible to specify the number of perdictions per"
                    + " stop. If not specified then vehicle information for entire agency is"
                    + " returned.",
            tags = {"SIRI", "feed"})
    public ResponseEntity<SiriStopMonitoring> getVehicles(
            StandardParameters stdParameters,
            @Parameter(description = "RoutesId or routeShortName", required = true)
            @RequestParam(value = "r") String routeIdOrShortName,
            @Parameter(description = "StopIds", required = true)
            @RequestParam(value = "s") String stopId,
            @Parameter(description = "Number of predictions", required = false)
            @RequestParam(value = "numPreds", required = false, defaultValue = "3") int numberPredictions
    ) {
        // Make sure request is valid
        validate(stdParameters);

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
                    new SiriStopMonitoring(preds, vehicles, stdParameters.getAgencyId());

            // Return SiriStopMonitoring response
            return ResponseEntity.ok(siriStopMonitoring);
    }
}
