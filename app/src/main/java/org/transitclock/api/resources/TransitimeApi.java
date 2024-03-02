/* (C)2023 */
package org.transitclock.api.resources;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.transitclock.api.data.*;
import org.transitclock.api.utils.PredsByLoc;
import org.transitclock.api.utils.StandardParameters;
import org.transitclock.api.utils.WebUtils;
import org.transitclock.core.TemporalDifference;
import org.transitclock.core.reports.Reports;
import org.transitclock.domain.hibernate.HibernateUtils;
import org.transitclock.domain.structs.Agency;
import org.transitclock.domain.structs.ExportTable;
import org.transitclock.domain.structs.Location;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.service.contract.PredictionsInterface.RouteStop;
import org.transitclock.service.dto.*;

import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Contains the API commands for the Transitime API for getting real-time vehicle and prediction
 * information plus the static configuration information. The intent of this feed is to provide what
 * is needed for creating a user interface application, such as a smartphone application.
 *
 * <p>The data output can be in either JSON or XML. The output format is specified by the accept
 * header or by using the query string parameter "format=json" or "format=xml".
 *
 * @author SkiBu Smith
 */
@RestController
@OpenAPIDefinition(
        info =
                @Info(
                        title = "TrasnsitClockAPI",
                        version = "1.0",
                        description = "TheTransitClock is an open source transit information system. It’s"
                                + " core function is to provide and analyze arrival predictions"
                                + " for transit systems.<br>Here you will find the detailed"
                                + " description of The Transit Clock API.<br>For more"
                                + " information visit <a"
                                + " href=\"https://thetransitclock.github.io/\">thetransitclock.github.io.</a><br>"
                                + " The original documentation can be found in <a"
                                + " href=\"https://github.com/Transitime/core/wiki/API\">Api"
                                + " doc</a>."),
        servers = {@Server(url = "/api/v1")})
@RequestMapping("/api/v1/key/{key}/agency/{agency}")
@RequiredArgsConstructor
public class TransitimeApi extends BaseApiResource {
    // For specifying how vehicles should be drawn in the UI.
    public enum UiMode {
        NORMAL,
        SECONDARY,
        MINOR
    }
    private final DbConfig dbConfig;

    /**
     * Handles the "vehicles" command. Returns data for all vehicles or for the vehicles specified
     * via the query string.
     *
     * <p>A Response object is returned instead of a regular object so that can have one method for
     * the both XML and JSON yet always return the proper media type even if it is configured via
     * the query string "format" parameter as opposed to the accept header.
     *
     * @param stdParameters StdParametersBean that gets the standard parameters from the URI, query
     *     string, and headers.
     * @param vehicleIds Optional way of specifying which vehicles to get data for
     * @param routesIdOrShortNames Optional way of specifying which routes to get data for
     * @param stopId Optional way of specifying a stop so can get predictions for routes and
     *     determine which vehicles are the ones generating the predictions. The other vehicles are
     *     labeled as minor so they can be drawn specially in the UI.
     * @param numberPredictions For when determining which vehicles are generating the predictions
     *     so can label minor vehicles
     * @return The Response object already configured for the specified media type.
     */
    @Operation(
            summary = "Returns data for all vehicles or for the vehicles specified via the query" + " string.",
            description = "Returns data for all vehicles or for the vehicles specified via the query" + " string.",
            tags = {"vehicle", "prediction"})
    @GetMapping(
        value = "/command/vehicles",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    public ResponseEntity<ApiVehicles> getVehicles(
            StandardParameters stdParameters,
            @Parameter(description = "Vehicles is list.")
            @RequestParam(value = "v", required = false) List<String> vehicleIds,
            @Parameter(description = "Specifies which vehicles to get data for.", required = false)
            @RequestParam(value = "r", required = false) List<String> routesIdOrShortNames,
            @Parameter(
                description = "Specifies a stop so can get predictions for routes and"
                        + " determine which vehicles are the ones generating the"
                        + " predictions. The other vehicles are labeled as minor so"
                        + " they can be drawn specially in the UI.",
                required = false)
            @RequestParam(value = "s", required = false) String stopId,
            @Parameter(description = "Number of predictions to show.", required = false)
            @RequestParam(value = "numPreds", required = false, defaultValue = "2") int numberPredictions) throws RemoteException {
        // Make sure request is valid
        validate(stdParameters);

        // Get Vehicle data from server
        Collection<IpcVehicle> vehicles;
        if (!routesIdOrShortNames.isEmpty() && !routesIdOrShortNames.get(0).trim().isEmpty()) {
            vehicles = vehiclesInterface.getForRoute(routesIdOrShortNames);
        } else if (!vehicleIds.isEmpty() && !vehicleIds.get(0).trim().isEmpty()) {
            vehicles = vehiclesInterface.get(vehicleIds);
        } else {
            vehicles = vehiclesInterface.get();
        }

        // If the vehicles doesn't exist then throw exception such that
        // Bad Request with an appropriate message is returned.
        if (vehicles == null)
            throw WebUtils.badRequestException("Invalid specifier for vehicles");

        // To determine how vehicles should be drawn in UI. If stop
        // specified
        // when getting vehicle info then only the vehicles being predicted
        // for, should be highlighted. The others should be dimmed.
        Map<String, UiMode> uiTypesForVehicles = determineUiModesForVehicles(
            vehicles, stdParameters, routesIdOrShortNames, stopId, numberPredictions);

        ApiVehicles apiVehicles = new ApiVehicles(vehicles, uiTypesForVehicles);

        // return ApiVehicles response
        return ResponseEntity.ok(apiVehicles);
    }

    @Operation(
            summary = "Returns data for vehicles assignment for specific block in current day",
            description = "Returns data for vehicles assignment for specific block in current day")
    @GetMapping(
        value = "/command/vehiclesToBlock",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    public ResponseEntity<ApiVehicleToBlockConfigs> getVehiclesToBlock(
            StandardParameters stdParameters,
            @Parameter(description = "Block id")
            @RequestParam(value = "blockId") String blockId) {

        validate(stdParameters);

        // Get Vehicle data from server
        var result = vehiclesInterface.getVehicleToBlockConfig(blockId);
        ApiVehicleToBlockConfigs res = new ApiVehicleToBlockConfigs(result);

        // return ApiVehicles response
        return ResponseEntity.ok(res);
    }

    @Operation(
            summary = "Returns avl report.",
            description = "Returns avl report.",
            tags = {"report", "vehicle"})
    @GetMapping(value = "/reports/avlReport",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity<String> getAvlReport(
            StandardParameters stdParameters,
            @Parameter(description = "Vehicle id") @RequestParam(value = "v") String vehicleId,
            @Parameter(description = "Begin date(MM-DD-YYYY.") @RequestParam(value = "beginDate") String beginDate,
            @Parameter(description = "Num days.", required = false) @RequestParam(value = "numDays", required = false) int numDays,
            @Parameter(description = "Begin time(HH:MM)") @RequestParam(value = "beginTime", required = false) String beginTime,
            @Parameter(description = "End time(HH:MM)") @RequestParam(value = "endTime", required = false) String endTime) {
        validate(stdParameters);
        String response = Reports.getAvlJson(
            stdParameters.getAgencyId(),
            vehicleId, beginDate, String.valueOf(numDays), beginTime, endTime);
        return ResponseEntity.ok(response);
    }

    /**
     * Handles the "tripWithTravelTimes" command which outputs arrival and departures data for the
     * specified trip by date.
     *
     * @param stdParameters
     * @param tripId
     * @param date
     * @return
     */
    @GetMapping(value = "/reports/tripWithTravelTimes",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Gets the arrivals and departures data of a trip.",
            description = "Gets the arrivals and departures data of a trip.",
            tags = {"base data", "trip"})
    public ResponseEntity<String> getTripWithTravelTimes(
            StandardParameters stdParameters,
            @Parameter(description = "Trip id", required = true) @RequestParam(value = "tripId") String tripId,
            @Parameter(description = "Begin date(YYYY-MM-DD).", required = true) @RequestParam(value = "date") String date) {

        // Make sure request is valid
        validate(stdParameters);

        String response = Reports.getTripWithTravelTimes(stdParameters.getAgencyId(), tripId, date);
        return ResponseEntity.ok(response);
    }

    /**
     * Handles the "trips" report which outputs trips by date which contains arrival and departures
     * data.
     *
     * @param stdParameters
     * @param date
     * @return
     * @
     */
    @GetMapping(value = "/reports/tripsByDate",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Gets the trips by date.",
            description = "Gets the trips by date.",
            tags = {"base data", "trip"})
    public ResponseEntity<String> getTrips(
            StandardParameters stdParameters,
            @Parameter(description = "Date(YYYY-MM-DD).", required = true)
            @RequestParam(value = "date") String date) {

        // Make sure request is valid
        validate(stdParameters);

        String response = Reports.getTripsFromArrivalAndDeparturesByDate(stdParameters.getAgencyId(), date);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Returns schedule adherence report.",
            description = "Returns schedule adherence report.",
            tags = {"report", "route", "schedule adherence"})
    @GetMapping(value = "/reports/scheduleAdh",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity<String> scheduleAdhReport(
            StandardParameters stdParameters,
            @Parameter(description = "Route id") @RequestParam(value = "r") String routeId,
            @Parameter(description = "Begin date(MM-DD-YYYY.") @RequestParam(value = "beginDate") String beginDate,
            @Parameter(description = "Num days.", required = false) @RequestParam(value = "numDays", required = false) int numDays,
            @Parameter(description = "Begin time(HH:MM)") @RequestParam(value = "beginTime") String beginTime,
            @Parameter(description = "End time(HH:MM)") @RequestParam(value = "endTime") String endTime,
            @Parameter(description = "Allowable early in mins(default 1.0)")
            @RequestParam(value = "allowableEarly", required = false, defaultValue = "1.0") String allowableEarly,
            @Parameter(description = "Allowable late in mins(default 4.0")
            @RequestParam(value = "allowableLate", required = false, defaultValue = "4.0") String allowableLate) {
        validate(stdParameters);
        String response = Reports.getScheduleAdhByStops(
            stdParameters.getAgencyId(),
            routeId,
            beginDate,
            allowableEarly,
            allowableLate,
            beginTime,
            endTime,
            numDays);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/reports/lastAvlJsonData",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Returns AVL Json data for last 24 hours.",
            description = "Returns AVL Json data for last 24 hours.",
            tags = {"report", "avl", "vehicle"})
    public ResponseEntity<String> getLastAvlJsonData(StandardParameters stdParameters) {
        // Make sure request is valid
        validate(stdParameters);

        String response = Reports.getLastAvlJson(stdParameters.getAgencyId());
        return ResponseEntity.ok(response);
    }

    /**
     * Handles the vehicleIds command. Returns list of vehicle IDs.
     *
     * @param stdParameters
     * @return
     */
    @GetMapping(value = "/command/vehicleIds")
    @Operation(
            summary = "Gets the list of vehicles Id",
            tags = {"vehicle"})
    public ResponseEntity<ApiIds> getVehicleIds(StandardParameters stdParameters) {
        // Make sure request is valid
        validate(stdParameters);

        // Get Vehicle data from server
        List<String> ids = configInterface.getVehicleIds();

        ApiIds apiIds = new ApiIds(ids);
        return ResponseEntity.ok(apiIds);
    }

    @GetMapping(value = "/command/vehicleLocation",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "It gets the location for the specified vehicle.",
            description = "It gets the location for the specified vehicle.",
            tags = {"vehicle"})
    //@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<Location> getVehicleLocation(
            StandardParameters stdParameters,
            @Parameter(description = "Specifies the vehicle from which to get the location from.", required = true)
            @RequestParam(value = "v") String vehicleId) {
        validate(stdParameters);

        // Get Vehicle data from server
        IpcVehicle vehicle = vehiclesInterface.get(vehicleId);
        if (vehicle == null) {
            throw WebUtils.badRequestException("Invalid specifier for " + "vehicle");
        }

        Location matchedLocation = new Location(vehicle.getPredictedLatitude(), vehicle.getPredictedLongitude());
        return ResponseEntity.ok(matchedLocation);
    }

    /**
     * Handles the "vehiclesDetails" command. Returns detailed data for all vehicles or for the
     * vehicles specified via the query string. This data includes things not necessarily intended
     * for the public, such as schedule adherence and driver IDs.
     *
     * <p>A Response object is returned instead of a regular object so that can have one method for
     * the both XML and JSON yet always return the proper media type even if it is configured via
     * the query string "format" parameter as opposed to the accept header.
     *
     * @param stdParameters StdParametersBean that gets the standard parameters from the URI, query
     *     string, and headers.
     * @param vehicleIds Optional way of specifying which vehicles to get data for
     * @param routesIdOrShortNames Optional way of specifying which routes to get data for
     * @param stopId Optional way of specifying a stop so can get predictions for routes and
     *     determine which vehicles are the ones generating the predictions. The other vehicles are
     *     labeled as minor so they can be drawn specially in the UI.
     * @param numberPredictions For when determining which vehicles are generating the predictions
     *     so can label minor vehicles
     * @return The Response object already configured for the specified media type.
     */
    @GetMapping(value = "/command/vehiclesDetails",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Returns detailed data for all " + "vehicles or for the vehicles specified via the query string",
            description = "Returns detailed data for all vehicles or for the vehicles specified via the"
                    + " query string. This data  includes things not necessarily intended for"
                    + " the public, such as schedule adherence and driver IDs.",
            tags = {"vehicle"})
    public ResponseEntity<ApiVehiclesDetails> getVehiclesDetails(
            StandardParameters stdParameters,
            @Parameter(description = "Specifies which vehicles to get data for", required = false)
            @RequestParam(value = "v", required = false, defaultValue = "")
            List<String> vehicleIds,
            @Parameter(description = "Specifies which routes to get data for", required = false)
            @RequestParam(value = "r", required = false, defaultValue = "")
            List<String> routesIdOrShortNames,
            @Parameter(description = "Specifies a stop so can get predictions for routes and"
                    + " determine which vehicles are the ones generating the"
                    + " predictions. The other vehicles are labeled as minor so"
                    + " they can be drawn specially in the UI. ",
                required = false)
            @RequestParam(value = "s", required = false)
            String stopId,
            @Parameter(description = " For when determining which vehicles are generating the"
                            + "predictions so can label minor vehicles",
                    required = false)
            @RequestParam(value = "numPreds", required = false, defaultValue = "3")
            int numberPredictions,
            @Parameter(description = " Return only assigned vehicles", required = false)
            @RequestParam(value = "onlyAssigned", required = false, defaultValue = "false")
            boolean onlyAssigned) {
        // Make sure request is valid
        validate(stdParameters);

        // Get Vehicle data from server
        Collection<IpcVehicle> vehicles;
        // Collection<IpcVehicle> vehicles_temp;
        if (!routesIdOrShortNames.isEmpty() && !routesIdOrShortNames.get(0).trim().isEmpty()) {
            vehicles = vehiclesInterface.getForRoute(routesIdOrShortNames);
        } else if (!vehicleIds.isEmpty() && !vehicleIds.get(0).trim().isEmpty()) {
            vehicles = vehiclesInterface.get(vehicleIds);
        } else {
            // vehicles_temp = inter.get();
            vehicles = vehiclesInterface.get();
            // vehicles.clear();
            // for(IpcVehicle ipcVehicle : vehicles_temp) {
            //	if(Reports.hasLastAvlJsonInHours(stdParameters.getAgencyId(), ipcVehicle.getId(),
            // 72))
            //		vehicles.add(ipcVehicle);
            // }
            // vehicles_temp.clear();
        }

        // If the vehicles doesn't exist then throw exception such that
        // Bad Request with an appropriate message is returned.
        if (vehicles == null)
            throw WebUtils.badRequestException("Invalid specifier for " + "vehicles");

        Collection<IpcVehicleConfig> vehicleConfigs = vehiclesInterface.getVehicleConfigs();

        Map<String, List<IpcVehicle>> vehiclesGrouped = vehicles.stream().collect(Collectors.groupingBy(IpcVehicle::getId));
        Map<String, List<IpcVehicleConfig>> vehiclesConfigsGrouped = vehicleConfigs.stream().collect(Collectors.groupingBy(IpcVehicleConfig::getId));

        for (String id : vehiclesGrouped.keySet()) {
            List<IpcVehicleConfig> configs = vehiclesConfigsGrouped.getOrDefault(id, List.of());
            if (!configs.isEmpty()) {
                for (IpcVehicleConfig config : configs) {
                    if (StringUtils.hasText(config.getName())) {
                        vehiclesGrouped.get(id).forEach(v -> v.setVehicleName(config.getName()));
                        break;
                    }
                }
            }
        }


        // To determine how vehicles should be drawn in UI. If stop
        // specified
        // when getting vehicle info then only the vehicles being predicted
        // for, should be highlighted. The others should be dimmed.
        Map<String, UiMode> uiTypesForVehicles = determineUiModesForVehicles(
            vehicles, stdParameters, routesIdOrShortNames, stopId, numberPredictions);

        // Convert IpcVehiclesDetails to ApiVehiclesDetails
        ApiVehiclesDetails apiVehiclesDetails =
            new ApiVehiclesDetails(vehicles, stdParameters.getAgencyId(), uiTypesForVehicles, onlyAssigned);

        // return ApiVehiclesDetails response
        return ResponseEntity.ok(apiVehiclesDetails);
    }


    /**
     * Gets information including vehicle IDs for all vehicles that have been configured. Useful for
     * creating a vehicle selector.
     *
     * @param stdParameters
     * @return
     */
    @GetMapping(value = "/command/vehicleConfigs",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Returns a list of vehilces with its configurarion.",
            description = "Returns a list of vehicles coniguration which inclides description, capacity,"
                    + " type and crushCapacity.",
            tags = {"vehicle"})
    public ResponseEntity<ApiVehicleConfigs> getVehicleConfigs(StandardParameters stdParameters) {
        // Make sure request is valid
        validate(stdParameters);

        // Get Vehicle data from server
        Collection<IpcVehicleConfig> ipcVehicleConfigs = vehiclesInterface.getVehicleConfigs();
        ApiVehicleConfigs apiVehicleConfigs = new ApiVehicleConfigs(ipcVehicleConfigs);

        // return ApiVehiclesDetails response
        return ResponseEntity.ok(apiVehicleConfigs);
    }

    /**
     * Determines Map of UiTypes for vehicles so that the vehicles can be drawn correctly in the UI.
     * If when getting vehicles no specific route and stop were specified then want to highlight all
     * vehicles. Therefore for this situation all vehicle IDs will be mapped to UiType.NORMAL.
     *
     * <p>But if route and stop were specified then the first vehicle predicted for at the specified
     * stop should be UiType.NORMAL, the subsequent ones are set to UiType.SECONDARY, and the
     * remaining vehicles are set to UiType.MINOR.
     *
     * @param vehicles
     * @param stdParameters
     * @param routesIdOrShortNames
     * @param stopId
     * @param numberPredictions
     * @return
     */
    private Map<String, UiMode> determineUiModesForVehicles(
            Collection<IpcVehicle> vehicles,
            StandardParameters stdParameters,
            List<String> routesIdOrShortNames,
            String stopId,
            int numberPredictions) {
        // Create map and initialize all vehicles to NORMAL UI mode
        Map<String, UiMode> modeMap = new HashMap<>();

        if (routesIdOrShortNames.isEmpty() || stopId == null) {
            // Stop not specified so simply return NORMAL type for all vehicles
            for (IpcVehicle ipcVehicle : vehicles) {
                modeMap.put(ipcVehicle.getId(), UiMode.NORMAL);
            }
        } else {
            // Stop specified so get predictions and set UI type accordingly
            List<String> vehiclesGeneratingPreds =
                    determineVehiclesGeneratingPreds(stdParameters, routesIdOrShortNames, stopId, numberPredictions);
            for (IpcVehicle ipcVehicle : vehicles) {
                UiMode uiType = UiMode.MINOR;
                if (!vehiclesGeneratingPreds.isEmpty() && ipcVehicle.getId().equals(vehiclesGeneratingPreds.get(0)))
                    uiType = UiMode.NORMAL;
                else if (vehiclesGeneratingPreds.contains(ipcVehicle.getId())) uiType = UiMode.SECONDARY;

                modeMap.put(ipcVehicle.getId(), uiType);
            }
        }

        // Return results
        return modeMap;
    }

    /**
     * Provides just a list of vehicle IDs of the vehicles generating predictions for the specified
     * stop. The list of vehicle IDs will be in time order such that the first one will be the next
     * predicted vehicle etc. If routeShortNames or stopId not specified then will return empty
     * array.
     *
     * @param stdParameters
     * @param routesIdOrShortNames
     * @param stopId
     * @param numberPredictions
     * @return List of vehicle IDs
     * @throws RemoteException
     */
    private List<String> determineVehiclesGeneratingPreds(
            StandardParameters stdParameters, List<String> routesIdOrShortNames, String stopId, int numberPredictions)
    {
        // The array of vehicle IDs to be returned
        List<String> vehiclesGeneratingPreds = new ArrayList<String>();

        // If stop specified then also get predictions for the stop to
        // determine which vehicles are generating the predictions.
        // If vehicle is not one of the ones generating a prediction
        // then it is labeled as a minor vehicle for the UI.
        if (!routesIdOrShortNames.isEmpty() && stopId != null) {
            List<IpcPredictionsForRouteStopDest> predictions =
                    predictionsInterface.get(routesIdOrShortNames.get(0), stopId, numberPredictions);

            // Determine set of which vehicles predictions generated for
            for (IpcPredictionsForRouteStopDest predsForRouteStop : predictions) {
                for (IpcPrediction ipcPrediction : predsForRouteStop.getPredictionsForRouteStop()) {
                    vehiclesGeneratingPreds.add(ipcPrediction.getVehicleId());
                }
            }
        }

        return vehiclesGeneratingPreds;
    }

    /**
     * Handles "predictions" command. Gets predictions from server and returns the corresponding
     * response.
     *
     * <p>A Response object is returned instead of a regular object so that can have one method for
     * the both XML and JSON yet always return the proper media type even if it is configured via
     * the query string "format" parameter as opposed to the accept header.
     *
     * @param stdParameters StdParametersBean that gets the standard parameters from the URI, query
     *     string, and headers.
     * @param routeStopStrs List of route/stops to return predictions for. If route not specified
     *     then data will be returned for all routes for the specified stop. The route specifier is
     *     the route id or the route short name. It is often best to use route short name for
     *     consistency across configuration changes (route ID is not consistent for many agencies).
     *     The stop specified can either be the stop ID or the stop code. Each route/stop is
     *     separated by the "|" character so for example the query string could have
     *     "rs=43|2029&rs=43|3029"
     * @param stopStrs List of stops to return predictions for. Provides predictions for all routes
     *     that serve the stop. Can use either stop ID or stop code. Can specify multiple stops.
     * @param numberPredictions Maximum number of predictions to return. Default value is 3.
     * @return
     */
    @GetMapping(value = "/command/predictions",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Gets predictions from server",
            tags = {"prediction"})
    public ResponseEntity getPredictions(
            StandardParameters stdParameters,
            @Parameter(
                            description = "List of route/stops to return predictions for. If route not"
                                    + " specified then data will be returned for all routes for"
                                    + " the specified stop. The route specifier is the route id"
                                    + " or the route short name. It is often best to use route"
                                    + " short name for consistency across configuration changes"
                                    + " (route ID is not consistent for many agencies). The"
                                    + " stop specified can either be the stop ID or the stop"
                                    + " code. Each route/stop is separated by the \"|\""
                                    + " character so for example the query string could have"
                                    + " \"rs=43|2029&rs=43|3029\"")
                    @RequestParam(value = "rs")
                    List<String> routeStopStrs,
            @Parameter(description = "List of stops to return predictions for. Can use either stop ID or stop code.")
            @RequestParam(value = "s") List<String> stopStrs,
            @Parameter(description = "Maximum number of predictions to return.")
            @RequestParam(value = "numPreds", defaultValue = "3") int numberPredictions) {
        // Make sure request is valid
        validate(stdParameters);

        // Create list of route/stops that should get predictions for
        List<RouteStop> routeStopsList = new ArrayList<RouteStop>();
        for (String routeStopStr : routeStopStrs) {
            // Each route/stop is specified as a single string using "\"
            // as a divider (e.g. "routeId|stopId")
            String[] routeStopParams = routeStopStr.split("\\|");

            String routeIdOrShortName;
            String stopIdOrCode;
            if (routeStopParams.length == 1) {
                // Just stop specified
                routeIdOrShortName = null;
                stopIdOrCode = routeStopParams[0];
            } else {
                // Both route and stop specified
                routeIdOrShortName = routeStopParams[0];
                stopIdOrCode = routeStopParams[1];
            }
            RouteStop routeStop = new RouteStop(routeIdOrShortName, stopIdOrCode);
            routeStopsList.add(routeStop);
        }

        // Add to list the stops that should get predictions for
        for (String stopStr : stopStrs) {
            // Use null for route identifier so get predictions for all
            // routes for the stop
            RouteStop routeStop = new RouteStop(null, stopStr);
            routeStopsList.add(routeStop);
        }

        // Actually get the predictions via IPC
        List<IpcPredictionsForRouteStopDest> predictions = predictionsInterface.get(routeStopsList, numberPredictions);

        // return ApiPredictions response
        ApiPredictions predictionsData = new ApiPredictions(predictions);
        return ResponseEntity.ok(predictionsData);
    }

    /**
     * Handles "predictionsByLoc" command. Gets predictions from server and returns the
     * corresponding response.
     *
     * <p>A Response object is returned instead of a regular object so that can have one method for
     * the both XML and JSON yet always return the proper media type even if it is configured via
     * the query string "format" parameter as opposed to the accept header.
     *
     * @param stdParameters StdParametersBean that gets the standard parameters from the URI, query
     *     string, and headers.
     * @param lat latitude in decimal degrees
     * @param lon longitude in decimal degrees
     * @param maxDistance How far away a stop can be from the lat/lon. Default is 1,500 m.
     * @param numberPredictions Maximum number of predictions to return. Default value is 3.
     * @return
     */
    @GetMapping(value = "/command/predictionsByLoc",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Gets predictions from server by location",
            tags = {"prediction"})
    public ResponseEntity<ApiPredictions> getPredictions(
            StandardParameters stdParameters,
            @Parameter(description = "Latitude of the location in decimal degrees.", required = true)
                    @RequestParam(value = "lat")
                    Double lat,
            @Parameter(description = "Longitude of the location in decimal degrees.", required = true)
                    @RequestParam(value = "lon")
                    Double lon,
            @Parameter(description = "How far away a stop can be from the location (lat/lon).", required = false)
                    @RequestParam(value = "maxDistance", defaultValue = "1500.0")
                    double maxDistance,
            @Parameter(description = "Maximum number of predictions to return.")
                    @RequestParam(value = "numPreds", defaultValue = "3")
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

        // Get predictions by location
        List<IpcPredictionsForRouteStopDest> predictions =
            predictionsInterface.get(new Location(lat, lon), maxDistance, numberPredictions);

        // return ApiPredictions response
        ApiPredictions predictionsData = new ApiPredictions(predictions);
        return ResponseEntity.ok(predictionsData);
    }

    /**
     * Handles the "routes" command. Returns summary data describing all of the routes. Useful for
     * creating a route selector as part of a UI.
     *
     * @param stdParameters
     * @return
     */
    @GetMapping(value = "/command/routes",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Gets the list of routes.",
            description = "Gets a list of the existing routes in the server. It might be filtered"
                    + " according to routeId or routeShortName. If more than one route have the"
                    + " same shortName, it is possible to specify keepDuplicates parameter to"
                    + " show all of them. ",
            tags = {"base data", "route"})
    public ResponseEntity<ApiRoutes> getRoutes(
            StandardParameters stdParameters,
            @Parameter(description = "List of routeId or routeShortName. Example: r=1&r=2", required = false)
            @RequestParam(value = "r", required = false)
            List<String> routeIdsOrShortNames,
            @Parameter(description = "Return all routes when more than one have the same shortName.", required = false)
            @RequestParam(value = "keepDuplicates", required = false)
            Boolean keepDuplicates) {

        // Make sure request is valid
        validate(stdParameters);

        // Get agency info so can also return agency name
        List<Agency> agencies = configInterface.getAgencies();

        // Get route data from server
        ApiRoutes routesData;
        if (routeIdsOrShortNames == null || routeIdsOrShortNames.isEmpty()) {
            // Get all routes
            List<IpcRouteSummary> routes = new ArrayList<IpcRouteSummary>(configInterface.getRoutes());

            // Handle duplicates. If should keep duplicates (where couple
            // of routes have the same route_short_name) then modify
            // the route name to indicate the different IDs. If should
            // ignore duplicates then don't include them in final list
            Collection<IpcRouteSummary> processedRoutes = new ArrayList<IpcRouteSummary>();
            for (int i = 0; i < routes.size() - 1; ++i) {
                IpcRouteSummary route = routes.get(i);
                IpcRouteSummary nextRoute = routes.get(i + 1);

                // If find a duplicate route_short_name...
                if (route.getShortName().equals(nextRoute.getShortName())) {
                    // Only keep route if supposed to
                    if (keepDuplicates != null && keepDuplicates) {
                        // Keep duplicates but change route name
                        IpcRouteSummary routeWithModifiedName =
                            new IpcRouteSummary(route, route.getName() + " (ID=" + route.getId() + ")");
                        processedRoutes.add(routeWithModifiedName);

                        IpcRouteSummary nextRouteWithModifiedName = new IpcRouteSummary(
                            nextRoute, nextRoute.getName() + " (ID=" + nextRoute.getId() + ")");
                        processedRoutes.add(nextRouteWithModifiedName);

                        // Since processed both this route and the next
                        // route can skip to next one
                        ++i;
                    }
                } else {
                    // Not a duplicate so simply add it
                    processedRoutes.add(route);
                }
            }
            // Add the last route
            processedRoutes.add(routes.get(routes.size() - 1));

            routesData = new ApiRoutes(processedRoutes, agencies.get(0));
        } else {
            // Get specified routes
            List<IpcRoute> ipcRoutes = configInterface.getRoutes(routeIdsOrShortNames);
            routesData = new ApiRoutes(ipcRoutes, agencies.get(0));
        }

        // Create and return response
        return ResponseEntity.ok(routesData);
    }

    /**
     * Handles the "routesDetails" command. Provides detailed information for a route includes all
     * stops and paths such that it can be drawn in a map.
     *
     * @param stdParameters
     * @param routeIdsOrShortNames list of route IDs or route short names. If a single route is
     *     specified then data for just the single route is returned. If no route is specified then
     *     data for all routes returned in an array. If multiple routes specified then data for
     *     those routes returned in an array.
     * @param stopId optional. If set then only this stop and the remaining ones on the trip pattern
     *     are marked as being for the UI and can be highlighted. Useful for when want to emphasize
     *     in the UI only the stops that are of interest to the user.
     * @param direction optional. If set then only the shape for specified direction is marked as
     *     being for the UI. Needed for situations where a single stop is used for both directions
     *     of a route and want to highlight in the UI only the stops and the shapes that the user is
     *     actually interested in.
     * @param tripPatternId optional. If set then only the specified trip pattern is marked as being
     *     for the UI.
     * @return
     * @
     */
    @GetMapping(value = "/command/routesDetails",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Provides detailed information for a route.",
            description = "Provides detailed information for a route includes all stops "
                    + "and paths such that it can be drawn in a map.",
            tags = {"base data", "route"})
    public ResponseEntity<ApiRoutesDetails> getRouteDetails(
            StandardParameters stdParameters,
            @Parameter(description = "List of routeId or routeShortName. Example: r=1&r=2", required = false)
            @RequestParam(value = "r", required = false)
                    List<String> routeIdsOrShortNames,
            @Parameter(
                            description = "If set then only the shape for specified direction is marked"
                                    + " as being for the UI.",
                            required = false)
            @RequestParam(value = "d", required = false)
                    String directionId,
            @Parameter(
                            description = "If set then only this stop and the remaining ones on the trip"
                                    + " pattern are marked as being for the UI and can be"
                                    + " highlighted. Useful for when want to emphasize in the"
                                    + " UI only  the stops that are of interest to the user.",
                            required = false)
                    @RequestParam(value = "s", required = false)
                    String stopId,
            @Parameter(
                            description =
                                    "If set then only the specified trip pattern is marked as being" + " for the UI",
                            required = false)
                    @RequestParam(value = "tripPattern", required = false)
                    String tripPatternId) {
        // Make sure request is valid
        validate(stdParameters);

        // Get agency info so can also return agency name
        List<Agency> agencies = configInterface.getAgencies();

        List<IpcRoute> ipcRoutes;

        // If single route specified
        if (routeIdsOrShortNames != null && routeIdsOrShortNames.size() == 1) {
            String routeIdOrShortName = routeIdsOrShortNames.get(0);
            IpcRoute route = configInterface.getRoute(routeIdOrShortName, directionId, stopId, tripPatternId);

            // If the route doesn't exist then throw exception such that
            // Bad Request with an appropriate message is returned.
            if (route == null)
                throw WebUtils.badRequestException("Route for route=" + routeIdOrShortName + " does not exist.");

            ipcRoutes = new ArrayList<IpcRoute>();
            ipcRoutes.add(route);
        } else {
            // Multiple routes specified
            ipcRoutes = configInterface.getRoutes(routeIdsOrShortNames);
        }

        // Take the IpcRoute data array and create and return
        // ApiRoutesDetails object
        ApiRoutesDetails routeData = new ApiRoutesDetails(ipcRoutes, agencies.get(0));
        return stdParameters.createResponse(routeData);

    }

    /**
     * Handles the "stops" command. Returns all stops associated with a route, grouped by direction.
     * Useful for creating a UI where user needs to select a stop from a list.
     *
     * @param stdParameters
     * @param routeShortName
     * @return
     * @
     */
    @GetMapping(value = "/command/stops",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Retrives bus stops from the server.",
            description = "Returns all stops associated with a route, grouped by direction. Useful for"
                    + " creating a UI where user needs to select a stop from a list.",
            tags = {"base data", "stop"})
    public ResponseEntity<ApiDirections> getStops(
            StandardParameters stdParameters,
            @Parameter(
                            description = "if set, retrives only busstops belongind to the route. "
                                    + "It might be routeId or route shrot name.",
                            required = false)
                    @RequestParam(value = "r", required = false)
                    String routesIdOrShortNames) {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get stops data from server
            IpcDirectionsForRoute stopsForRoute = configInterface.getStops(dbConfig, routesIdOrShortNames);

            // If the route doesn't exist then throw exception such that
            // Bad Request with an appropriate message is returned.
            if (stopsForRoute == null)
                throw WebUtils.badRequestException("route=" + routesIdOrShortNames + " does not exist.");

            // Create and return ApiDirections response
            ApiDirections directionsData = new ApiDirections(stopsForRoute);
            return stdParameters.createResponse(directionsData);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    /**
     * Handles the "block" command which outputs configuration data for the specified block ID and
     * service ID. Includes all sub-data such as trips and trip patterns.
     *
     * @param stdParameters
     * @param blockId
     * @param serviceId
     * @return
     * @
     */
    @GetMapping(value = "/command/block",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Retrives configuration data for the specified block ID and service ID",
            description = "Retrives configuration data for the specified block ID and service ID. "
                    + "Includes all sub-data such as trips ans trip patterns."
                    + "Every trip is associated with a block.",
            tags = {"base data", "trip", "block"})
    public ResponseEntity<ApiBlock> getBlock(
            StandardParameters stdParameters,
            @Parameter(description = "Block id to be asked.", required = true)
            @RequestParam(value = "blockId")
                    String blockId,
            @Parameter(description = "Service id to be asked.", required = true)
            @RequestParam(value = "serviceId")
                    String serviceId) {

        // Make sure request is valid
        validate(stdParameters);
        if (serviceId == null) throw WebUtils.badRequestException("Must specify serviceId");

        try {
            // Get block data from server
            IpcBlock ipcBlock = configInterface.getBlock(blockId, serviceId);

            // If the block doesn't exist then throw exception such that
            // Bad Request with an appropriate message is returned.
            if (ipcBlock == null)
                throw WebUtils.badRequestException(
                        "The blockId=" + blockId + " for serviceId=" + serviceId + " does not exist.");

            // Create and return ApiBlock response
            ApiBlock apiBlock = new ApiBlock(ipcBlock);
            return stdParameters.createResponse(apiBlock);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    /**
     * Handles the "blocksTerse" command which outputs configuration data for the specified block
     * ID. Does not include trip pattern and schedule data for trips.
     *
     * @param stdParameters
     * @param blockId
     * @return
     * @
     */
    @GetMapping(value = "/command/blocksTerse",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Retrives configuration data for the specified block ID.",
            description = "Retrives configuration data for the specified block ID. It does not include"
                    + " trip patterns.Every trip is associated with a block.",
            tags = {"base data", "trip", "block"})
    public ResponseEntity<ApiBlocksTerse> getBlocksTerse(
            StandardParameters stdParameters,
            @Parameter(description = "Block id to be asked.", required = true) @RequestParam(value = "blockId")
                    String blockId)
             {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get block data from server
            Collection<IpcBlock> ipcBlocks = configInterface.getBlocks(blockId);

            // If the block doesn't exist then throw exception such that
            // Bad Request with an appropriate message is returned.
            if (ipcBlocks.isEmpty()) throw WebUtils.badRequestException("The blockId=" + blockId + " does not exist.");

            // Create and return ApiBlock response
            ApiBlocksTerse apiBlocks = new ApiBlocksTerse(ipcBlocks);
            return stdParameters.createResponse(apiBlocks);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    /**
     * Handles the "blocks" command which outputs configuration data for the specified block ID.
     * Includes all sub-data such as trips and trip patterns.
     *
     * @param stdParameters
     * @param blockId
     * @return
     * @
     */
    @GetMapping(value = "/command/blocks",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Retrives configuration data for the specified block ID",
            description = "Retrives configuration data for the specified block ID. Includes all sub-data"
                    + " such as trips and trip.Every trip is associated with a block.",
            tags = {"base data", "trip", "block"})
    public ResponseEntity<ApiBlocks> getBlocks(
            StandardParameters stdParameters,
            @Parameter(description = "Block id to be asked.", required = true) @RequestParam(value = "blockId")
                    String blockId)
             {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get block data from server
            Collection<IpcBlock> ipcBlocks = configInterface.getBlocks(blockId);

            // If the block doesn't exist then throw exception such that
            // Bad Request with an appropriate message is returned.
            if (ipcBlocks.isEmpty()) throw WebUtils.badRequestException("The blockId=" + blockId + " does not exist.");

            // Create and return ApiBlock response
            ApiBlocks apiBlocks = new ApiBlocks(ipcBlocks);
            return stdParameters.createResponse(apiBlocks);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    /**
     * Handles the "blockIds" command. Returns list of block IDs.
     *
     * @param stdParameters
     * @return
     * @
     */
    @GetMapping(value = "/command/blockIds",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Retrives a list of all blockId for the specified service ID",
            description = "Retrives a list of all blockId for the specified service ID."
                    + "Every trip is associated with a block.",
            tags = {"base data", "trip", "block"})
    public ResponseEntity<ApiIds> getBlockIds(
            StandardParameters stdParameters,
            @Parameter(description = "if set, returns only the data for that serviceId.", required = false)
                    @RequestParam(value = "serviceId")
                    String serviceId)
             {
        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get Vehicle data from server
            List<String> ids = configInterface.getBlockIds(serviceId);

            ApiIds apiIds = new ApiIds(ids);
            return stdParameters.createResponse(apiIds);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    /**
     * Gets which blocks are active. Can optionally specify list of routes and how much before a
     * block is supposed to start is it considered active.
     *
     * @param stdParameters StdParametersBean that gets the standard parameters from the URI, query
     *     string, and headers.
     * @param routesIdOrShortNames Optional parameter for specifying which routes want data for.
     * @param allowableBeforeTimeSecs Optional parameter. A block will be active if the time is
     *     between the block start time minus allowableBeforeTimeSecs and the block end time.
     *     Default value for allowableBeforeTimeSecs is 0.
     * @return
     * @
     */
    @GetMapping(value = "/command/activeBlocks",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    // @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Gets which blocks are active",
            description = "Retrives a list of active blocks. Optionally can be filered accorditn to"
                    + " routesIdOrShortNames params.Every trip is associated with a block.",
            tags = {"prediction", "trip", "block"})
    public ResponseEntity<ApiActiveBlocks> getActiveBlocks(
            StandardParameters stdParameters,
            @Parameter(
                            description = "if set, retrives only active blocks belongind to the route. "
                                    + "It might be routeId or route shrot name.",
                            required = false)
                    @RequestParam(value = "r", required = false)
                    List<String> routesIdOrShortNames,
            @Parameter(
                            description = "A block will be active if the time is between the block start"
                                    + " time minus allowableBeforeTimeSecs and the block end"
                                    + " time")
                    @RequestParam(value = "t", defaultValue = "0")
                    int allowableBeforeTimeSecs)
             {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get active block data from server

            Collection<IpcActiveBlock> activeBlocks =
                    vehiclesInterface.getActiveBlocks(routesIdOrShortNames, allowableBeforeTimeSecs);

            // Create and return ApiBlock response
            ApiActiveBlocks apiActiveBlocks = new ApiActiveBlocks(activeBlocks, stdParameters.getAgencyId());
            return stdParameters.createResponse(apiActiveBlocks);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    @GetMapping(value = "/command/activeBlocksByRoute",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Gets which blocks are active by route",
            description = "Retrives a list routes with its  active blocks. Optionally can be filered"
                    + " according to routesIdOrShortNames params.Every trip is associated with"
                    + " a block.",
            tags = {"prediction", "trip", "block", "route"})
    public ResponseEntity<ApiActiveBlocksRoutes> getActiveBlocksByRoute(
            StandardParameters stdParameters,
            @Parameter(
                            description = "if set, retrives only active blocks belongind to the route. "
                                    + "It might be routeId or route shrot name.",
                            required = false)
                    @RequestParam(value = "r", required = false)
                    List<String> routesIdOrShortNames,
            @Parameter(
                            description = "A block will be active if the time is between the block start"
                                    + " time minus allowableBeforeTimeSecs and the block end"
                                    + " time")
                    @RequestParam(value = "t", defaultValue = "0", required = false)
                    int allowableBeforeTimeSecs) {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get active block data from server

            Collection<IpcActiveBlock> activeBlocks =
                vehiclesInterface.getActiveBlocks(routesIdOrShortNames, allowableBeforeTimeSecs);

            // Create and return ApiBlock response
            ApiActiveBlocksRoutes apiActiveBlocksRoutes =
                new ApiActiveBlocksRoutes(activeBlocks, stdParameters.getAgencyId());
            return stdParameters.createResponse(apiActiveBlocksRoutes);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }
    @GetMapping(value = "/command/activeBlocksByRouteWithoutVehicles",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Gets which blocks are active by route.",
            description = "Retrives a list routes with its  active blocks, without the vechicles."
                    + " Optionally can be filered accorditn to routesIdOrShortNames"
                    + " params.Every trip is associated with a block.",
            tags = {"prediction", "trip", "block", "route"})
    public ResponseEntity<ApiActiveBlocksRoutes> getActiveBlocksByRouteWithoutVehicles(
            StandardParameters stdParameters,
            @Parameter(
                            description = "if set, retrives only active blocks belongind to the route. "
                                    + "It might be routeId or route shrot name.",
                            required = false)
                    @RequestParam(value = "r", required = false)
                    List<String> routesIdOrShortNames,
            @Parameter(
                            description = "A block will be active if the time is between the block start"
                                    + " time minus allowableBeforeTimeSecs and the block end"
                                    + " time")
                    @RequestParam(value = "t", defaultValue = "0")
                    int allowableBeforeTimeSecs)
             {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get active block data from server
            Collection<IpcActiveBlock> activeBlocks =
                    vehiclesInterface.getActiveBlocksWithoutVehicles(routesIdOrShortNames, allowableBeforeTimeSecs);

            // Create and return ApiBlock response
            ApiActiveBlocksRoutes apiActiveBlocksRoutes =
                    new ApiActiveBlocksRoutes(activeBlocks, stdParameters.getAgencyId());
            return stdParameters.createResponse(apiActiveBlocksRoutes);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    @GetMapping(value = "/command/activeBlockByRouteWithVehicles",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Gets which blocks are active by route.",
            description = "Retrives a list routes with its  active blocks, including the vechicles. "
                    + "Optionally can be filered accorditn to routesIdOrShortNames params."
                    + "Every trip is associated with a block.",
            tags = {"prediction", "trip", "block", "route", "vehicle"})
    public ResponseEntity<ApiActiveBlocksRoutes> getActiveBlockByRouteWithVehicles(
            StandardParameters stdParameters,
            @Parameter(
                            description = "if set, retrives only active blocks belongind to the route. It"
                                    + " might be routeId or route shrot name.",
                            required = false)
                    @RequestParam(value = "r", required = false)
                    String routesIdOrShortName,
            @Parameter(
                            description = "A block will be active if the time is between the block start"
                                    + " time minus allowableBeforeTimeSecs and the block end"
                                    + " time")
                    @RequestParam(value = "t", defaultValue = "0")
                    int allowableBeforeTimeSecs)
             {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get active block data from server
            Collection<IpcActiveBlock> activeBlocks =
                    vehiclesInterface.getActiveBlocksAndVehiclesByRouteId(routesIdOrShortName, allowableBeforeTimeSecs);

            // Create and return ApiBlock response
            ApiActiveBlocksRoutes apiActiveBlocksRoutes =
                    new ApiActiveBlocksRoutes(activeBlocks, stdParameters.getAgencyId());
            return stdParameters.createResponse(apiActiveBlocksRoutes);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    @GetMapping(value = "/command/activeBlockByRouteNameWithVehicles",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Gets which blocks are active by routeName.",
            description = "Retrives a list routes with its  active blocks, including the vechicles. "
                    + "Optionally can be filered accorditn to routesIdOrShortNames params."
                    + "Every trip is associated with a block.",
            tags = {"prediction", "trip", "block", "route", "vehicle"})
    public ResponseEntity<ApiActiveBlocksRoutes> getActiveBlockByRouteNameWithVehicles(
            StandardParameters stdParameters,
            @Parameter(
                            description =
                                    "if set, retrives only active blocks belongind to the route" + " name specified.",
                            required = false)
                    @RequestParam(value = "r", required = false)
                    String routeName,
            @Parameter(
                            description = "A block will be active if the time is between the block start"
                                    + " time minus allowableBeforeTimeSecs and the block end"
                                    + " time")
                    @RequestParam(value = "t", defaultValue = "0")
                    int allowableBeforeTimeSecs)
             {
        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get active block data from server
            Collection<IpcActiveBlock> activeBlocks =
                    vehiclesInterface.getActiveBlocksAndVehiclesByRouteName(routeName, allowableBeforeTimeSecs);

            for (IpcActiveBlock ipcActiveBlocks : activeBlocks) {
                for (IpcVehicle ipcVehicle : ipcActiveBlocks.getVehicles()) {
                    for (IpcVehicleConfig iVC : vehiclesInterface.getVehicleConfigs()) {

                        if (iVC.getId().equals(ipcVehicle.getId()) && StringUtils.hasText(iVC.getName())) {
                            ipcVehicle.setVehicleName(iVC.getName());

                            break;
                        }
                    }
                }
            }

            // Create and return ApiBlock response
            ApiActiveBlocksRoutes apiActiveBlocksRoutes =
                    new ApiActiveBlocksRoutes(activeBlocks, stdParameters.getAgencyId());

            return stdParameters.createResponse(apiActiveBlocksRoutes);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    @GetMapping(value = "/command/vehicleAdherenceSummary",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Returns the counters  for the number of current vehicles running early, late" + " or on time. ",
            description = "Returns the amount of vehicles running  early, late or on time. Besides"
                    + " specify the amount of vehicles no predictables and the amount of active"
                    + " blocks.",
            tags = {"prediction"})
    public ResponseEntity<ApiAdherenceSummary> getVehicleAdherenceSummary(
            StandardParameters stdParameters,
            @Parameter(
                            description = "The number of seconds early a vehicle has to be before it is"
                                    + " considered in the early counter.",
                            required = false)
                    @RequestParam(value = "allowableEarlySec", defaultValue = "0")
                    int allowableEarlySec,
            @Parameter(
                            description = "The number of seconds early a vehicle has to be before it is"
                                    + " considered in the late counter.",
                            required = false)
                    @RequestParam(value = "allowableLateSec", defaultValue = "0")
                    int allowableLateSec,
            @Parameter(
                            description = "A block will be active if the time is between the block start"
                                    + " time minus allowableBeforeTimeSecs (t) and the block"
                                    + " end time")
                    @RequestParam(value = "t", defaultValue = "0")
                    int allowableBeforeTimeSecs)
             {

        // Make sure request is valid
        validate(stdParameters);

        try {

            int late = 0, ontime = 0, early = 0, nodata = 0, blocks = 0;

            Collection<IpcVehicle> ipcVehicles = vehiclesInterface.getVehiclesForBlocks();

            for (IpcVehicle v : ipcVehicles) {
                TemporalDifference adh = v.getRealTimeSchedAdh();

                if (adh == null) nodata++;
                else if (adh.isEarlierThan(allowableEarlySec)) early++;
                else if (adh.isLaterThan(allowableLateSec)) late++;
                else ontime++;
            }

            blocks = vehiclesInterface.getNumActiveBlocks(null, allowableBeforeTimeSecs);

            ApiAdherenceSummary resp = new ApiAdherenceSummary(late, ontime, early, nodata, blocks);

            return stdParameters.createResponse(resp);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    /**
     * Handles the "trip" command which outputs configuration data for the specified trip. Includes
     * all sub-data such as trip patterns.
     *
     * @param stdParameters
     * @param tripId Can be the GTFS trip_id or the trip_short_name
     * @return
     * @
     */
    @GetMapping(value = "/command/trip",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Gets the configuration data of a trip.",
            description = "Gets the configuration data of a trip",
            tags = {"base data", "trip"})
    public ResponseEntity<ApiTrip> getTrip(
            StandardParameters stdParameters,
            @Parameter(description = "Trip id", required = true) @RequestParam(value = "tripId") String tripId)
             {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get block data from server
            IpcTrip ipcTrip = configInterface.getTrip(tripId);

            // If the trip doesn't exist then throw exception such that
            // Bad Request with an appropriate message is returned.
            if (ipcTrip == null) throw WebUtils.badRequestException("TripId=" + tripId + " does not exist.");

            // Create and return ApiBlock response.
            // Include stop path info since just outputting single trip.
            ApiTrip apiTrip = new ApiTrip(ipcTrip, true);
            return stdParameters.createResponse(apiTrip);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    /**
     * Handles the "tripWithTravelTimes" command which outputs configuration data for the specified
     * trip. Includes all sub-data such as trip patterns.
     *
     * @param stdParameters
     * @param tripId
     * @return
     * @
     */
    @GetMapping(value = "/command/tripWithTravelTimes",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Gets the configuration data of a trip.",
            description = "Gets the configuration data of a trip. Includes all sub-data such as trip" + " patterns",
            tags = {"base data", "trip"})
    public ResponseEntity<ApiTripWithTravelTimes> getTripWithTravelTimes(
            StandardParameters stdParameters,
            @Parameter(description = "Trip id", required = true) @RequestParam(value = "tripId") String tripId)
             {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get block data from server
            IpcTrip ipcTrip = configInterface.getTrip(tripId);

            // If the trip doesn't exist then throw exception such that
            // Bad Request with an appropriate message is returned.
            if (ipcTrip == null) throw WebUtils.badRequestException("TripId=" + tripId + " does not exist.");

            // Create and return ApiBlock response.
            // Include stop path info since just outputting single trip.
            ApiTripWithTravelTimes apiTrip = new ApiTripWithTravelTimes(ipcTrip, true);
            return stdParameters.createResponse(apiTrip);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    /**
     * Handles the "tripsWithTravelTimes" command which outputs arrival and departures data for the
     * specified trip by date.
     *
     * @param stdParameters
     * @param date
     * @return
     * @
     */
    @GetMapping(value = "/reports/tripsWithTravelTimes",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Gets the arrivals and departures data of a trips.",
            description = "Gets the arrivals and departures data of a trips.",
            tags = {"base data", "trips"})
    public ResponseEntity<String> getTripsWithTravelTimes(
            StandardParameters stdParameters,
            @Parameter(description = "Begin date(YYYY-MM-DD).") @RequestParam(value = "date") String date)
             {

        // Make sure request is valid
        validate(stdParameters);

        try {

            String response = Reports.getTripsWithTravelTimes(stdParameters.getAgencyId(), date);
            return stdParameters.createResponse(response);
        } catch (Exception e) {
            throw WebUtils.badRequestException(e);
        }
    }

    /**
     * Handles the tripIds command. Returns list of trip IDs.
     *
     * @param stdParameters
     * @return
     * @
     */
    @GetMapping(value = "/command/tripIds",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Retrives a list of all tripIds",
            description = "Retrives a list of all tripIds.",
            tags = {"base data", "trip"})
    public ResponseEntity<ApiIds> getTripIds(StandardParameters stdParameters)  {
        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get Vehicle data from server
            List<String> ids = configInterface.getTripIds();

            ApiIds apiIds = new ApiIds(ids);
            return stdParameters.createResponse(apiIds);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    /**
     * Handles the "tripPattern" command which outputs trip pattern configuration data for the
     * specified route.
     *
     * @param stdParameters
     * @param routesIdOrShortNames
     * @return
     * @
     */
    @GetMapping(value = "/command/tripPatterns",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Retrives a list of all trip patters.",
            description = "Retrives a list of all trip patters for the specific routeId or" + " routeShortName.",
            tags = {"base data", "trip"})
    public ResponseEntity<ApiTripPatterns> getTripPatterns(
            StandardParameters stdParameters,
            @Parameter(description = "Specifies the routeId or routeShortName.", required = true)
                    @RequestParam(value = "r")
                    String routesIdOrShortNames)
             {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get block data from server
            List<IpcTripPattern> ipcTripPatterns = configInterface.getTripPatterns(routesIdOrShortNames);

            // If the trip doesn't exist then throw exception such that
            // Bad Request with an appropriate message is returned.
            if (ipcTripPatterns == null)
                throw WebUtils.badRequestException("route=" + routesIdOrShortNames + " does not exist.");

            // Create and return ApiTripPatterns response
            ApiTripPatterns apiTripPatterns = new ApiTripPatterns(ipcTripPatterns);
            return stdParameters.createResponse(apiTripPatterns);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    /**
     * Handles the "scheduleVertStops" command which outputs schedule for the specified route. The
     * data is output such that the stops are listed vertically (and the trips are horizontal). For
     * when there are a good number of stops but not as many trips, such as for commuter rail.
     *
     * @param stdParameters
     * @param routesIdOrShortNames
     * @return
     * @
     */
    @GetMapping(value = "/command/scheduleVertStops",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Retrives schedule for the specified route.",
            description = "Retrives schedule for the specified route.  The data is output such that the"
                    + " stops are listed vertically (and the trips are horizontal). For when"
                    + " there are a good number of stops but not as many trips, such as for"
                    + " commuter rail.",
            tags = {"base data", "schedule"})
    public ResponseEntity<ApiSchedulesVertStops> getScheduleVertStops(
            StandardParameters stdParameters,
            @Parameter(description = "Specifies the routeId or routeShortName.", required = true)
                    @RequestParam(value = "r")
                    String routesIdOrShortNames)
             {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get block data from server
            List<IpcSchedule> ipcSchedules = configInterface.getSchedules(routesIdOrShortNames);

            // If the trip doesn't exist then throw exception such that
            // Bad Request with an appropriate message is returned.
            if (ipcSchedules == null)
                throw WebUtils.badRequestException("route=" + routesIdOrShortNames + " does not exist.");

            // Create and return ApiSchedules response
            ApiSchedulesVertStops apiSchedules = new ApiSchedulesVertStops(ipcSchedules);
            return stdParameters.createResponse(apiSchedules);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    /**
     * Handles the "scheduleHorizStops" command which outputs schedule for the specified route. The
     * data is output such that the stops are listed horizontally (and the trips are vertical). For
     * when there are many more trips than stops, which is typical for bus routes.
     *
     * @param stdParameters
     * @param routesIdOrShortNames
     * @return
     * @
     */
    @GetMapping(value = "/command/scheduleHorizStops",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Retrives schedule for the specified route.",
            description = "Retrives schedule for the specified route.  The data is output such that the"
                    + " stops are listed horizontally (and the trips are vertical). For when"
                    + " there are a good number of stops but not as many trips, such as for"
                    + " commuter rail.",
            tags = {"base data", "schedule"})
    public ResponseEntity<ApiSchedulesHorizStops> getScheduleHorizStops(
            StandardParameters stdParameters,
            @Parameter(description = "Specifies the routeId or routeShortName.", required = true)
                    @RequestParam(value = "r")
                    String routesIdOrShortNames)
             {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get block data from server
            List<IpcSchedule> ipcSchedules = configInterface.getSchedules(routesIdOrShortNames);

            // If the trip doesn't exist then throw exception such that
            // Bad Request with an appropriate message is returned.
            if (ipcSchedules == null)
                throw WebUtils.badRequestException("route=" + routesIdOrShortNames + " does not exist.");

            // Create and return ApiSchedules response
            ApiSchedulesHorizStops apiSchedules = new ApiSchedulesHorizStops(ipcSchedules);
            return stdParameters.createResponse(apiSchedules);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    /**
     * For getting Agency data for a specific agencyId.
     *
     * @param stdParameters
     * @return
     * @
     */
    @GetMapping(value = "/command/agencyGroup",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Retrives agency infomation.",
            description = "Retrives agency infomation, including extent.",
            tags = {"base data", "agency"})
    public ResponseEntity<ApiAgencies> getAgencyGroup(StandardParameters stdParameters)  {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get block data from server
            List<Agency> agencies = configInterface.getAgencies();

            // Create and return ApiAgencies response
            List<ApiAgency> apiAgencyList = new ArrayList<ApiAgency>();
            for (Agency agency : agencies) {
                apiAgencyList.add(new ApiAgency(stdParameters.getAgencyId(), agency));
            }
            ApiAgencies apiAgencies = new ApiAgencies(apiAgencyList);
            return stdParameters.createResponse(apiAgencies);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    /**
     * For getting calendars that are currently active.
     *
     * @param stdParameters
     * @return
     * @
     */
    @GetMapping(value = "/command/currentCalendars",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Retrives current calendar infomation.",
            description = "Retrives current calendar infomation. Only teh calendar that applies to" + " current day",
            tags = {"base data", "calendar", "serviceId"})
    public ResponseEntity<ApiCalendars> getCurrentCalendars(StandardParameters stdParameters)  {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get block data from server
            List<IpcCalendar> ipcCalendars = configInterface.getCurrentCalendars();

            // Create and return ApiAgencies response
            ApiCalendars apiCalendars = new ApiCalendars(ipcCalendars);
            return stdParameters.createResponse(apiCalendars);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    /**
     * For getting all calendars.
     *
     * @param stdParameters
     * @return
     * @
     */
    @GetMapping(value = "/command/allCalendars",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Retrives all calendar infomation.",
            description = "Retrives all calendar infomation.",
            tags = {"base data", "calendar", "serviceId"})
    public ResponseEntity<ApiCalendars> getAllCalendars(StandardParameters stdParameters)  {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get block data from server
            List<IpcCalendar> ipcCalendars = configInterface.getAllCalendars();

            // Create and return ApiAgencies response
            ApiCalendars apiCalendars = new ApiCalendars(ipcCalendars);
            return stdParameters.createResponse(apiCalendars);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    /**
     * Handles the "serviceIds" command. Returns list of all service IDs.
     *
     * @param stdParameters
     * @return
     * @
     */
    @GetMapping(value = "/command/serviceIds",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Retrives all service id.",
            description = "Retrives all service id.",
            tags = {"base data", "serviceId"})
    public ResponseEntity<ApiIds> getServiceIds(StandardParameters stdParameters)  {
        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get Vehicle data from server
            List<String> ids = configInterface.getServiceIds();

            ApiIds apiIds = new ApiIds(ids);
            return stdParameters.createResponse(apiIds);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    /**
     * Handles the currentServiceIds command. Returns list of service IDs that are currently active.
     *
     * @param stdParameters
     * @return
     * @
     */
    @GetMapping(value = "/command/currentServiceIds",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Retrives current service id.",
            description = "Retrives current service id.",
            tags = {"base data", "serviceId"})
    public ResponseEntity<ApiIds> getCurrentServiceIds(StandardParameters stdParameters)  {
        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get Vehicle data from server
            List<String> ids = configInterface.getCurrentServiceIds();

            ApiIds apiIds = new ApiIds(ids);
            return stdParameters.createResponse(apiIds);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    /**
     * Returns status about the specified agency server. Currently provides info on the DbLogger
     * queue.
     *
     * @param stdParameters
     * @return
     * @
     */
    @GetMapping(value = "/command/serverStatus",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Retrives server status information.",
            description = "Retrives server status information.",
            tags = {"server status"})
    public ResponseEntity<ApiServerStatus> getServerStatus(StandardParameters stdParameters)  {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get status information from server
            IpcServerStatus ipcServerStatus = serverStatusInterface.get();

            // Create and return ApiServerStatus response
            ApiServerStatus apiServerStatus = new ApiServerStatus(stdParameters.getAgencyId(), ipcServerStatus);
            return stdParameters.createResponse(apiServerStatus);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    /**
     * Returns info for this particular web server for each agency on how many outstanding RMI calls
     * there are.
     *
     * @param stdParameters
     * @return
     * @
     */
    @GetMapping(value = "/command/rmiStatus",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Retrives RMI status information.",
            description = "Retrives RMI server status information.",
            tags = {"server status"})
    public ResponseEntity<ApiRmiServerStatus> getRmiStatus(StandardParameters stdParameters)  {

        // Make sure request is valid
        validate(stdParameters);

        ApiRmiServerStatus apiRmiServerStatus = new ApiRmiServerStatus();
        return stdParameters.createResponse(apiRmiServerStatus);
    }

    @GetMapping(value = "/command/currentServerTime",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Retrives server time.",
            description = "Retrives server time",
            tags = {"server status"})
    public ResponseEntity<ApiCurrentServerDate> getCurrentServerTime(StandardParameters stdParameters) {
        // Make sure request is valid
        validate(stdParameters);
        Date currentTime = serverStatusInterface.getCurrentServerTime();

        return stdParameters.createResponse(new ApiCurrentServerDate(currentTime));
    }

    @Operation(summary = "Returns exports list", description = "Returns exports list")
    @GetMapping(value = "/command/exports",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity<ApiExportsData> getExports(StandardParameters stdParameters)  {
        validate(stdParameters);
        ApiExportsData result = null;
        Session session = HibernateUtils.getSession();
        try {
            result = new ApiExportsData(ExportTable.getExportTable(session));

            session.close();
            return stdParameters.createResponse(result);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            session.close();
            throw WebUtils.badRequestException(e);
        }
    }

    @Operation(summary = "Return export file", description = "Return export file")
    @GetMapping(value = "/command/getExportFile",
        produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE })
    public ResponseEntity<Object> getExportById(
            StandardParameters stdParameters,
            @Parameter(description = "Id eksportu") @RequestParam(value = "id") long id) {
        validate(stdParameters);
        Session session = HibernateUtils.getSession();
        try {
            ExportTable result = ExportTable.getExportFile(session, id).get(0);

            session.close();
            // return ApiVehicles response
            // return stdParameters.createResponse(result);
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + result.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(result.getFile());
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            session.close();
            throw WebUtils.badRequestException(e);
        }
    }
    // /**
    // * For creating response of list of vehicles. Would like to make this a
    // * generic type but due to type erasure cannot do so since GenericEntity
    // * somehow works differently with generic types.
    // * <p>
    // * Deprecated because found that much better off using a special
    // * container class for lists of items since that way can control the
    // * name of the list element.
    // *
    // * @param collection
    // * Collection of Vehicle objects to be returned in XML or JSON.
    // * Must be ArrayList so can use GenericEntity to create Response.
    // * @param stdParameters
    // * For specifying media type.
    // * @return The created response in the proper media type.
    // */
    // private static Response createListResponse(Collection<ApiVehicle>
    // collection,
    // StdParametersBean stdParameters) {
    // // Must be ArrayList so can use GenericEntity to create Response.
    // ArrayList<ApiVehicle> arrayList = (ArrayList<ApiVehicle>) collection;
    //
    // // Create a GenericEntity that can handle list of the appropriate
    // // type.
    // GenericEntity<List<ApiVehicle>> entity =
    // new GenericEntity<List<ApiVehicle>>(arrayList) {};
    //
    // // Return the response using the generic entity
    // return createResponse(entity, stdParameters);
    // }

}
