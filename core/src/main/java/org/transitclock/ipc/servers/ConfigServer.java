/* (C)2023 */
package org.transitclock.ipc.servers;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.core.dataCache.VehicleDataCache;
import org.transitclock.db.structs.Agency;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.Calendar;
import org.transitclock.db.structs.Route;
import org.transitclock.db.structs.Trip;
import org.transitclock.db.structs.TripPattern;
import org.transitclock.db.structs.VehicleConfig;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.ipc.data.IpcBlock;
import org.transitclock.ipc.data.IpcCalendar;
import org.transitclock.ipc.data.IpcDirectionsForRoute;
import org.transitclock.ipc.data.IpcRoute;
import org.transitclock.ipc.data.IpcRouteSummary;
import org.transitclock.ipc.data.IpcSchedule;
import org.transitclock.ipc.data.IpcTrip;
import org.transitclock.ipc.data.IpcTripPattern;
import org.transitclock.ipc.interfaces.ConfigInterface;
import org.transitclock.ipc.rmi.AbstractServer;

/**
 * Implements ConfigInterface to serve up configuration information to RMI clients.
 *
 * @author SkiBu Smith
 */
@Slf4j
public class ConfigServer extends AbstractServer implements ConfigInterface {

    // Should only be accessed as singleton class
    private static ConfigServer singleton;


    /**
     * Starts up the ConfigServer so that RMI calls can query for configuration data. This will
     * automatically cause the object to continue to run and serve requests.
     *
     * @param agencyId
     * @return the singleton ConfigServer object. Usually does not need to used since the server
     *     will be fully running.
     */
    public static ConfigServer start(String agencyId) {
        if (singleton == null) {
            singleton = new ConfigServer(agencyId);
        }

        if (!singleton.getAgencyId().equals(agencyId)) {
            logger.error(
                    "Tried calling ConfigServer.start() for "
                            + "agencyId={} but the singleton was created for agencyId={}",
                    agencyId,
                    singleton.getAgencyId());
            return null;
        }

        return singleton;
    }

    /**
     * Constructor. Made private so that can only be instantiated by get(). Doesn't actually do
     * anything since all the work is done in the superclass constructor.
     *
     * @param agencyId for registering this object with the rmiregistry
     */
    private ConfigServer(String agencyId) {
        super(agencyId, ConfigInterface.class.getSimpleName());
    }

    /**
     * For getting route from routeIdOrShortName. Tries using routeIdOrShortName as first a route
     * short name to see if there is such a route. If not, then uses routeIdOrShortName as a
     * routeId.
     *
     * @param routeIdOrShortName
     * @return The Route, or null if no such route
     */
    private Route getRoute(String routeIdOrShortName) {
        DbConfig dbConfig = Core.getInstance().getDbConfig();
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
    public Collection<IpcRouteSummary> getRoutes() throws RemoteException {
        // Get the db route info
        DbConfig dbConfig = Core.getInstance().getDbConfig();
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
            throws RemoteException {
        // Determine the route
        Route dbRoute = getRoute(routeIdOrShortName);
        if (dbRoute == null) {
            return null;
        }

        // Convert db route into an ipc route and return it
        return new IpcRoute(dbRoute, directionId, stopId, tripPatternId);
    }

    /*
     * (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getRoutes(java.util.List)
     */
    @Override
    public List<IpcRoute> getRoutes(List<String> routeIdsOrShortNames) throws RemoteException {
        List<IpcRoute> routes = new ArrayList<>();

        // If no route specified then return data for all routes
        if (routeIdsOrShortNames == null || routeIdsOrShortNames.isEmpty()) {
            DbConfig dbConfig = Core.getInstance().getDbConfig();
            List<org.transitclock.db.structs.Route> dbRoutes = dbConfig.getRoutes();
            for (Route dbRoute : dbRoutes) {
                IpcRoute ipcRoute = new IpcRoute(dbRoute, null, null, null);
                routes.add(ipcRoute);
            }
        } else {
            // Routes specified so return data for those routes
            for (String routeIdOrShortName : routeIdsOrShortNames) {
                // Determine the route
                Route dbRoute = getRoute(routeIdOrShortName);
                if (dbRoute == null) continue;

                IpcRoute ipcRoute = new IpcRoute(dbRoute, null, null, null);
                routes.add(ipcRoute);
            }
        }

        return routes;
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getStops(java.lang.String)
     */
    @Override
    public IpcDirectionsForRoute getStops(String routeIdOrShortName) throws RemoteException {
        // Get the db route info
        Route dbRoute = getRoute(routeIdOrShortName);
        if (dbRoute == null) return null;

        // Return the ipc route
        return new IpcDirectionsForRoute(dbRoute);
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getBlock(java.lang.String, java.lang.String)
     */
    @Override
    public IpcBlock getBlock(String blockId, String serviceId) throws RemoteException {
        Block dbBlock = Core.getInstance().getDbConfig().getBlock(serviceId, blockId);

        // If no such block then return null since can't create a IpcBlock
        if (dbBlock == null) {
            return null;
        }

        return new IpcBlock(dbBlock);
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getBlocks(java.lang.String)
     */
    @Override
    public Collection<IpcBlock> getBlocks(String blockId) throws RemoteException {
        // For returning results
        List<IpcBlock> ipcBlocks = new ArrayList<>();

        // Get the blocks with specified ID
        Collection<Block> dbBlocks = Core.getInstance().getDbConfig().getBlocksForAllServiceIds(blockId);

        // Convert blocks from DB into IpcBlocks
        for (Block dbBlock : dbBlocks) {
            ipcBlocks.add(new IpcBlock(dbBlock));
        }

        // Return result
        return ipcBlocks;
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getTrip(java.lang.String)
     */
    @Override
    public IpcTrip getTrip(String tripId) throws RemoteException {
        Trip dbTrip = Core.getInstance().getDbConfig().getTrip(tripId);

        // If couldn't find a trip with the specified trip_id then see if a
        // trip has the trip_short_name specified.
        if (dbTrip == null) {
            dbTrip = Core.getInstance().getDbConfig().getTripUsingTripShortName(tripId);
        }

        // If no such trip then return null since can't create a IpcTrip
        if (dbTrip == null) {
            return null;
        }

        return new IpcTrip(dbTrip);
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getTripPattern(java.lang.String)
     */
    @Override
    public List<IpcTripPattern> getTripPatterns(String routeIdOrShortName) throws RemoteException {
        DbConfig dbConfig = Core.getInstance().getDbConfig();

        Route dbRoute = getRoute(routeIdOrShortName);
        if (dbRoute == null) return null;

        List<TripPattern> dbTripPatterns = dbConfig.getTripPatternsForRoute(dbRoute.getId());
        if (dbTripPatterns == null) return null;

        List<IpcTripPattern> tripPatterns = new ArrayList<>();
        for (TripPattern dbTripPattern : dbTripPatterns) {
            tripPatterns.add(new IpcTripPattern(dbTripPattern));
        }
        return tripPatterns;
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getAgencies()
     */
    @Override
    public List<Agency> getAgencies() throws RemoteException {
        return Core.getInstance().getDbConfig().getAgencies();
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getSchedules(java.lang.String)
     */
    @Override
    public List<IpcSchedule> getSchedules(String routeIdOrShortName) throws RemoteException {
        // Determine the route
        Route dbRoute = getRoute(routeIdOrShortName);
        if (dbRoute == null) return null;

        // Determine the blocks for the route for all service IDs
        List<Block> blocksForRoute = Core.getInstance().getDbConfig().getBlocksForRoute(dbRoute.getId());

        // Convert blocks to list of IpcSchedule objects and return
        return IpcSchedule.createSchedules(dbRoute, blocksForRoute);
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getCurrentCalendars()
     */
    @Override
    public List<IpcCalendar> getCurrentCalendars() {
        // Get list of currently active calendars
        List<Calendar> calendarList = Core.getInstance().getDbConfig().getCurrentCalendars();

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
        List<Calendar> calendarList = Core.getInstance().getDbConfig().getCalendars();

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
    public List<String> getVehicleIds() throws RemoteException {
        Collection<VehicleConfig> vehicleConfigs =
                VehicleDataCache.getInstance().getVehicleConfigs();
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
    public List<String> getServiceIds() throws RemoteException {
        // Convert the Set from getServiceIds() to a List since need
        // to use a List for IPC due to serialization.
        return new ArrayList<>(Core.getInstance().getDbConfig().getServiceIds());
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getCurrentServiceIds()
     */
    @Override
    public List<String> getCurrentServiceIds() throws RemoteException {
        // Convert the Set from getCurrentServiceIds() to a List since need
        // to use a List for IPC due to serialization.
        return new ArrayList<>(Core.getInstance().getDbConfig().getCurrentServiceIds());
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getTripIds()
     */
    @Override
    public List<String> getTripIds() throws RemoteException {
        var trips = Core.getInstance().getDbConfig().getTrips().values();
        return trips.stream()
                .map(Trip::getId)
                .collect(Collectors.toList());
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getBlockIds()
     */
    @Override
    public List<String> getBlockIds() throws RemoteException {
        var blocks = Core.getInstance().getDbConfig().getBlocks();
        return blocks.stream()
                .map(Block::getId)
                .distinct()
                .collect(Collectors.toList());
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ConfigInterface#getBlockIds()
     */
    @Override
    public List<String> getBlockIds(String serviceId) throws RemoteException {
        if (serviceId == null) {
            return getBlockIds();
        }

        var blocks = Core.getInstance().getDbConfig().getBlocks(serviceId);
        return blocks.stream()
                .map(Block::getId)
                .distinct()
                .collect(Collectors.toList());
    }
}
