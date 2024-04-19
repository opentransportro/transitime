/* (C)2023 */
package org.transitclock.service.contract;

import org.transitclock.domain.structs.Agency;
import org.transitclock.service.dto.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Defines the RMI interface for getting configuration data.
 *
 * @author SkiBu Smith
 */
public interface ConfigInterface {

    /**
     * Obtains ordered list of route summaries.
     *
     * @return
     */
    Collection<IpcRouteSummary> getRoutes();

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
     */
    IpcRoute getRoute(String routeIdOrShortName, String directionId, String stopId, String tripPatternId)
           ;

    /**
     * Obtains ordered list of route details
     *
     * @param routeIdsOrShortNames
     * @return
     */
    List<IpcRoute> getRoutes(List<String> routeIdsOrShortNames);

    /**
     * Returns stops for each direction for a route.
     *
     * @param routeIdOrShortName Specifies which route to provide data for. routeShortName is often
     *     used instead of routeId since routeIds unfortunately often change when there is a
     *     schedule change.
     * @return
     */
    IpcDirectionsForRoute getStops(String routeIdOrShortName);

    /**
     * Returns block info for specified blockId and serviceId. Includes all trip and trip pattern
     * info associated with the block.
     *
     * @param blockId
     * @param serviceId
     * @return
     */
    IpcBlock getBlock(String blockId, String serviceId);

    /**
     * Returns blocks for each service class for the blockId specified.
     *
     * @param blockId
     * @return
     */
    Collection<IpcBlock> getBlocks(String blockId);

    /**
     * Returns trip info for specified tripId. If trip with the specified trip_id is not found then
     * looks for a trip with a trip_short_name that matches the tripId. This way can easily find
     * trips using trip_short_name, which for agencies such as MBTA commuter rail is more frequently
     * used. Includes all trip pattern info associated with the trip.
     *
     * @param tripId The GTFS trip_id or trip_short_name
     * @return The IpcTrip for interprocess communication
     */
    IpcTrip getTrip(String tripId);

    /**
     * Returns trip patterns for specified routeIdOrShortName.
     *
     * @param routeIdOrShortName
     * @return
     */
    List<IpcTripPattern> getTripPatterns(String routeIdOrShortName);

    /**
     * Returns list of IpcSchedule objects for the specified routeIdOrShortName
     *
     * @param routeIdOrShortName
     * @return
     */
    List<IpcSchedule> getSchedules(String routeIdOrShortName);

    /**
     * Returns list of Agency objects containing data from GTFS agency.txt file
     *
     * @return
     */
    List<Agency> getAgencies();

    /**
     * Returns list of Calendar objects that are currently active.
     *
     * @return
     */
    List<IpcCalendar> getCurrentCalendars();

    /**
     * Returns list of all Calendar objects.
     *
     * @return
     */
    List<IpcCalendar> getAllCalendars();

    /**
     * Returns list of vehicle IDs, unsorted
     *
     * @return vehicle IDs
     */
    List<String> getVehicleIds();

    /**
     * Returns list of service IDs, unsorted
     *
     * @return vehicle IDs
     */
    List<String> getServiceIds();

    /**
     * Returns list of service IDs, unsorted
     *
     * @return vehicle IDs
     */
    List<String> getCurrentServiceIds();

    /**
     * Returns list of trip IDs, unsorted
     *
     * @return vehicle IDs
     */
    List<String> getTripIds();

    /**
     * Returns list of block IDs, unsorted, duplicates removed
     *
     * @return vehicle IDs
     */
    List<String> getBlockIds();

    /**
     * Returns list of block IDs for specified serviceId, unsorted
     *
     * @param serviceId If null then all block IDs are returned
     * @return vehicle IDs
     */
    List<String> getBlockIds(String serviceId);

    /**
     * Returns sorted lists of block IDs what belong to all service IDs
     *
     * @return Map of service IDs with belong block IDs
     */
    Map<String, List<String>> getServiceIdsWithBlockIds();


    /**
     * Obtains list of routes which contained stops with following stop ID
     *
     * @param stopId
     * @return list of IpcRoute
     */
    List<IpcRoute> getRoutesByStopId(String stopId);
}
