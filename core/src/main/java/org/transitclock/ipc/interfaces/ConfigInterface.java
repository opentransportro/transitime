/* (C)2023 */
package org.transitclock.ipc.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import org.transitclock.db.structs.Agency;
import org.transitclock.ipc.data.IpcBlock;
import org.transitclock.ipc.data.IpcCalendar;
import org.transitclock.ipc.data.IpcDirectionsForRoute;
import org.transitclock.ipc.data.IpcRoute;
import org.transitclock.ipc.data.IpcRouteSummary;
import org.transitclock.ipc.data.IpcSchedule;
import org.transitclock.ipc.data.IpcTrip;
import org.transitclock.ipc.data.IpcTripPattern;

/**
 * Defines the RMI interface for getting configuration data.
 *
 * @author SkiBu Smith
 */
public interface ConfigInterface extends Remote {

    /**
     * Obtains ordered list of route summaries.
     *
     * @return
     * @throws RemoteException
     */
    Collection<IpcRouteSummary> getRoutes() throws RemoteException;

    /**
     * Obtains data for single route.
     *
     * @param routeIdOrShortName Specifies which route to provide data for. routeShortName is often
     *     used instead of routeId since routeIds unfortunately often change when there is a
     *     schedule change.
     * @param directionId optional. If want UI to highlight the remaining stops and paths left in
     *     the trip then can specify directionId along with the stopId. The directionId can be
     *     needed for agencies where only a single stop is used for both directions for a route.
     * @param stopId optional. If want UI to highlight the remaining stops and paths left in trip
     *     then stopId is used to return which stops remain in trip. If this additional info not
     *     needed for UI then null can be specified.
     * @param tripPatternId optional. If want UI to highlight the remaining stops and paths left in
     *     trip then stopId is used to determine which trip pattern to highlight. If this additional
     *     info not needed for UI then null can be specified.
     * @return
     * @throws RemoteException
     */
    IpcRoute getRoute(String routeIdOrShortName, String directionId, String stopId, String tripPatternId)
            throws RemoteException;

    /**
     * Obtains ordered list of route details
     *
     * @param routeIdOrShortName
     * @return
     * @throws RemoteException
     */
    List<IpcRoute> getRoutes(List<String> routeIdsOrShortNames) throws RemoteException;

    /**
     * Returns stops for each direction for a route.
     *
     * @param routeIdOrShortName Specifies which route to provide data for. routeShortName is often
     *     used instead of routeId since routeIds unfortunately often change when there is a
     *     schedule change.
     * @return
     * @throws RemoteException
     */
    IpcDirectionsForRoute getStops(String routeIdOrShortName) throws RemoteException;

    /**
     * Returns block info for specified blockId and serviceId. Includes all trip and trip pattern
     * info associated with the block.
     *
     * @param blockId
     * @param serviceId
     * @return
     * @throws RemoteException
     */
    IpcBlock getBlock(String blockId, String serviceId) throws RemoteException;

    /**
     * Returns blocks for each service class for the blockId specified.
     *
     * @param blockId
     * @return
     * @throws RemoteException
     */
    Collection<IpcBlock> getBlocks(String blockId) throws RemoteException;

    /**
     * Returns trip info for specified tripId. If trip with the specified trip_id is not found then
     * looks for a trip with a trip_short_name that matches the tripId. This way can easily find
     * trips using trip_short_name, which for agencies such as MBTA commuter rail is more frequently
     * used. Includes all trip pattern info associated with the trip.
     *
     * @param tripId The GTFS trip_id or trip_short_name
     * @return The IpcTrip for interprocess communication
     * @throws RemoteException
     */
    IpcTrip getTrip(String tripId) throws RemoteException;

    /**
     * Returns trip patterns for specified routeIdOrShortName.
     *
     * @param routeIdOrShortName
     * @return
     * @throws RemoteException
     */
    List<IpcTripPattern> getTripPatterns(String routeIdOrShortName) throws RemoteException;

    /**
     * Returns list of IpcSchedule objects for the specified routeIdOrShortName
     *
     * @param routeIdOrShortName
     * @return
     * @throws RemoteException
     */
    List<IpcSchedule> getSchedules(String routeIdOrShortName) throws RemoteException;

    /**
     * Returns list of Agency objects containing data from GTFS agency.txt file
     *
     * @return
     * @throws RemoteException
     */
    List<Agency> getAgencies() throws RemoteException;

    /**
     * Returns list of Calendar objects that are currently active.
     *
     * @return
     * @throws RemoteException
     */
    List<IpcCalendar> getCurrentCalendars() throws RemoteException;

    /**
     * Returns list of all Calendar objects.
     *
     * @return
     * @throws RemoteException
     */
    List<IpcCalendar> getAllCalendars() throws RemoteException;

    /**
     * Returns list of vehicle IDs, unsorted
     *
     * @return vehicle IDs
     * @throws RemoteException
     */
    List<String> getVehicleIds() throws RemoteException;

    /**
     * Returns list of service IDs, unsorted
     *
     * @return vehicle IDs
     * @throws RemoteException
     */
    List<String> getServiceIds() throws RemoteException;

    /**
     * Returns list of service IDs, unsorted
     *
     * @return vehicle IDs
     * @throws RemoteException
     */
    List<String> getCurrentServiceIds() throws RemoteException;

    /**
     * Returns list of trip IDs, unsorted
     *
     * @return vehicle IDs
     * @throws RemoteException
     */
    List<String> getTripIds() throws RemoteException;

    /**
     * Returns list of block IDs, unsorted, duplicates removed
     *
     * @return vehicle IDs
     * @throws RemoteException
     */
    List<String> getBlockIds() throws RemoteException;

    /**
     * Returns list of block IDs for specified serviceId, unsorted
     *
     * @param serviceId If null then all block IDs are returned
     * @return vehicle IDs
     * @throws RemoteException
     */
    List<String> getBlockIds(String serviceId) throws RemoteException;
}
