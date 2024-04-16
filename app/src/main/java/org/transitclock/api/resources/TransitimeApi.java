package org.transitclock.api.resources;

import java.util.List;

import org.transitclock.api.data.ApiActiveBlocksResponse;
import org.transitclock.api.data.ApiActiveBlocksRoutesResponse;
import org.transitclock.api.data.ApiAdherenceSummary;
import org.transitclock.api.data.ApiAgenciesResponse;
import org.transitclock.api.data.ApiBlock;
import org.transitclock.api.data.ApiBlocksResponse;
import org.transitclock.api.data.ApiBlocksTerseResponse;
import org.transitclock.api.data.ApiCalendarsResponse;
import org.transitclock.api.data.ApiDirectionsResponse;
import org.transitclock.api.data.ApiExportsDataResponse;
import org.transitclock.api.data.ApiIdsResponse;
import org.transitclock.api.data.ApiPredictionsResponse;
import org.transitclock.api.data.ApiRoutesResponse;
import org.transitclock.api.data.ApiRoutesDetailsResponse;
import org.transitclock.api.data.ApiSchedulesHorizStops;
import org.transitclock.api.data.ApiSchedulesVertStopsResponse;
import org.transitclock.api.data.ApiTrip;
import org.transitclock.api.data.ApiTripPatternsResponse;
import org.transitclock.api.data.ApiTripWithTravelTimes;
import org.transitclock.api.data.ApiVehicleConfigsResponse;
import org.transitclock.api.data.ApiVehicleToBlockResponse;
import org.transitclock.api.data.ApiVehiclesDetailsResponse;
import org.transitclock.api.data.ApiVehiclesResponse;
import org.transitclock.api.utils.StandardParameters;
import org.transitclock.domain.structs.Location;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/v1/key/{key}/agency/{agency}")
public interface TransitimeApi {
    // For specifying how vehicles should be drawn in the UI.
    enum UiMode {
        NORMAL,
        SECONDARY,
        MINOR
    }
    /**
     * Handles the "vehicles" command. Returns data for all vehicles or for the vehicles specified
     * via the query string.
     *
     * <p>A Response object is returned instead of a regular object so that can have one method for
     * the both XML and JSON yet always return the proper media type even if it is configured via
     * the query string "format" parameter as opposed to the accept header.
     *
     * @param stdParameters        StdParametersBean that gets the standard parameters from the URI, query
     *                             string, and headers.
     * @param vehicleIds           Optional way of specifying which vehicles to get data for
     * @param routesIdOrShortNames Optional way of specifying which routes to get data for
     * @param stopId               Optional way of specifying a stop so can get predictions for routes and
     *                             determine which vehicles are the ones generating the predictions. The other vehicles are
     *                             labeled as minor so they can be drawn specially in the UI.
     * @param numberPredictions    For when determining which vehicles are generating the predictions
     *                             so can label minor vehicles
     *
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
    ResponseEntity<ApiVehiclesResponse> getVehicles(
            StandardParameters stdParameters,

            @Parameter(description = "Vehicles is list.")
            @RequestParam(value = "v", required = false, defaultValue = "")
            List<String> vehicleIds,

            @Parameter(description = "Specifies which vehicles to get data for.")
            @RequestParam(value = "r", required = false, defaultValue = "")
            List<String> routesIdOrShortNames,

            @Parameter(
                    description = "Specifies a stop so can get predictions for routes and"
                            + " determine which vehicles are the ones generating the"
                            + " predictions. The other vehicles are labeled as minor so"
                            + " they can be drawn specially in the UI."
            )
            @RequestParam(value = "s", required = false)
            String stopId,

            @Parameter(description = "Number of predictions to show.")
            @RequestParam(value = "numPreds", required = false, defaultValue = "2")
            int numberPredictions);

    @Operation(
            summary = "Returns data for vehicles assignment for specific block in current day",
            description = "Returns data for vehicles assignment for specific block in current day")
    @GetMapping(
            value = "/command/vehiclesToBlock",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    ResponseEntity<ApiVehicleToBlockResponse> getVehiclesToBlock(
            StandardParameters stdParameters,
            @Parameter(description = "Block id")
            @RequestParam(value = "blockId")
            String blockId);

    /**
     * Handles the vehicleIds command. Returns list of vehicle IDs.
     *
     * @param stdParameters
     *
     * @return
     */
    @GetMapping(value = "/command/vehicleIds")
    @Operation(
            summary = "Gets the list of vehicles Id",
            tags = {"vehicle"})
    ResponseEntity<ApiIdsResponse> getVehicleIds(StandardParameters stdParameters);

    @GetMapping(value = "/command/vehicleLocation",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "It gets the location for the specified vehicle.",
            description = "It gets the location for the specified vehicle.",
            tags = {"vehicle"})
    ResponseEntity<Location> getVehicleLocation(
            StandardParameters stdParameters,
            @Parameter(description = "Specifies the vehicle from which to get the location from.", required = true)
            @RequestParam(value = "v") String vehicleId);

    /**
     * Handles the "vehiclesDetails" command. Returns detailed data for all vehicles or for the
     * vehicles specified via the query string. This data includes things not necessarily intended
     * for the public, such as schedule adherence and driver IDs.
     *
     * <p>A Response object is returned instead of a regular object so that can have one method for
     * the both XML and JSON yet always return the proper media type even if it is configured via
     * the query string "format" parameter as opposed to the accept header.
     *
     * @param stdParameters        StdParametersBean that gets the standard parameters from the URI, query
     *                             string, and headers.
     * @param vehicleIds           Optional way of specifying which vehicles to get data for
     * @param routesIdOrShortNames Optional way of specifying which routes to get data for
     * @param stopId               Optional way of specifying a stop so can get predictions for routes and
     *                             determine which vehicles are the ones generating the predictions. The other vehicles are
     *                             labeled as minor so they can be drawn specially in the UI.
     * @param numberPredictions    For when determining which vehicles are generating the predictions
     *                             so can label minor vehicles
     *
     * @return The Response object already configured for the specified media type.
     */
    @GetMapping(value = "/command/vehiclesDetails",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Returns detailed data for all " + "vehicles or for the vehicles specified via the query string",
            description = "Returns detailed data for all vehicles or for the vehicles specified via the"
                    + " query string. This data  includes things not necessarily intended for"
                    + " the public, such as schedule adherence and driver IDs.",
            tags = {"vehicle"})
    ResponseEntity<ApiVehiclesDetailsResponse> getVehiclesDetails(
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
            boolean onlyAssigned);

    /**
     * Gets information including vehicle IDs for all vehicles that have been configured. Useful for
     * creating a vehicle selector.
     *
     * @param stdParameters
     *
     * @return
     */
    @GetMapping(value = "/command/vehicleConfigs",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Returns a list of vehilces with its configurarion.",
            description = "Returns a list of vehicles coniguration which inclides description, capacity,"
                    + " type and crushCapacity.",
            tags = {"vehicle"})
    ResponseEntity<ApiVehicleConfigsResponse> getVehicleConfigs(StandardParameters stdParameters);

    /**
     * Handles "predictions" command. Gets predictions from server and returns the corresponding
     * response.
     *
     * <p>A Response object is returned instead of a regular object so that can have one method for
     * the both XML and JSON yet always return the proper media type even if it is configured via
     * the query string "format" parameter as opposed to the accept header.
     *
     * @param stdParameters     StdParametersBean that gets the standard parameters from the URI, query
     *                          string, and headers.
     * @param routeStopStrs     List of route/stops to return predictions for. If route not specified
     *                          then data will be returned for all routes for the specified stop. The route specifier is
     *                          the route id or the route short name. It is often best to use route short name for
     *                          consistency across configuration changes (route ID is not consistent for many agencies).
     *                          The stop specified can either be the stop ID or the stop code. Each route/stop is
     *                          separated by the "|" character so for example the query string could have
     *                          "rs=43|2029&rs=43|3029"
     * @param stopStrs          List of stops to return predictions for. Provides predictions for all routes
     *                          that serve the stop. Can use either stop ID or stop code. Can specify multiple stops.
     * @param numberPredictions Maximum number of predictions to return. Default value is 3.
     *
     * @return
     */
    @GetMapping(value = "/command/predictions",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(summary = "Gets predictions from server", tags = {"prediction"})
    ResponseEntity<ApiPredictionsResponse> getPredictions(
            StandardParameters stdParameters,
            @Parameter(description = "List of route/stops to return predictions for. If route not specified then data will be returned for all routes for the specified stop. The route specifier is the route id"
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
            @RequestParam(value = "s")
            List<String> stopStrs,
            @Parameter(description = "Maximum number of predictions to return.")
            @RequestParam(value = "numPreds", defaultValue = "3")
            int numberPredictions);

    /**
     * Handles "predictionsByLoc" command. Gets predictions from server and returns the
     * corresponding response.
     *
     * <p>A Response object is returned instead of a regular object so that can have one method for
     * the both XML and JSON yet always return the proper media type even if it is configured via
     * the query string "format" parameter as opposed to the accept header.
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
    @GetMapping(value = "/command/predictionsByLoc",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Gets predictions from server by location",
            tags = {"prediction"})
    ResponseEntity<ApiPredictionsResponse> getPredictions(
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
            int numberPredictions);

    /**
     * Handles the "routes" command. Returns summary data describing all of the routes. Useful for
     * creating a route selector as part of a UI.
     *
     * @param stdParameters
     *
     * @return
     */
    @GetMapping(value = "/command/routes",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Gets the list of routes.",
            description = "Gets a list of the existing routes in the server. It might be filtered"
                    + " according to routeId or routeShortName. If more than one route have the"
                    + " same shortName, it is possible to specify keepDuplicates parameter to"
                    + " show all of them. ",
            tags = {"base data", "route"})
    ResponseEntity<ApiRoutesResponse> getRoutes(
            StandardParameters stdParameters,
            @Parameter(description = "List of routeId or routeShortName. Example: r=1&r=2", required = false)
            @RequestParam(value = "r", required = false, defaultValue = "")
            List<String> routeIdsOrShortNames,
            @Parameter(description = "Return all routes when more than one have the same shortName.", required = false)
            @RequestParam(value = "keepDuplicates", required = false, defaultValue = "false")
            Boolean keepDuplicates);

    /**
     * Handles the "routesDetails" command. Provides detailed information for a route includes all
     * stops and paths such that it can be drawn in a map.
     *
     * @param stdParameters
     * @param routeIdsOrShortNames list of route IDs or route short names. If a single route is
     *                             specified then data for just the single route is returned. If no route is specified then
     *                             data for all routes returned in an array. If multiple routes specified then data for
     *                             those routes returned in an array.
     * @param stopId               optional. If set then only this stop and the remaining ones on the trip pattern
     *                             are marked as being for the UI and can be highlighted. Useful for when want to emphasize
     *                             in the UI only the stops that are of interest to the user.
     * @param direction            optional. If set then only the shape for specified direction is marked as
     *                             being for the UI. Needed for situations where a single stop is used for both directions
     *                             of a route and want to highlight in the UI only the stops and the shapes that the user is
     *                             actually interested in.
     * @param tripPatternId        optional. If set then only the specified trip pattern is marked as being
     *                             for the UI.
     *
     * @return
     *
     * @
     */
    @GetMapping(value = "/command/routesDetails",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Provides detailed information for a route.",
            description = "Provides detailed information for a route includes all stops "
                    + "and paths such that it can be drawn in a map.",
            tags = {"base data", "route"})
    ResponseEntity<ApiRoutesDetailsResponse> getRouteDetails(
            StandardParameters stdParameters,
            @Parameter(description = "List of routeId or routeShortName. Example: r=1&r=2", required = false)
            @RequestParam(value = "r", required = false, defaultValue = "")
            List<String> routeIdsOrShortNames,
            @Parameter(description = "If set then only the shape for specified direction is marked as being for the UI.", required = false)
            @RequestParam(value = "d", required = false)
            String directionId,
            @Parameter(description = "If set then only this stop and the remaining ones on the trip"
                            + " pattern are marked as being for the UI and can be"
                            + " highlighted. Useful for when want to emphasize in the"
                            + " UI only  the stops that are of interest to the user.",
                    required = false)
            @RequestParam(value = "s", required = false)
            String stopId,
            @Parameter(description = "If set then only the specified trip pattern is marked as being" + " for the UI",
                    required = false)
            @RequestParam(value = "tripPattern", required = false)
            String tripPatternId);

    /**
     * Handles the "stops" command. Returns all stops associated with a route, grouped by direction.
     * Useful for creating a UI where user needs to select a stop from a list.
     *
     * @param stdParameters
     * @param routeShortName
     *
     * @return
     *
     * @
     */
    @GetMapping(value = "/command/stops",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Retrives bus stops from the server.",
            description = "Returns all stops associated with a route, grouped by direction. Useful for creating a UI where user needs to select a stop from a list.",
            tags = {"base data", "stop"})
    ResponseEntity<ApiDirectionsResponse> getStops(
            StandardParameters stdParameters,
            @Parameter(description = "if set, retrives only busstops belongind to the route. It might be routeId or route shrot name.", required = false)
            @RequestParam(value = "r", required = false)
            String routesIdOrShortNames);

    /**
     * Handles the "block" command which outputs configuration data for the specified block ID and
     * service ID. Includes all sub-data such as trips and trip patterns.
     *
     * @param stdParameters
     * @param blockId
     * @param serviceId
     *
     * @return
     *
     * @
     */
    @GetMapping(value = "/command/block",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Retrives configuration data for the specified block ID and service ID",
            description = "Retrives configuration data for the specified block ID and service ID. "
                    + "Includes all sub-data such as trips ans trip patterns."
                    + "Every trip is associated with a block.",
            tags = {"base data", "trip", "block"})
    ResponseEntity<ApiBlock> getBlock(
            StandardParameters stdParameters,
            @Parameter(description = "Block id to be asked.", required = true)
            @RequestParam(value = "blockId")
            String blockId,
            @Parameter(description = "Service id to be asked.", required = true)
            @RequestParam(value = "serviceId")
            String serviceId);

    /**
     * Handles the "blocksTerse" command which outputs configuration data for the specified block
     * ID. Does not include trip pattern and schedule data for trips.
     *
     * @param stdParameters
     * @param blockId
     *
     * @return
     *
     * @
     */
    @GetMapping(value = "/command/blocksTerse",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Retrives configuration data for the specified block ID.",
            description = "Retrives configuration data for the specified block ID. It does not include"
                    + " trip patterns.Every trip is associated with a block.",
            tags = {"base data", "trip", "block"})
    ResponseEntity<ApiBlocksTerseResponse> getBlocksTerse(
            StandardParameters stdParameters,
            @Parameter(description = "Block id to be asked.", required = true) @RequestParam(value = "blockId")
            String blockId);

    /**
     * Handles the "blocks" command which outputs configuration data for the specified block ID.
     * Includes all sub-data such as trips and trip patterns.
     *
     * @param stdParameters
     * @param blockId
     *
     * @return
     *
     * @
     */
    @GetMapping(value = "/command/blocks",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Retrives configuration data for the specified block ID",
            description = "Retrives configuration data for the specified block ID. Includes all sub-data"
                    + " such as trips and trip.Every trip is associated with a block.",
            tags = {"base data", "trip", "block"})
    ResponseEntity<ApiBlocksResponse> getBlocks(
            StandardParameters stdParameters,
            @Parameter(description = "Block id to be asked.", required = true) @RequestParam(value = "blockId")
            String blockId);

    /**
     * Handles the "blockIds" command. Returns list of block IDs.
     *
     * @param stdParameters
     *
     * @return
     *
     * @
     */
    @GetMapping(value = "/command/blockIds",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Retrives a list of all blockId for the specified service ID",
            description = "Retrives a list of all blockId for the specified service ID."
                    + "Every trip is associated with a block.",
            tags = {"base data", "trip", "block"})
    ResponseEntity<ApiIdsResponse> getBlockIds(
            StandardParameters stdParameters,
            @Parameter(description = "if set, returns only the data for that serviceId.", required = false)
            @RequestParam(value = "serviceId")
            String serviceId);

    /**
     * Gets which blocks are active. Can optionally specify list of routes and how much before a
     * block is supposed to start is it considered active.
     *
     * @param stdParameters           StdParametersBean that gets the standard parameters from the URI, query
     *                                string, and headers.
     * @param routesIdOrShortNames    Optional parameter for specifying which routes want data for.
     * @param allowableBeforeTimeSecs Optional parameter. A block will be active if the time is
     *                                between the block start time minus allowableBeforeTimeSecs and the block end time.
     *                                Default value for allowableBeforeTimeSecs is 0.
     *
     * @return
     *
     * @
     */
    @GetMapping(value = "/command/activeBlocks",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    // @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Gets which blocks are active",
            description = "Retrives a list of active blocks. Optionally can be filered accorditn to"
                    + " routesIdOrShortNames params.Every trip is associated with a block.",
            tags = {"prediction", "trip", "block"})
    ResponseEntity<ApiActiveBlocksResponse> getActiveBlocks(
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
            int allowableBeforeTimeSecs);

    @GetMapping(value = "/command/activeBlocksByRoute",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Gets which blocks are active by route",
            description = "Retrives a list routes with its  active blocks. Optionally can be filered"
                    + " according to routesIdOrShortNames params.Every trip is associated with"
                    + " a block.",
            tags = {"prediction", "trip", "block", "route"})
    ResponseEntity<ApiActiveBlocksRoutesResponse> getActiveBlocksByRoute(
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
            int allowableBeforeTimeSecs);

    @GetMapping(value = "/command/activeBlocksByRouteWithoutVehicles",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Gets which blocks are active by route.",
            description = "Retrives a list routes with its  active blocks, without the vechicles."
                    + " Optionally can be filered accorditn to routesIdOrShortNames"
                    + " params.Every trip is associated with a block.",
            tags = {"prediction", "trip", "block", "route"})
    ResponseEntity<ApiActiveBlocksRoutesResponse> getActiveBlocksByRouteWithoutVehicles(
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
            int allowableBeforeTimeSecs);

    @GetMapping(value = "/command/activeBlockByRouteWithVehicles",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Gets which blocks are active by route.",
            description = "Retrives a list routes with its  active blocks, including the vechicles. "
                    + "Optionally can be filered accorditn to routesIdOrShortNames params."
                    + "Every trip is associated with a block.",
            tags = {"prediction", "trip", "block", "route", "vehicle"})
    ResponseEntity<ApiActiveBlocksRoutesResponse> getActiveBlockByRouteWithVehicles(
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
            int allowableBeforeTimeSecs);

    @GetMapping(value = "/command/activeBlockByRouteNameWithVehicles",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Gets which blocks are active by routeName.",
            description = "Retrives a list routes with its  active blocks, including the vechicles. "
                    + "Optionally can be filered accorditn to routesIdOrShortNames params."
                    + "Every trip is associated with a block.",
            tags = {"prediction", "trip", "block", "route", "vehicle"})
    ResponseEntity<ApiActiveBlocksRoutesResponse> getActiveBlockByRouteNameWithVehicles(
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
            int allowableBeforeTimeSecs);

    @GetMapping(value = "/command/vehicleAdherenceSummary",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Returns the counters  for the number of current vehicles running early, late" + " or on time. ",
            description = "Returns the amount of vehicles running  early, late or on time. Besides"
                    + " specify the amount of vehicles no predictables and the amount of active"
                    + " blocks.",
            tags = {"prediction"})
    ResponseEntity<ApiAdherenceSummary> getVehicleAdherenceSummary(
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
            int allowableBeforeTimeSecs);

    /**
     * Handles the "trip" command which outputs configuration data for the specified trip. Includes
     * all sub-data such as trip patterns.
     *
     * @param stdParameters
     * @param tripId        Can be the GTFS trip_id or the trip_short_name
     *
     * @return
     *
     * @
     */
    @GetMapping(value = "/command/trip",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Gets the configuration data of a trip.",
            description = "Gets the configuration data of a trip",
            tags = {"base data", "trip"})
    ResponseEntity<ApiTrip> getTrip(
            StandardParameters stdParameters,
            @Parameter(description = "Trip id", required = true) @RequestParam(value = "tripId") String tripId);

    /**
     * Handles the "tripWithTravelTimes" command which outputs configuration data for the specified
     * trip. Includes all sub-data such as trip patterns.
     *
     * @param stdParameters
     * @param tripId
     *
     * @return
     *
     * @
     */
    @GetMapping(value = "/command/tripWithTravelTimes",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Gets the configuration data of a trip.",
            description = "Gets the configuration data of a trip. Includes all sub-data such as trip" + " patterns",
            tags = {"base data", "trip"})
    ResponseEntity<ApiTripWithTravelTimes> getTripWithTravelTimes(
            StandardParameters stdParameters,
            @Parameter(description = "Trip id", required = true) @RequestParam(value = "tripId") String tripId);

    /**
     * Handles the tripIds command. Returns list of trip IDs.
     *
     * @param stdParameters
     *
     * @return
     *
     * @
     */
    @GetMapping(value = "/command/tripIds",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Retrives a list of all tripIds",
            description = "Retrives a list of all tripIds.",
            tags = {"base data", "trip"})
    ResponseEntity<ApiIdsResponse> getTripIds(StandardParameters stdParameters);

    /**
     * Handles the "tripPattern" command which outputs trip pattern configuration data for the
     * specified route.
     *
     * @param stdParameters
     * @param routesIdOrShortNames
     *
     * @return
     *
     * @
     */
    @GetMapping(value = "/command/tripPatterns",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Retrives a list of all trip patters.",
            description = "Retrives a list of all trip patters for the specific routeId or" + " routeShortName.",
            tags = {"base data", "trip"})
    ResponseEntity<ApiTripPatternsResponse> getTripPatterns(
            StandardParameters stdParameters,
            @Parameter(description = "Specifies the routeId or routeShortName.", required = true)
            @RequestParam(value = "r")
            String routesIdOrShortNames);

    /**
     * Handles the "scheduleVertStops" command which outputs schedule for the specified route. The
     * data is output such that the stops are listed vertically (and the trips are horizontal). For
     * when there are a good number of stops but not as many trips, such as for commuter rail.
     *
     * @param stdParameters
     * @param routesIdOrShortNames
     *
     * @return
     *
     * @
     */
    @GetMapping(value = "/command/scheduleVertStops",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Retrives schedule for the specified route.",
            description = "Retrives schedule for the specified route.  The data is output such that the"
                    + " stops are listed vertically (and the trips are horizontal). For when"
                    + " there are a good number of stops but not as many trips, such as for"
                    + " commuter rail.",
            tags = {"base data", "schedule"})
    ResponseEntity<ApiSchedulesVertStopsResponse> getScheduleVertStops(
            StandardParameters stdParameters,
            @Parameter(description = "Specifies the routeId or routeShortName.", required = true)
            @RequestParam(value = "r")
            String routesIdOrShortNames);

    /**
     * Handles the "scheduleHorizStops" command which outputs schedule for the specified route. The
     * data is output such that the stops are listed horizontally (and the trips are vertical). For
     * when there are many more trips than stops, which is typical for bus routes.
     *
     * @param stdParameters
     * @param routesIdOrShortNames
     *
     * @return
     *
     * @
     */
    @GetMapping(value = "/command/scheduleHorizStops",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Retrives schedule for the specified route.",
            description = "Retrives schedule for the specified route.  The data is output such that the"
                    + " stops are listed horizontally (and the trips are vertical). For when"
                    + " there are a good number of stops but not as many trips, such as for"
                    + " commuter rail.",
            tags = {"base data", "schedule"})
    ResponseEntity<ApiSchedulesHorizStops> getScheduleHorizStops(
            StandardParameters stdParameters,
            @Parameter(description = "Specifies the routeId or routeShortName.", required = true)
            @RequestParam(value = "r")
            String routesIdOrShortNames);

    /**
     * For getting Agency data for a specific agencyId.
     *
     * @param stdParameters
     *
     * @return
     *
     * @
     */
    @GetMapping(value = "/command/agencyGroup")
    @Operation(
            summary = "Retrives agency infomation.",
            description = "Retrives agency infomation, including extent.",
            tags = {"base data", "agency"})
    ResponseEntity<ApiAgenciesResponse> getAgencyGroup(StandardParameters stdParameters);

    /**
     * For getting calendars that are currently active.
     *
     * @param stdParameters
     *
     * @return
     *
     * @
     */
    @GetMapping(value = "/command/currentCalendars",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Retrives current calendar infomation.",
            description = "Retrives current calendar infomation. Only teh calendar that applies to" + " current day",
            tags = {"base data", "calendar", "serviceId"})
    ResponseEntity<ApiCalendarsResponse> getCurrentCalendars(StandardParameters stdParameters);

    /**
     * For getting all calendars.
     *
     * @param stdParameters
     *
     * @return
     *
     * @
     */
    @GetMapping(value = "/command/allCalendars",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Retrives all calendar infomation.",
            description = "Retrives all calendar infomation.",
            tags = {"base data", "calendar", "serviceId"})
    ResponseEntity<ApiCalendarsResponse> getAllCalendars(StandardParameters stdParameters);

    /**
     * Handles the "serviceIds" command. Returns list of all service IDs.
     *
     * @param stdParameters
     *
     * @return
     *
     * @
     */
    @GetMapping(value = "/command/serviceIds",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Retrives all service id.",
            description = "Retrives all service id.",
            tags = {"base data", "serviceId"})
    ResponseEntity<ApiIdsResponse> getServiceIds(StandardParameters stdParameters);

    /**
     * Handles the currentServiceIds command. Returns list of service IDs that are currently active.
     *
     * @param stdParameters
     *
     * @return
     *
     * @
     */
    @GetMapping(value = "/command/currentServiceIds",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Retrives current service id.",
            description = "Retrives current service id.",
            tags = {"base data", "serviceId"})
    ResponseEntity<ApiIdsResponse> getCurrentServiceIds(StandardParameters stdParameters);

    @Operation(summary = "Returns exports list", description = "Returns exports list")
    @GetMapping(value = "/command/exports",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    ResponseEntity<ApiExportsDataResponse> getExports(StandardParameters stdParameters);

    @Operation(summary = "Return export file", description = "Return export file")
    @GetMapping(value = "/command/getExportFile",
            produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    ResponseEntity<Object> getExportById(
            StandardParameters stdParameters,
            @Parameter(description = "Id eksportu") @RequestParam(value = "id") long id);
}
