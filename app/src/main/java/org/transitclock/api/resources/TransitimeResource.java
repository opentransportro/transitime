/* (C)2023 */
package org.transitclock.api.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.transitclock.api.data.ApiActiveBlocksResponse;
import org.transitclock.api.data.ApiActiveBlocksRoutesResponse;
import org.transitclock.api.data.ApiAdherenceSummary;
import org.transitclock.api.data.ApiAgenciesResponse;
import org.transitclock.api.data.ApiAgency;
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
import org.transitclock.api.data.ApiServerStatus;
import org.transitclock.api.data.ApiTrip;
import org.transitclock.api.data.ApiTripPatternsResponse;
import org.transitclock.api.data.ApiTripWithTravelTimes;
import org.transitclock.api.data.ApiVehicleConfigsResponse;
import org.transitclock.api.data.ApiVehicleToBlockResponse;
import org.transitclock.api.data.ApiVehiclesDetailsResponse;
import org.transitclock.api.data.ApiVehiclesResponse;
import org.transitclock.api.utils.PredsByLoc;
import org.transitclock.api.utils.StandardParameters;
import org.transitclock.api.utils.WebUtils;
import org.transitclock.core.TemporalDifference;
import org.transitclock.domain.hibernate.HibernateUtils;
import org.transitclock.domain.structs.Agency;
import org.transitclock.domain.structs.ExportTable;
import org.transitclock.domain.structs.Location;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.service.contract.PredictionsInterface.RouteStop;
import org.transitclock.service.dto.IpcActiveBlock;
import org.transitclock.service.dto.IpcBlock;
import org.transitclock.service.dto.IpcCalendar;
import org.transitclock.service.dto.IpcDirectionsForRoute;
import org.transitclock.service.dto.IpcPrediction;
import org.transitclock.service.dto.IpcPredictionsForRouteStopDest;
import org.transitclock.service.dto.IpcRoute;
import org.transitclock.service.dto.IpcRouteSummary;
import org.transitclock.service.dto.IpcSchedule;
import org.transitclock.service.dto.IpcServerStatus;
import org.transitclock.service.dto.IpcTrip;
import org.transitclock.service.dto.IpcTripPattern;
import org.transitclock.service.dto.IpcVehicle;
import org.transitclock.service.dto.IpcVehicleConfig;

import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

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
@RequiredArgsConstructor
public class TransitimeResource extends BaseApiResource implements TransitimeApi {
    private final DbConfig dbConfig;

    @Override
    public ResponseEntity<ApiVehiclesResponse> getVehicles(
            StandardParameters stdParameters,
            List<String> vehicleIds,
            List<String> routesIdOrShortNames,
            String stopId,
            int numberPredictions) {
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
        if (vehicles == null) {
            throw WebUtils.badRequestException("Invalid specifier for vehicles");
        }

        // To determine how vehicles should be drawn in UI. If stop
        // specified
        // when getting vehicle info then only the vehicles being predicted
        // for, should be highlighted. The others should be dimmed.
        Map<String, UiMode> uiTypesForVehicles = determineUiModesForVehicles(
                vehicles, stdParameters, routesIdOrShortNames, stopId, numberPredictions);

        ApiVehiclesResponse apiVehiclesResponse = new ApiVehiclesResponse(vehicles, uiTypesForVehicles);

        // return ApiVehicles response
        return ResponseEntity.ok(apiVehiclesResponse);
    }

    @Override
    public ResponseEntity<ApiVehicleToBlockResponse> getVehiclesToBlock(
            StandardParameters stdParameters,
            String blockId) {

        validate(stdParameters);

        // Get Vehicle data from server
        var result = vehiclesInterface.getVehicleToBlockConfig(blockId);
        ApiVehicleToBlockResponse res = new ApiVehicleToBlockResponse(result);

        // return ApiVehicles response
        return ResponseEntity.ok(res);
    }

    @Override
    public ResponseEntity<ApiIdsResponse> getVehicleIds(StandardParameters stdParameters) {
        // Make sure request is valid
        validate(stdParameters);

        // Get Vehicle data from server
        List<String> ids = configInterface.getVehicleIds();

        ApiIdsResponse apiIds = new ApiIdsResponse(ids);
        return ResponseEntity.ok(apiIds);
    }

    @Override
    public ResponseEntity<Location> getVehicleLocation(
            StandardParameters stdParameters,
            String vehicleId) {
        validate(stdParameters);

        // Get Vehicle data from server
        IpcVehicle vehicle = vehiclesInterface.get(vehicleId);
        if (vehicle == null) {
            throw WebUtils.badRequestException("Invalid specifier for " + "vehicle");
        }

        Location matchedLocation = new Location(vehicle.getPredictedLatitude(), vehicle.getPredictedLongitude());
        return ResponseEntity.ok(matchedLocation);
    }

    @Override
    public ResponseEntity<ApiVehiclesDetailsResponse> getVehiclesDetails(
            StandardParameters stdParameters,
            List<String> vehicleIds,
            List<String> routesIdOrShortNames,
            String stopId,
            int numberPredictions,
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
        if (vehicles == null) {
            throw WebUtils.badRequestException("Invalid specifier for " + "vehicles");
        }

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
        ApiVehiclesDetailsResponse apiVehiclesDetailsResponse =
                new ApiVehiclesDetailsResponse(vehicles, stdParameters.getAgencyId(), uiTypesForVehicles, onlyAssigned);

        // return ApiVehiclesDetails response
        return ResponseEntity.ok(apiVehiclesDetailsResponse);
    }


    @Override
    public ResponseEntity<ApiVehicleConfigsResponse> getVehicleConfigs(StandardParameters stdParameters) {
        // Make sure request is valid
        validate(stdParameters);

        // Get Vehicle data from server
        Collection<IpcVehicleConfig> ipcVehicleConfigs = vehiclesInterface.getVehicleConfigs();
        ApiVehicleConfigsResponse apiVehicleConfigsResponse = new ApiVehicleConfigsResponse(ipcVehicleConfigs);

        // return ApiVehiclesDetails response
        return ResponseEntity.ok(apiVehicleConfigsResponse);
    }

    @Override
    public ResponseEntity<ApiPredictionsResponse> getPredictions(
            StandardParameters stdParameters,
            List<String> routeStopStrs,
            List<String> stopStrs,
            int numberPredictions) {
        // Make sure request is valid
        validate(stdParameters);

        // Create list of route/stops that should get predictions for
        List<RouteStop> routeStopsList = new ArrayList<>();
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
            routeStopsList.add(new RouteStop(routeIdOrShortName, stopIdOrCode));
        }

        // Add to list the stops that should get predictions for
        for (String stopStr : stopStrs) {
            // Use null for route identifier so get predictions for all
            // routes for the stop
            routeStopsList.add(new RouteStop(null, stopStr));
        }

        // Actually get the predictions via IPC
        List<IpcPredictionsForRouteStopDest> predictions = predictionsInterface.get(routeStopsList, numberPredictions);

        // return ApiPredictions response
        ApiPredictionsResponse predictionsData = new ApiPredictionsResponse(predictions);
        return ResponseEntity.ok(predictionsData);
    }

    @Override
    public ResponseEntity<ApiPredictionsResponse> getPredictions(
            StandardParameters stdParameters,
            Double lat,
            Double lon,
            double maxDistance,
            int numberPredictions) {
        // Make sure request is valid
        validate(stdParameters);

        if (maxDistance > PredsByLoc.MAX_MAX_DISTANCE) {
            throw new RuntimeException("Maximum maxDistance parameter "
                                               + "is "
                                               + PredsByLoc.MAX_MAX_DISTANCE
                                               + "m but "
                                               + maxDistance
                                               + "m was specified in the request.");
        }

        // Get predictions by location
        List<IpcPredictionsForRouteStopDest> predictions =
                predictionsInterface.get(new Location(lat, lon), maxDistance, numberPredictions);

        // return ApiPredictions response
        ApiPredictionsResponse predictionsData = new ApiPredictionsResponse(predictions);
        return ResponseEntity.ok(predictionsData);
    }

    @Override
    public ResponseEntity<ApiRoutesResponse> getRoutes(
            StandardParameters stdParameters,
            List<String> routeIdsOrShortNames,
            Boolean keepDuplicates) {

        // Make sure request is valid
        validate(stdParameters);

        // Get agency info so can also return agency name
        List<Agency> agencies = configInterface.getAgencies();

        // Get route data from server
        ApiRoutesResponse routesData;
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

            routesData = new ApiRoutesResponse(processedRoutes, agencies.get(0));
        } else {
            // Get specified routes
            List<IpcRoute> ipcRoutes = configInterface.getRoutes(routeIdsOrShortNames);
            routesData = new ApiRoutesResponse(ipcRoutes, agencies.get(0));
        }

        // Create and return response
        return ResponseEntity.ok(routesData);
    }

    @Override
    public ResponseEntity<ApiRoutesDetailsResponse> getRouteDetails(
            StandardParameters stdParameters,
            List<String> routeIdsOrShortNames,
            String directionId,
            String stopId,
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
            if (route == null) {
                throw WebUtils.badRequestException("Route for route=" + routeIdOrShortName + " does not exist.");
            }

            ipcRoutes = new ArrayList<IpcRoute>();
            ipcRoutes.add(route);
        } else {
            // Multiple routes specified
            ipcRoutes = configInterface.getRoutes(routeIdsOrShortNames);
        }

        // Take the IpcRoute data array and create and return
        // ApiRoutesDetails object
        ApiRoutesDetailsResponse routeData = new ApiRoutesDetailsResponse(ipcRoutes, agencies.get(0));
        return stdParameters.createResponse(routeData);

    }

    @Override
    public ResponseEntity<ApiDirectionsResponse> getStops(
            StandardParameters stdParameters,
            String routesIdOrShortNames) {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get stops data from server
            IpcDirectionsForRoute stopsForRoute = configInterface.getStops(dbConfig, routesIdOrShortNames);

            // If the route doesn't exist then throw exception such that
            // Bad Request with an appropriate message is returned.
            if (stopsForRoute == null) {
                throw WebUtils.badRequestException("route=" + routesIdOrShortNames + " does not exist.");
            }

            // Create and return ApiDirections response
            ApiDirectionsResponse directionsData = new ApiDirectionsResponse(stopsForRoute);
            return stdParameters.createResponse(directionsData);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    @Override
    public ResponseEntity<ApiBlock> getBlock(
            StandardParameters stdParameters,
            String blockId,
            String serviceId) {

        // Make sure request is valid
        validate(stdParameters);
        if (serviceId == null) {
            throw WebUtils.badRequestException("Must specify serviceId");
        }

        try {
            // Get block data from server
            IpcBlock ipcBlock = configInterface.getBlock(blockId, serviceId);

            // If the block doesn't exist then throw exception such that
            // Bad Request with an appropriate message is returned.
            if (ipcBlock == null) {
                throw WebUtils.badRequestException(
                        "The blockId=" + blockId + " for serviceId=" + serviceId + " does not exist.");
            }

            // Create and return ApiBlock response
            ApiBlock apiBlock = new ApiBlock(ipcBlock);
            return stdParameters.createResponse(apiBlock);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    @Override
    public ResponseEntity<ApiBlocksTerseResponse> getBlocksTerse(
            StandardParameters stdParameters,
            String blockId) {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get block data from server
            Collection<IpcBlock> ipcBlocks = configInterface.getBlocks(blockId);

            // If the block doesn't exist then throw exception such that
            // Bad Request with an appropriate message is returned.
            if (ipcBlocks.isEmpty()) {
                throw WebUtils.badRequestException("The blockId=" + blockId + " does not exist.");
            }

            // Create and return ApiBlock response
            ApiBlocksTerseResponse apiBlocks = new ApiBlocksTerseResponse(ipcBlocks);
            return stdParameters.createResponse(apiBlocks);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    @Override
    public ResponseEntity<ApiBlocksResponse> getBlocks(
            StandardParameters stdParameters,
            String blockId) {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get block data from server
            Collection<IpcBlock> ipcBlocks = configInterface.getBlocks(blockId);

            // If the block doesn't exist then throw exception such that
            // Bad Request with an appropriate message is returned.
            if (ipcBlocks.isEmpty()) {
                throw WebUtils.badRequestException("The blockId=" + blockId + " does not exist.");
            }

            // Create and return ApiBlock response
            ApiBlocksResponse apiBlocksResponse = new ApiBlocksResponse(ipcBlocks);
            return stdParameters.createResponse(apiBlocksResponse);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    @Override
    public ResponseEntity<ApiIdsResponse> getBlockIds(
            StandardParameters stdParameters,
            String serviceId) {
        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get Vehicle data from server
            List<String> ids = configInterface.getBlockIds(serviceId);

            ApiIdsResponse apiIds = new ApiIdsResponse(ids);
            return stdParameters.createResponse(apiIds);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    @Override
    public ResponseEntity<ApiActiveBlocksResponse> getActiveBlocks(
            StandardParameters stdParameters,
            List<String> routesIdOrShortNames,
            int allowableBeforeTimeSecs) {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get active block data from server

            Collection<IpcActiveBlock> activeBlocks =
                    vehiclesInterface.getActiveBlocks(routesIdOrShortNames, allowableBeforeTimeSecs);

            // Create and return ApiBlock response
            ApiActiveBlocksResponse apiActiveBlocksResponse = new ApiActiveBlocksResponse(activeBlocks, stdParameters.getAgencyId());
            return stdParameters.createResponse(apiActiveBlocksResponse);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    @Override
    public ResponseEntity<ApiActiveBlocksRoutesResponse> getActiveBlocksByRoute(
            StandardParameters stdParameters,
            List<String> routesIdOrShortNames,
            int allowableBeforeTimeSecs) {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get active block data from server
            Collection<IpcActiveBlock> activeBlocks =
                    vehiclesInterface.getActiveBlocks(routesIdOrShortNames, allowableBeforeTimeSecs);

            // Create and return ApiBlock response
            ApiActiveBlocksRoutesResponse apiActiveBlocksRoutesResponse =
                    new ApiActiveBlocksRoutesResponse(activeBlocks, stdParameters.getAgencyId());
            return stdParameters.createResponse(apiActiveBlocksRoutesResponse);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    @Override
    public ResponseEntity<ApiActiveBlocksRoutesResponse> getActiveBlocksByRouteWithoutVehicles(
            StandardParameters stdParameters,
            List<String> routesIdOrShortNames,
            int allowableBeforeTimeSecs) {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get active block data from server
            Collection<IpcActiveBlock> activeBlocks =
                    vehiclesInterface.getActiveBlocksWithoutVehicles(routesIdOrShortNames, allowableBeforeTimeSecs);

            // Create and return ApiBlock response
            ApiActiveBlocksRoutesResponse apiActiveBlocksRoutesResponse =
                    new ApiActiveBlocksRoutesResponse(activeBlocks, stdParameters.getAgencyId());
            return stdParameters.createResponse(apiActiveBlocksRoutesResponse);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    @Override
    public ResponseEntity<ApiActiveBlocksRoutesResponse> getActiveBlockByRouteWithVehicles(
            StandardParameters stdParameters,
            String routesIdOrShortName,
            int allowableBeforeTimeSecs) {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get active block data from server
            Collection<IpcActiveBlock> activeBlocks =
                    vehiclesInterface.getActiveBlocksAndVehiclesByRouteId(routesIdOrShortName, allowableBeforeTimeSecs);

            // Create and return ApiBlock response
            ApiActiveBlocksRoutesResponse apiActiveBlocksRoutesResponse =
                    new ApiActiveBlocksRoutesResponse(activeBlocks, stdParameters.getAgencyId());
            return stdParameters.createResponse(apiActiveBlocksRoutesResponse);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    @Override
    public ResponseEntity<ApiActiveBlocksRoutesResponse> getActiveBlockByRouteNameWithVehicles(
            StandardParameters stdParameters,
            String routeName,
            int allowableBeforeTimeSecs) {
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
            ApiActiveBlocksRoutesResponse apiActiveBlocksRoutesResponse =
                    new ApiActiveBlocksRoutesResponse(activeBlocks, stdParameters.getAgencyId());

            return stdParameters.createResponse(apiActiveBlocksRoutesResponse);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    @Override
    public ResponseEntity<ApiAdherenceSummary> getVehicleAdherenceSummary(
            StandardParameters stdParameters,
            int allowableEarlySec,
            int allowableLateSec,
            int allowableBeforeTimeSecs) {

        // Make sure request is valid
        validate(stdParameters);

        try {

            int late = 0, ontime = 0, early = 0, nodata = 0, blocks = 0;

            Collection<IpcVehicle> ipcVehicles = vehiclesInterface.getVehiclesForBlocks();

            for (IpcVehicle v : ipcVehicles) {
                TemporalDifference adh = v.getRealTimeSchedAdh();

                if (adh == null) {
                    nodata++;
                } else if (adh.isEarlierThan(allowableEarlySec)) {
                    early++;
                } else if (adh.isLaterThan(allowableLateSec)) {
                    late++;
                } else {
                    ontime++;
                }
            }

            blocks = vehiclesInterface.getNumActiveBlocks(null, allowableBeforeTimeSecs);

            ApiAdherenceSummary resp = new ApiAdherenceSummary(late, ontime, early, nodata, blocks);

            return stdParameters.createResponse(resp);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    @Override
    public ResponseEntity<ApiTrip> getTrip(StandardParameters stdParameters, String tripId) {
        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get block data from server
            IpcTrip ipcTrip = configInterface.getTrip(tripId);

            // If the trip doesn't exist then throw exception such that
            // Bad Request with an appropriate message is returned.
            if (ipcTrip == null) {
                throw WebUtils.badRequestException("TripId=" + tripId + " does not exist.");
            }

            // Create and return ApiBlock response.
            // Include stop path info since just outputting single trip.
            ApiTrip apiTrip = new ApiTrip(ipcTrip, true);
            return stdParameters.createResponse(apiTrip);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    @Override
    public ResponseEntity<ApiTripWithTravelTimes> getTripWithTravelTimes(StandardParameters stdParameters, String tripId) {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get block data from server
            IpcTrip ipcTrip = configInterface.getTrip(tripId);

            // If the trip doesn't exist then throw exception such that
            // Bad Request with an appropriate message is returned.
            if (ipcTrip == null) {
                throw WebUtils.badRequestException("TripId=" + tripId + " does not exist.");
            }

            // Create and return ApiBlock response.
            // Include stop path info since just outputting single trip.
            ApiTripWithTravelTimes apiTrip = new ApiTripWithTravelTimes(ipcTrip, true);
            return stdParameters.createResponse(apiTrip);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    @Override
    public ResponseEntity<ApiIdsResponse> getTripIds(StandardParameters stdParameters) {
        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get Vehicle data from server
            List<String> ids = configInterface.getTripIds();

            ApiIdsResponse apiIds = new ApiIdsResponse(ids);
            return stdParameters.createResponse(apiIds);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    @Override
    public ResponseEntity<ApiTripPatternsResponse> getTripPatterns(StandardParameters stdParameters, String routesIdOrShortNames) {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get block data from server
            List<IpcTripPattern> ipcTripPatterns = configInterface.getTripPatterns(routesIdOrShortNames);

            // If the trip doesn't exist then throw exception such that
            // Bad Request with an appropriate message is returned.
            if (ipcTripPatterns == null) {
                throw WebUtils.badRequestException("route=" + routesIdOrShortNames + " does not exist.");
            }

            // Create and return ApiTripPatterns response
            ApiTripPatternsResponse apiTripPatternsResponse = new ApiTripPatternsResponse(ipcTripPatterns);
            return stdParameters.createResponse(apiTripPatternsResponse);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    @Override
    public ResponseEntity<ApiSchedulesVertStopsResponse> getScheduleVertStops(
            StandardParameters stdParameters,
            String routesIdOrShortNames) {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get block data from server
            List<IpcSchedule> ipcSchedules = configInterface.getSchedules(routesIdOrShortNames);

            // If the trip doesn't exist then throw exception such that
            // Bad Request with an appropriate message is returned.
            if (ipcSchedules == null) {
                throw WebUtils.badRequestException("route=" + routesIdOrShortNames + " does not exist.");
            }

            // Create and return ApiSchedules response
            ApiSchedulesVertStopsResponse apiSchedules = new ApiSchedulesVertStopsResponse(ipcSchedules);
            return stdParameters.createResponse(apiSchedules);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    @Override
    public ResponseEntity<ApiSchedulesHorizStops> getScheduleHorizStops(
            StandardParameters stdParameters,
            String routesIdOrShortNames) {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get block data from server
            List<IpcSchedule> ipcSchedules = configInterface.getSchedules(routesIdOrShortNames);

            // If the trip doesn't exist then throw exception such that
            // Bad Request with an appropriate message is returned.
            if (ipcSchedules == null) {
                throw WebUtils.badRequestException("route=" + routesIdOrShortNames + " does not exist.");
            }

            // Create and return ApiSchedules response
            ApiSchedulesHorizStops apiSchedules = new ApiSchedulesHorizStops(ipcSchedules);
            return stdParameters.createResponse(apiSchedules);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    @Override
    public ResponseEntity<ApiAgenciesResponse> getAgencyGroup(StandardParameters stdParameters) {

        // Make sure request is valid
        validate(stdParameters);
        // Get block data from server
        List<Agency> agencies = configInterface.getAgencies();

        ApiAgenciesResponse apiAgencies = new ApiAgenciesResponse(stdParameters.getAgencyId(), agencies);
        return ResponseEntity.ok(apiAgencies);
    }

    @Override
    public ResponseEntity<ApiCalendarsResponse> getCurrentCalendars(StandardParameters stdParameters) {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get block data from server
            List<IpcCalendar> ipcCalendars = configInterface.getCurrentCalendars();

            // Create and return ApiAgencies response
            ApiCalendarsResponse apiCalendarsResponse = new ApiCalendarsResponse(ipcCalendars);
            return stdParameters.createResponse(apiCalendarsResponse);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    @Override
    public ResponseEntity<ApiCalendarsResponse> getAllCalendars(StandardParameters stdParameters) {

        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get block data from server
            List<IpcCalendar> ipcCalendars = configInterface.getAllCalendars();

            // Create and return ApiAgencies response
            ApiCalendarsResponse apiCalendarsResponse = new ApiCalendarsResponse(ipcCalendars);
            return stdParameters.createResponse(apiCalendarsResponse);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    @Override
    public ResponseEntity<ApiIdsResponse> getServiceIds(StandardParameters stdParameters) {
        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get Vehicle data from server
            List<String> ids = configInterface.getServiceIds();

            ApiIdsResponse apiIds = new ApiIdsResponse(ids);
            return stdParameters.createResponse(apiIds);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    @Override
    public ResponseEntity<ApiIdsResponse> getCurrentServiceIds(StandardParameters stdParameters) {
        // Make sure request is valid
        validate(stdParameters);

        try {
            // Get Vehicle data from server
            List<String> ids = configInterface.getCurrentServiceIds();

            ApiIdsResponse apiIds = new ApiIdsResponse(ids);
            return stdParameters.createResponse(apiIds);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    @Override
    public ResponseEntity<ApiServerStatus> getServerStatus(StandardParameters stdParameters) {

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

    @Override
    public ResponseEntity<ApiExportsDataResponse> getExports(StandardParameters stdParameters) {
        validate(stdParameters);
        ApiExportsDataResponse result;
        Session session = HibernateUtils.getSession();
        try {
            result = new ApiExportsDataResponse(ExportTable.getExportTable(session));

            session.close();
            return stdParameters.createResponse(result);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            session.close();
            throw WebUtils.badRequestException(e);
        }
    }

    @Override
    public ResponseEntity<Object> getExportById(StandardParameters stdParameters, long id) {
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
     *
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
                if (!vehiclesGeneratingPreds.isEmpty() && ipcVehicle.getId().equals(vehiclesGeneratingPreds.get(0))) {
                    uiType = UiMode.NORMAL;
                } else if (vehiclesGeneratingPreds.contains(ipcVehicle.getId())) {
                    uiType = UiMode.SECONDARY;
                }

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
     *
     * @return List of vehicle IDs
     */
    private List<String> determineVehiclesGeneratingPreds(
            StandardParameters stdParameters, List<String> routesIdOrShortNames, String stopId, int numberPredictions) {
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
}
