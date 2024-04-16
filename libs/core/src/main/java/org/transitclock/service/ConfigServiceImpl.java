/* (C)2023 */
package org.transitclock.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.transitclock.core.dataCache.PredictionDataCache;
import org.transitclock.core.dataCache.VehicleDataCache;
import org.transitclock.domain.structs.Agency;
import org.transitclock.domain.structs.Block;
import org.transitclock.domain.structs.Calendar;
import org.transitclock.domain.structs.Location;
import org.transitclock.domain.structs.Route;
import org.transitclock.domain.structs.Trip;
import org.transitclock.domain.structs.TripPattern;
import org.transitclock.domain.structs.VehicleConfig;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.service.contract.ConfigService;
import org.transitclock.service.dto.IpcBlock;
import org.transitclock.service.dto.IpcCalendar;
import org.transitclock.service.dto.IpcDirectionsForRoute;
import org.transitclock.service.dto.IpcPrediction;
import org.transitclock.service.dto.IpcPredictionsForRouteStopDest;
import org.transitclock.service.dto.IpcRoute;
import org.transitclock.service.dto.IpcRouteSummary;
import org.transitclock.service.dto.IpcSchedule;
import org.transitclock.service.dto.IpcTrip;
import org.transitclock.service.dto.IpcTripPattern;
import org.transitclock.service.dto.IpcVehicleComplete;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implements ConfigInterface to serve up configuration information to RMI clients.
 *
 * @author SkiBu Smith
 */
@Slf4j
@Component
public class ConfigServiceImpl implements ConfigService {
    @Autowired
    private VehicleDataCache vehicleDataCache;
    @Autowired
    private PredictionDataCache predictionDataCache;
    @Autowired
    private DbConfig dbConfig;


    /**
     * For getting route from routeIdOrShortName. Tries using routeIdOrShortName as first a route
     * short name to see if there is such a route. If not, then uses routeIdOrShortName as a
     * routeId.
     *
     * @param routeIdOrShortName
     * @return The Route, or null if no such route
     */
    private Route getRoute(String routeIdOrShortName) {
        Route dbRoute = dbConfig.getRouteByShortName(routeIdOrShortName);
        if (dbRoute == null) {
            dbRoute = dbConfig.getRouteById(routeIdOrShortName);
        }

        return dbRoute;
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getRoutes()
     */
    @Override
    public Collection<IpcRouteSummary> getRoutes() {
        var dbRoutes = dbConfig.getRoutes();

        return dbRoutes
                .stream()
                .map(IpcRouteSummary::new)
                .collect(Collectors.toList());
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getRoute(java.lang.String)
     */
    @Override
    public IpcRoute getRoute(String routeIdOrShortName, String directionId, String stopId, String tripPatternId)
            {
        // Determine the route
        Route dbRoute = getRoute(routeIdOrShortName);
        if (dbRoute == null) {
            return null;
        }

        var location = getLocationOfNextPredictedVehicle(dbRoute, directionId, stopId);
        // Convert db route into an ipc route and return it
        return new IpcRoute(dbRoute, dbConfig, directionId, stopId, tripPatternId, location);
    }

    /**
     * If stop specified then returns the location of the next predicted vehicle for that stop.
     * Returns null if stop not specified or no predictions for stop.
     *
     * @param dbRoute
     * @param directionId Set if want to know which part of route is major and which is minor.
     *     Otherwise set to null.
     * @param stopId
     * @return
     */
    private Location getLocationOfNextPredictedVehicle(Route dbRoute, String directionId, String stopId) {
        // If no stop specified then can't determine next predicted vehicle
        if (stopId == null) return null;

        // Determine the first IpcPrediction for the stop
        List<IpcPredictionsForRouteStopDest> predsList =
                predictionDataCache
                        .getPredictions(dbRoute.getShortName(), directionId, stopId);
        if (predsList.isEmpty()) return null;

        List<IpcPrediction> ipcPreds = predsList.get(0).getPredictionsForRouteStop();
        if (ipcPreds.isEmpty()) return null;

        // Based on the first prediction determine the current IpcVehicle info
        String vehicleId = ipcPreds.get(0).getVehicleId();

        IpcVehicleComplete vehicle =  vehicleDataCache.getVehicle(vehicleId);

        return new Location(vehicle.getLatitude(), vehicle.getLongitude());
    }
    /*
     * (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getRoutes(java.util.List)
     */
    @Override
    public List<IpcRoute> getRoutes(List<String> routeIdsOrShortNames) {
        List<IpcRoute> routes = new ArrayList<>();

        // If no route specified then return data for all routes
        if (routeIdsOrShortNames == null || routeIdsOrShortNames.isEmpty()) {
            List<Route> dbRoutes = dbConfig.getRoutes();
            for (Route dbRoute : dbRoutes) {
                IpcRoute ipcRoute = new IpcRoute(dbRoute, dbConfig, null, null, null, null);
                routes.add(ipcRoute);
            }
        } else {
            // Routes specified so return data for those routes
            for (String routeIdOrShortName : routeIdsOrShortNames) {
                // Determine the route
                Route dbRoute = getRoute(routeIdOrShortName);
                if (dbRoute == null) continue;

                IpcRoute ipcRoute = new IpcRoute(dbRoute, dbConfig, null, null, null, null);
                routes.add(ipcRoute);
            }
        }

        return routes;
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getStops(java.lang.String)
     */
    @Override
    public IpcDirectionsForRoute getStops(DbConfig dbConfig, String routeIdOrShortName) {
        // Get the db route info
        Route dbRoute = getRoute(routeIdOrShortName);
        if (dbRoute == null) return null;

        // Return the ipc route
        return new IpcDirectionsForRoute(dbConfig, dbRoute);
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getBlock(java.lang.String, java.lang.String)
     */
    @Override
    public IpcBlock getBlock(String blockId, String serviceId) {
        Block dbBlock = dbConfig.getBlock(serviceId, blockId);

        // If no such block then return null since can't create a IpcBlock
        if (dbBlock == null) {
            return null;
        }

        return new IpcBlock(dbBlock, dbConfig);
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getBlocks(java.lang.String)
     */
    @Override
    public Collection<IpcBlock> getBlocks(String blockId) {
        // For returning results
        List<IpcBlock> ipcBlocks = new ArrayList<>();

        // Get the blocks with specified ID
        Collection<Block> dbBlocks = dbConfig.getBlocksForAllServiceIds(blockId);

        // Convert blocks from DB into IpcBlocks
        for (Block dbBlock : dbBlocks) {
            ipcBlocks.add(new IpcBlock(dbBlock, dbConfig));
        }

        // Return result
        return ipcBlocks;
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getTrip(java.lang.String)
     */
    @Override
    public IpcTrip getTrip(String tripId) {
        Trip dbTrip = dbConfig.getTrip(tripId);

        // If couldn't find a trip with the specified trip_id then see if a
        // trip has the trip_short_name specified.
        if (dbTrip == null) {
            dbTrip = dbConfig.getTripUsingTripShortName(tripId);
        }

        // If no such trip then return null since can't create a IpcTrip
        if (dbTrip == null) {
            return null;
        }

        return new IpcTrip(dbTrip, dbConfig);
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getTripPattern(java.lang.String)
     */
    @Override
    public List<IpcTripPattern> getTripPatterns(String routeIdOrShortName) {
        Route dbRoute = getRoute(routeIdOrShortName);
        if (dbRoute == null) return null;

        List<TripPattern> dbTripPatterns = dbConfig.getTripPatternsForRoute(dbRoute.getId());
        if (dbTripPatterns == null) return null;

        List<IpcTripPattern> tripPatterns = new ArrayList<>();
        for (TripPattern dbTripPattern : dbTripPatterns) {
            tripPatterns.add(new IpcTripPattern(dbTripPattern, dbConfig));
        }
        return tripPatterns;
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getAgencies()
     */
    @Override
    public List<Agency> getAgencies() {
        return dbConfig.getAgencies();
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getSchedules(java.lang.String)
     */
    @Override
    public List<IpcSchedule> getSchedules(String routeIdOrShortName) {
        // Determine the route
        Route dbRoute = getRoute(routeIdOrShortName);
        if (dbRoute == null) return null;

        // Determine the blocks for the route for all service IDs
        List<Block> blocksForRoute = dbConfig.getBlocksForRoute(dbRoute.getId());

        // Convert blocks to list of IpcSchedule objects and return
        return IpcSchedule.createSchedules(dbRoute, blocksForRoute, dbConfig);
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getCurrentCalendars()
     */
    @Override
    public List<IpcCalendar> getCurrentCalendars() {
        // Get list of currently active calendars
        List<Calendar> calendarList = dbConfig.getCurrentCalendars();

        // Convert Calendar list to IpcCalendar list
        List<IpcCalendar> ipcCalendarList = new ArrayList<>();
        for (Calendar calendar : calendarList) {
            ipcCalendarList.add(new IpcCalendar(calendar));
        }

        return ipcCalendarList;
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getAllCalendars()
     */
    @Override
    public List<IpcCalendar> getAllCalendars() {
        // Get list of currently active calendars
        List<Calendar> calendarList = dbConfig.getCalendars();

        // Convert Calendar list to IpcCalendar list
        List<IpcCalendar> ipcCalendarList = new ArrayList<>();
        for (Calendar calendar : calendarList) {
            ipcCalendarList.add(new IpcCalendar(calendar));
        }

        return ipcCalendarList;
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getVehicleIds()
     */
    @Override
    public List<String> getVehicleIds() {
        Collection<VehicleConfig> vehicleConfigs = vehicleDataCache.getVehicleConfigs();
        List<String> vehicleIds = new ArrayList<>(vehicleConfigs.size());
        for (VehicleConfig vehicleConfig : vehicleConfigs) {
            vehicleIds.add(vehicleConfig.getId());
        }
        return vehicleIds;
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getServiceIds()
     */
    @Override
    public List<String> getServiceIds() {
        // Convert the Set from getServiceIds() to a List since need
        // to use a List for IPC due to serialization.
        return new ArrayList<>(dbConfig.getServiceIds());
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getCurrentServiceIds()
     */
    @Override
    public List<String> getCurrentServiceIds() {
        // Convert the Set from getCurrentServiceIds() to a List since need
        // to use a List for IPC due to serialization.
        return new ArrayList<>(dbConfig.getCurrentServiceIds());
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getTripIds()
     */
    @Override
    public List<String> getTripIds() {
        var trips = dbConfig.getTrips().values();
        return trips.stream()
                .map(Trip::getId)
                .collect(Collectors.toList());
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getBlockIds()
     */
    @Override
    public List<String> getBlockIds() {
        var blocks = dbConfig.getBlocks();
        return blocks.stream()
                .map(Block::getId)
                .distinct()
                .collect(Collectors.toList());
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getBlockIds()
     */
    @Override
    public List<String> getBlockIds(String serviceId) {
        if (serviceId == null) {
            return getBlockIds();
        }

        var blocks = dbConfig.getBlocks(serviceId);
        return blocks.stream()
                .map(Block::getId)
                .distinct()
                .collect(Collectors.toList());
    }
}
