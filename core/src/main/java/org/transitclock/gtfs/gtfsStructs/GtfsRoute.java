/* (C)2023 */
package org.transitclock.gtfs.gtfsStructs;

import javax.annotation.concurrent.Immutable;

import lombok.Getter;
import lombok.ToString;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.utils.csv.CsvBase;

/**
 * A GTFS routes object.
 *
 * <p>Contains two extension columns beyond the GTFS spec. These are rout.order and hidden. They are
 * useful for the user interface. They will usually be set using a supplemental routes file.
 *
 * @author SkiBu Smith
 */
@ToString
@Getter
@Immutable
public class GtfsRoute extends CsvBase {

    private final String routeId;
    private final String agencyId;
    private final String routeShortName;
    private final String routeLongName;
    private final String routeDesc;
    private final String routeType;
    private final String routeURL;
    private final String routeColor;
    private final String routeTextColor;
    // Extensions to GTFS spec:
    // routeOrder is used to order the routes. It is optional. If set to below
    // 1,000 then the route will be ordered by route order with with this route
    // at the beginning. If greater than 1,000,000 then the route will be at
    // the end of the list.
    private final Integer routeOrder;
    private final Boolean hidden;
    private final Boolean remove;
    private final String unscheduledBlockSuffix;
    private final String parentRouteId;
    private final Integer breakTime;
    private final Double maxDistance;

    /** For creating a GtfsStop object from scratch. */
    public GtfsRoute(
            String routeId,
            String agencyId,
            String routeShortName,
            String routeLongName,
            String routeType,
            String routeColor,
            String routeTextColor) {
        this.routeId = routeId;
        this.agencyId = agencyId;
        this.routeShortName = routeShortName;
        this.routeLongName = routeLongName;
        this.routeType = routeType;
        this.routeColor = routeColor;
        this.routeTextColor = routeTextColor;

        // Bunch of optional parameters are simply set to null
        this.routeDesc = null;
        this.routeURL = null;
        this.routeOrder = null;
        this.hidden = null;
        this.remove = null;
        this.unscheduledBlockSuffix = null;
        this.parentRouteId = null;
        this.breakTime = null;
        this.maxDistance = null;
    }

    /**
     * Creates a GtfsRoute object by reading the data from the CSVRecord.
     *
     * @param record
     * @param supplementalFile
     * @param fileName for logging errors
     */
    public GtfsRoute(CSVRecord record, boolean supplementalFile, String fileName) {
        super(record, supplementalFile, fileName);

        routeId = getRequiredUnlessSupplementalValue(record, "route_id");
        agencyId = getOptionalValue(record, "agency_id");
        routeShortName = getOptionalValue(record, "route_short_name");
        routeLongName = getOptionalValue(record, "route_long_name");
        routeDesc = getOptionalValue(record, "route_desc");
        routeType = getRequiredUnlessSupplementalValue(record, "route_type");
        routeURL = getOptionalValue(record, "route_url");
        routeColor = getOptionalValue(record, "route_color");
        routeTextColor = getOptionalValue(record, "route_text_color");

        // Note: route_order is an extra column not defined in GTFS spec.
        // It is useful for supplemental files because it enables the routes
        // to be ordered in a more user friendly way. Transit agencies often
        // don't order the routes in the routes.txt file.
        routeOrder = getOptionalIntegerValue(record, "route_order");

        // hidden is also an extra column not defined in GTFS spec.
        // Useful for supplemental files because allows one to hide
        // a particular stop from the public.
        hidden = getOptionalBooleanValue(record, "hidden");

        remove = getOptionalBooleanValue(record, "remove");

        unscheduledBlockSuffix = getOptionalValue(record, "unscheduled_block_suffix");

        parentRouteId = getOptionalValue(record, "parent_route_id");

        breakTime = getOptionalIntegerValue(record, "break_time");

        maxDistance = getOptionalDoubleValue(record, "max_distance");
    }

    /**
     * When combining a regular route with a supplemental route need to create a whole new object
     * since this class is Immutable to make it safer to use.
     *
     * @param originalRoute
     * @param supplementRoute
     */
    public GtfsRoute(GtfsRoute originalRoute, GtfsRoute supplementRoute) {
        super(originalRoute);

        // Use short variable names
        GtfsRoute o = originalRoute;
        GtfsRoute s = supplementRoute;

        routeId = originalRoute.routeId;
        agencyId = s.agencyId == null ? o.agencyId : s.agencyId;
        routeShortName = s.routeShortName == null ? o.routeShortName : s.routeShortName;
        routeLongName = s.routeLongName == null ? o.routeLongName : s.routeLongName;
        routeDesc = s.routeDesc == null ? o.routeDesc : s.routeDesc;
        routeType = s.routeType == null ? o.routeType : s.routeType;
        routeURL = s.routeURL == null ? o.routeURL : s.routeURL;
        routeColor = s.routeColor == null ? o.routeColor : s.routeColor;
        routeTextColor = s.routeTextColor == null ? o.routeTextColor : s.routeTextColor;
        routeOrder = s.routeOrder == null ? o.routeOrder : s.routeOrder;
        hidden = s.hidden == null ? o.hidden : s.hidden;
        remove = s.remove == null ? o.remove : s.remove;
        unscheduledBlockSuffix = s.unscheduledBlockSuffix == null ? o.unscheduledBlockSuffix : s.unscheduledBlockSuffix;
        parentRouteId = s.parentRouteId == null ? o.parentRouteId : s.parentRouteId;
        breakTime = s.breakTime == null ? o.breakTime : s.breakTime;
        maxDistance = s.maxDistance == null ? o.maxDistance : s.maxDistance;
    }

    /**
     * Returns if route should be removed from configuration so it doesn't show up at all
     *
     * @return
     */
    public boolean shouldRemove() {
        // remove is optional so can be null. Therefore need
        // to handle specially
        return remove != null ? remove : false;
    }

    /**
     * Returns if route should be hidden from user interface for the public.
     *
     * @return
     */
    public boolean getHidden() {
        // hidden is optional so can be null. Therefore need
        // to handle specially
        return hidden != null ? hidden : false;
    }

    /**
     * Returns true if should create unscheduled block assignments for this route.
     *
     * @return
     */
    public boolean shouldCreateUnscheduledBlock() {
        return unscheduledBlockSuffix != null;
    }
}
