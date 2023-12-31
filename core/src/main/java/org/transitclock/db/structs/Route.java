/* (C)2023 */
package org.transitclock.db.structs;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.transitclock.applications.Core;
import org.transitclock.gtfs.TitleFormatter;
import org.transitclock.gtfs.gtfsStructs.GtfsRoute;
import org.transitclock.utils.OrderedCollection;
import org.transitclock.utils.StringUtils;

/**
 * For storing in db information for a route. Based on GTFS information from routes.txt and other
 * files.
 *
 * @author SkiBu Smith
 */
@Entity
@DynamicUpdate
@EqualsAndHashCode
@ToString
@Getter
@Setter
@Table(name = "Routes")
@Slf4j
public class Route implements Serializable {

    @Id
    @Column
    private final int configRev;

    @Id
    @Column(length = 60)
    private final String id;

    @Column(length = 10)
    private final String color;

    @Column(length = 10)
    private final String textColor;

    // Not declared final because need to set route order for all
    // routes that did not have the order configured in the db,
    // but can only do so once all routes read in and sorted.
    @Column
    private Integer routeOrder;

    @Column
    private final boolean hidden;

    @Column(length = 2)
    private final String type;

    @Column(length = 1024)
    private final String description;

    // Directly from GTFS data
    @Column
    private final String shortName;

    // Directly from GTFS data
    @Column
    private final String longName;

    // Processed name combing the GTFS route_short_name and route_long_name
    @Column
    private final String name;

    @Embedded
    private final Extent extent;

    // Optional parameter that specifies how far away AVL report can be
    // from segments and still be considered a match.
    @Column
    private final Double maxDistance;

    // Later will probably want to store this in database,
    // but not yet sure. This means it is not available to application!
    @Transient
    private final List<TripPattern> tripPatternsForRoute;

    // For getStops()
    @Transient
    @EqualsAndHashCode.Exclude
    private Collection<Stop> stops = null;

    // For getPathSegments()
    @Transient
    @EqualsAndHashCode.Exclude
    private Collection<Vector> stopPaths = null;

    // For getOrderedStopsByDirection()
    @Transient
    @EqualsAndHashCode.Exclude
    private Map<String, List<String>> orderedStopsPerDirectionMap = null;

    // For getStopOrder().
    // Keeps track of stop order for each direction. Keyed on direction id.
    // The submap is keyed on stop id and contains list of stop orders.
    // Need a list since a stop can be in a trip multiple times.
    @Transient
    @EqualsAndHashCode.Exclude
    private Map<String, Map<String, List<Integer>>> stopOrderByDirectionMap = null;

    public Route(
            int configRev, GtfsRoute gtfsRoute, List<TripPattern> tripPatternsForRoute, TitleFormatter titleFormatter) {
        // Because will be writing data to the sandbox in the db
        this.configRev = configRev;

        // Here are most of the params from GtfsRoute
        this.id = gtfsRoute.getRouteId();
        this.color = gtfsRoute.getRouteColor();
        this.textColor = gtfsRoute.getRouteTextColor();
        this.routeOrder = gtfsRoute.getRouteOrder();
        this.hidden = gtfsRoute.getHidden();
        this.type = gtfsRoute.getRouteType();
        this.description = gtfsRoute.getRouteDesc();

        this.longName = titleFormatter.processTitle(gtfsRoute.getRouteLongName());

        // Handle short name specially. route_short_name is optional
        // but Transitime uses it as an identifier since route_ids
        // are not always consistent across schedule changes. Therefore
        // if the GTFS route_short_name is not set then use the
        // route_long_name.
        String shortName = gtfsRoute.getRouteShortName();
        this.shortName = shortName != null && !shortName.isEmpty() ? shortName : gtfsRoute.getRouteLongName();

        // Get the name of the route. Need to do some fancy processing here because
        // need to fix the capitalization using the TitleFormatter. This also
        // does all the regex processing to fix other issues. Might also need
        // to combine short and long names into a single name.
        if (gtfsRoute.getRouteLongName() != null
                && !gtfsRoute.getRouteLongName().isEmpty()) {
            // route_long_name is set so use it

            // Prepend the route short name plus a " - ", but only if
            // route short name is defined and it is short. This way
            // will end up with a route name like "38 - Geary" but
            // not "HYDE-POWELL - Hyde Powell". For LA Metro some route short
            // names to prepend are something like "51/52/352" so need to go up
            // to 9 characters. Also, only prepend the route short name if
            // the route long name doesn't already contain it.
            String shortNameComponent = "";
            if (gtfsRoute.getRouteShortName() != null
                    && gtfsRoute.getRouteShortName().length() <= 9
                    && !this.longName.contains(gtfsRoute.getRouteShortName()))
                shortNameComponent = gtfsRoute.getRouteShortName() + " - ";

            this.name = shortNameComponent + this.longName;
        } else {
            // route_long_name not set so just use the route_short_name
            this.name = titleFormatter.processTitle(this.shortName);
        }

        this.tripPatternsForRoute = tripPatternsForRoute;
        this.maxDistance = gtfsRoute.getMaxDistance();

        // Determine the extent of the route by looking at the extent
        // of all of the trip patterns.
        this.extent = new Extent();
        for (TripPattern tp : tripPatternsForRoute) {
            this.extent.add(tp.getExtent());
        }
    }

    /** Needed because Hibernate requires no-arg constructor */
    @SuppressWarnings("unused")
    protected Route() {
        configRev = -1;
        id = null;
        color = null;
        textColor = null;
        routeOrder = null;
        hidden = false;
        type = null;
        description = null;
        shortName = null;
        longName = null;
        name = null;
        extent = null;
        tripPatternsForRoute = null;
        maxDistance = null;
    }

    /**
     * Deletes rev from the Routes table
     *
     * @param session
     * @param configRev
     * @return Number of rows deleted
     * @throws HibernateException
     */
    public static int deleteFromRev(Session session, int configRev) throws HibernateException {
        // Note that hql uses class name, not the table name
        return session.createQuery("DELETE Route WHERE configRev=:configRev")
                .setParameter("configRev", configRev)
                .executeUpdate();
    }

    // For dealing with route order
    private static final int BEGINNING_OF_LIST_ROUTE_ORDER = 1000;
    private static final int END_OF_LIST_ROUTE_ORDER = 1000000;

    private boolean atBeginning() {
        return routeOrder != null && routeOrder < BEGINNING_OF_LIST_ROUTE_ORDER;
    }

    private boolean atEnd() {
        return routeOrder != null && END_OF_LIST_ROUTE_ORDER >= 1000000;
    }

    /**
     * Comparator for sorting Routes into proper order.
     *
     * <p>If routeOrder is set and is below 1,000 then the route will be at beginning of list and
     * will be ordered by routeOrder. If routeOrder is set and is above 1,000,000 then route will be
     * put at end of list and will be ordered by routeOrder. If routeOrder is not set then will
     * order by route short name. If route short name starts with numbers it will be padded by zeros
     * so that proper numerical order will be used.
     */
    public static final Comparator<Route> routeComparator = new Comparator<Route>() {
        /** Returns negative if r1<r2, zero if r1=r2, and positive if r1>r2 */
        @Override
        public int compare(Route r1, Route r2) {
            // Handle if routeOrder indicates r1 should be at beginning of list
            if (r1.atBeginning()) {
                // If r2 also at beginning and it should be before r1...
                if (r2.atBeginning() && r1.getRouteOrder() > r2.getRouteOrder()) return 1;
                else return -1;
            }

            // Handle if routeOrder indicates r1 should be at end of list
            if (r1.atEnd()) {
                // If r2 also at end and it should be after r1...
                if (r2.atEnd() && r1.getRouteOrder() < r2.getRouteOrder()) return -1;
                else return 1;
            }

            // r1 is in the middle so check to see if r2 is at beginning or end
            if (r2.atBeginning()) return 1;
            if (r2.atEnd()) return -1;

            // Both r1 and r2 don't have a route order to order them by
            // route name
            return StringUtils.paddedName(r1.name).compareTo(StringUtils.paddedName(r2.name));
        }
    };

    /**
     * Returns List of Route objects for the specified database revision. Orders them based on the
     * GTFS route_order extension or the route short name if route_order not set.
     *
     * @param session
     * @param configRev
     * @return Map of routes keyed on routeId
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<Route> getRoutes(Session session, int configRev) throws HibernateException {
        // Get list of routes from database
        List<Route> routesList = session.createQuery("FROM Route WHERE configRev = :configRev ORDER BY routeOrder, shortName")
                .setParameter("configRev", configRev)
                .list();

        // Need to set the route order for each route so that can sort
        // predictions based on distance from stop and route order. For
        // the routes that didn't have route ordered configured in db
        // start with 1000 and count on up.
        int routeOrderForWhenNotConfigured = 1000;
        for (Route route : routesList) {
            if (!route.atBeginning() && !route.atEnd()) {
                route.setRouteOrder(routeOrderForWhenNotConfigured++);
            }
        }

        // Return the list of routes
        return routesList;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        // Don't want to output list of full TripPattern objects because
        // each TripPattern.toString() result is pretty long (list of stops,
        // extent, etc). Therefore for tripPatternsForRoute just output
        // a short version of the object.
        StringBuilder tripPatternIds = new StringBuilder("not set");
        if (tripPatternsForRoute != null) {
            tripPatternIds = new StringBuilder("[");
            for (TripPattern tp : tripPatternsForRoute) tripPatternIds.append(tp.toShortString()).append(", ");
            tripPatternIds.append("]");
        }

        return "Route ["
                + "configRev="
                + configRev
                + ", id="
                + id
                + ", name="
                + name
                + ", color="
                + color
                + ", textColor="
                + textColor
                + ", routeOrder="
                + routeOrder
                + ", hidden="
                + hidden
                + ", type="
                + type
                + ", description="
                + description
                + ", shortName="
                + shortName
                + ", longName="
                + longName
                + ", extent"
                + extent
                + ", tripPatternsForRoute="
                + tripPatternIds
                + "]";
    }

    /**
     * Returns unordered collection of stops associated with route.
     *
     * <p>Synchronized because caching stops.
     *
     * @return
     */
    public synchronized Collection<Stop> getStops() {
        // If stop collection already determined then simply return it
        if (stops != null) return stops;

        // Get the trip patterns for the route. Can't use the member
        // variable tripPatternsForRoute since it is only set when the
        // GTFS data is processed and stored in the db. Since this member
        // is transient it is not stored in the db and therefore not
        // available to this client application. But it can be obtained
        // from the DbConfig.
        List<TripPattern> tripPatternsForRoute = Core.getInstance().getDbConfig().getTripPatternsForRoute(id);

        // Stop list not yet determined so determine it now using
        // trip patterns.
        Map<String, Stop> stopMap = new HashMap<>();
        for (TripPattern tripPattern : tripPatternsForRoute) {
            for (StopPath stopPath : tripPattern.getStopPaths()) {
                String stopId = stopPath.getStopId();

                // If already added this stop then continue to next one
                if (stopMap.containsKey(stopId)) continue;

                Stop stop = Core.getInstance().getDbConfig().getStop(stopId);
                stopMap.put(stopId, stop);
            }
        }
        stops = stopMap.values();

        // Return the newly created collection of stops
        return stops;
    }

    /**
     * Returns the specified trip pattern, or null if that trip pattern doesn't exist for the route.
     *
     * @param tripPatternId
     * @return
     */
    public TripPattern getTripPattern(String tripPatternId) {
        List<TripPattern> tripPatternsForRoute =
                Core.getInstance().getDbConfig().getTripPatternsForRoute(getId());
        for (TripPattern tripPattern : tripPatternsForRoute) {
            if (tripPattern.getId().equals(tripPatternId)) return tripPattern;
        }

        // Never found the specified trip pattern
        return null;
    }

    /**
     * Returns longest trip pattern for the directionId specified. Note: gets trip patterns from
     * Core, which means it works in the core application, not just when processing GTFS data.
     *
     * @param directionId
     * @return
     */
    public TripPattern getLongestTripPatternForDirection(String directionId) {
        List<TripPattern> tripPatternsForRoute =
                Core.getInstance().getDbConfig().getTripPatternsForRoute(getId());
        TripPattern longestTripPatternForDir = null;
        for (TripPattern tripPattern : tripPatternsForRoute) {
            if (Objects.equals(tripPattern.getDirectionId(), directionId)) {
                if (longestTripPatternForDir == null || tripPattern.getNumberStopPaths() > longestTripPatternForDir.getNumberStopPaths())
                    longestTripPatternForDir = tripPattern;
            }
        }

        return longestTripPatternForDir;
    }

    /**
     * Returns the longest trip pattern for each direction ID for the route. Will typically be two
     * trip patterns since there are usually two directions per route.
     *
     * @return
     */
    public List<TripPattern> getLongestTripPatternForEachDirection() {
        List<TripPattern> tripPatterns = new ArrayList<>();

        List<String> directionIds = getDirectionIds();
        for (String directionId : directionIds) tripPatterns.add(getLongestTripPatternForDirection(directionId));

        return tripPatterns;
    }

    /**
     * Returns list of trip patterns for the directionId specified.
     *
     * @param directionId
     * @return
     */
    public List<TripPattern> getTripPatterns(String directionId) {
        List<TripPattern> tripPatternsForRoute =
                Core.getInstance().getDbConfig().getTripPatternsForRoute(getId());
        List<TripPattern> tripPatternsForDir = new ArrayList<>();
        for (TripPattern tripPattern : tripPatternsForRoute) {
            if (Objects.equals(tripPattern.getDirectionId(), directionId)) tripPatternsForDir.add(tripPattern);
        }

        return tripPatternsForDir;
    }

    /**
     * Returns list of direction IDs for the route.
     *
     * @return
     */
    public List<String> getDirectionIds() {
        List<String> directionIds = new ArrayList<String>();
        List<TripPattern> tripPatternsForRoute =
                Core.getInstance().getDbConfig().getTripPatternsForRoute(getId());
        if (tripPatternsForRoute == null) return directionIds;
        for (TripPattern tripPattern : tripPatternsForRoute) {
            String directionId = tripPattern.getDirectionId();
            if (!directionIds.contains(directionId)) directionIds.add(directionId);
        }
        return directionIds;
    }

    /**
     * Returns unordered collection of path vectors associated with route
     *
     * @return
     */
    public synchronized Collection<Vector> getPathSegments() {
        // If stop paths collection already determined then simply return it
        if (stopPaths != null) return stopPaths;

        // Get the trip patterns for the route. Can't use the member
        // variable tripPatternsForRoute since it is only set when the
        // GTFS data is processed and stored in the db. Since this member
        // is transient it is not stored in the db and therefore not
        // available to this client application. But it can be obtained
        // from the DbConfig.
        List<TripPattern> tripPatternsForRoute =
                Core.getInstance().getDbConfig().getTripPatternsForRoute(id);

        Map<String, StopPath> stopPathMap = new HashMap<String, StopPath>();
        for (TripPattern tripPattern : tripPatternsForRoute) {
            for (StopPath stopPath : tripPattern.getStopPaths()) {
                String stopPathId = stopPath.getId();

                // If already added this stop then continue to next one
                if (stopPathMap.containsKey(stopPathId)) continue;

                stopPathMap.put(stopPathId, stopPath);
            }
        }

        // For each of the unique stop paths add the vectors to the collection
        stopPaths = new ArrayList<Vector>(stopPathMap.values().size());
        for (StopPath stopPath : stopPathMap.values()) {
            stopPaths.addAll(stopPath.getSegmentVectors());
        }

        // Return the newly created collection of stop paths
        return stopPaths;
    }

    /**
     * For each GTFS direction ID returns list of stops that in the appropriate order for the
     * direction. The appropriate order means that when there are different trip patterns that the
     * stops that are different will be inserted appropriately into the list. Synchronized since can
     * be access through multiple threads.
     *
     * @return Map keyed by direction ID and value of List of ordered stop IDs.
     */
    public synchronized Map<String, List<String>> getOrderedStopsByDirection() {
        // If already determined the stops return the cached map
        if (orderedStopsPerDirectionMap != null) return orderedStopsPerDirectionMap;

        // Haven't yet determined ordered stops so do so now
        orderedStopsPerDirectionMap = new HashMap<>();

        // For each direction
        for (String directionId : getDirectionIds()) {
            // Determine ordered collection of stops for direction
            OrderedCollection orderedCollection = new OrderedCollection();
            List<TripPattern> tripPatternsForDir = getTripPatterns(directionId);
            List<List<String>> stopIdsForTripPatternList = new ArrayList<>();
            for (TripPattern tripPattern : tripPatternsForDir) {
                List<String> stopIdsForTripPattern = tripPattern.getStopIds();
                stopIdsForTripPatternList.add(stopIdsForTripPattern);
            }
            orderedCollection.add(stopIdsForTripPatternList);

            orderedStopsPerDirectionMap.put(directionId, orderedCollection.get());
        }

        return orderedStopsPerDirectionMap;
    }

    /**
     * Initializes stopOrderByDirectionMap member if haven't done so yet. Used by getStopOrder().
     * Synchronized since can be access through multiple threads.
     */
    private synchronized void createStopOrderByDirectionMapIfNeedTo() {
        // If map already created then done
        if (stopOrderByDirectionMap != null) return;

        // Map not yet created so create it now
        stopOrderByDirectionMap = new HashMap<String, Map<String, List<Integer>>>();
        Map<String, List<String>> orderedStopsByDirection = getOrderedStopsByDirection();

        // Create submap for each direction Id
        for (String directionId : orderedStopsByDirection.keySet()) {
            // Create the submap for the direction ID. Map is keyed on stop Id
            // and contains list of stop order for the stop. Need a list since
            // a stop can be in a trip multiple times.
            Map<String, List<Integer>> stopOrderMap = new HashMap<>();
            stopOrderByDirectionMap.put(directionId, stopOrderMap);

            // Add each stop for the direction Id
            List<String> orderedStopsForDirectionList = orderedStopsPerDirectionMap.get(directionId);
            for (int stopOrder = 0; stopOrder < orderedStopsForDirectionList.size(); ++stopOrder) {
                // Determine stopId (we already have stopOrder)
                String stopId = orderedStopsForDirectionList.get(stopOrder);

                // Get list of stop orders for the stop Id
                List<Integer> stopOrderListForStopId = stopOrderMap.computeIfAbsent(stopId, k -> new ArrayList<Integer>(1));

                // For the stopId add the stop order to its list of stop orders
                stopOrderListForStopId.add(stopOrder);
            }
        }
    }

    /**
     * Gets the order for the stop. Important when have multiple trip patterns for a direction.
     * Allows each stop in the trip to have a unique sequential stop order so that can save the stop
     * order in the db and then use it for db queries to order the stop data for a direction for a
     * route.
     *
     * @param directionId
     * @param stopId
     * @param stopIndex so that can handle stop being on trip multiple times
     * @return the stop order for the stop for the direction for the route
     */
    public int getStopOrder(String directionId, String stopId, int stopIndex) {
        // Make sure initialized
        createStopOrderByDirectionMapIfNeedTo();

        // For the direction get the map of stop orders
        Map<String, List<Integer>> stopOrderMap = stopOrderByDirectionMap.get(directionId);
        if (stopOrderMap == null) {
            logger.error("In Route.getStopOrder() directionId={} is not valid " + "for routeId={}.", directionId, id);
            return -1;
        }

        // For the stop Id for the direction get the list of stop orders
        List<Integer> stopOrderListForStopId = stopOrderMap.get(stopId);
        if (stopOrderListForStopId == null) {
            logger.error(
                    "In Route.getStopOrder() stopId={} is not valid " + "for routeId={} directionId={}.",
                    stopId,
                    id,
                    directionId);
            return -1;
        }

        // Return the appropriate stop order. In order to handle situation
        // where stop might be in trip multiple times need to make sure the
        // stop order is greater than the stop index.
        for (int stopOrder : stopOrderListForStopId) {
            if (stopOrder >= stopIndex) return stopOrder;
        }

        // Didn't find appropriate stop order
        logger.error(
                "In Route.getStopOrder() did not find stop order for "
                        + "for routeId={} directionId={} stopId={} stopIndex={}.",
                id,
                directionId,
                stopId,
                stopIndex);
        return -1;
    }

    /**
     * The short name is either specified by route_short_name in the routes.txt GTFS file or if that
     * is null it will be the long name name.
     *
     * @return the short name for the route
     */
    public String getShortName() {
        return shortName != null ? shortName : name;
    }

    /**
     * The long name is either specified by route_long_name in the routes.txt GTFS file or if that
     * is null it will be the short name name.
     *
     * @return the long name for the route
     */
    public String getLongName() {
        return longName != null ? longName : name;
    }

    /**
     * For specifying on a per route basis how far AVL report can be from segment and still have it
     * be considered a match.
     *
     * @return the max distance if set, otherwise NaN
     */
    public double getMaxAllowableDistanceFromSegment() {
        if (maxDistance != null) return maxDistance;
        else return Double.NaN;
    }

}
