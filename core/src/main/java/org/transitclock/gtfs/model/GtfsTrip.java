/* (C)2023 */
package org.transitclock.gtfs.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.config.StringConfigValue;
import org.transitclock.configData.GtfsConfig;
import org.transitclock.utils.csv.CsvBase;

/**
 * A GTFS trip object as read from the trips.txt GTFS file.
 *
 * @author SkiBu Smith
 */
@Getter
@ToString
public class GtfsTrip extends CsvBase {

    private final String routeId;
    private final String serviceId;
    private final String tripId;
    /**
     * -- GETTER --
     *
     * @return trip_headsign from trips.txt. This element is optional so can return null.
     */
    private final String tripHeadsign;

    private final String tripShortName;
    private final String directionId;
    private final String blockId;
    private final String shapeId;
    private final Integer wheelchairAccessible;
    private final Integer bikesAllowed;

    public GtfsTrip(
            String routeId,
            String serviceId,
            String tripId,
            String tripHeadsign,
            String tripShortName,
            String directionId,
            String blockId,
            String shapeId) {
        this.routeId = routeId;
        this.serviceId = serviceId;
        this.tripId = tripId;
        this.tripHeadsign = tripHeadsign;
        this.tripShortName = tripShortName;
        this.directionId = directionId;
        this.blockId = blockId;
        this.shapeId = shapeId;

        this.wheelchairAccessible = null;
        this.bikesAllowed = null;
    }

    public GtfsTrip(CSVRecord record, boolean supplemental, String fileName) {
        super(record, supplemental, fileName);

        routeId = getRequiredUnlessSupplementalValue(record, "route_id");
        serviceId = getRequiredUnlessSupplementalValue(record, "service_id");
        tripId = getRequiredUnlessSupplementalValue(record, "trip_id");
        tripHeadsign = getOptionalValue(record, "trip_headsign");

        // Trip short name is a bit more complicated. For a regular
        // non-supplemental trips.txt file want to use the trip_short_name
        // if it is specified but otherwise use the trip_name or use a
        // regular expression on the trip_id to determine it. Hence,
        // getTripShortName() is called to determine the trip short name
        // for a non-supplemental file. But for a supplemental file only
        // want to use a trip_short_name if it is specified. This way won't
        // overwrite the trip short name from the regular trips.txt file
        // unless the trip_short_name is explicitly specified in the
        // supplemental trips.txt file.
        tripShortName = supplemental
                ? getOptionalValue(record, "trip_short_name")
                : getTripShortName(getOptionalValue(record, "trip_short_name"), tripId);

        directionId = getOptionalValue(record, "direction_id");
        blockId = getBlockId(getOptionalValue(record, "block_id"));
        shapeId = getOptionalValue(record, "shape_id");
        String wheelchairAccessibleStr = getOptionalValue(record, "wheelchair_accessible");
        wheelchairAccessible = wheelchairAccessibleStr == null ? null : Integer.parseInt(wheelchairAccessibleStr);
        String bikesAllowedStr = getOptionalValue(record, "bikes_allowed");
        bikesAllowed = bikesAllowedStr == null ? null : Integer.parseInt(bikesAllowedStr);
    }

    public GtfsTrip(
            String routeId,
            String serviceId,
            String tripId,
            String tripHeadsign,
            String tripShortName,
            String directionId,
            String blockId,
            String shapeId,
            Integer wheelchairAccessible,
            Integer bikesAllowed) {
        super();
        this.routeId = routeId;
        this.serviceId = serviceId;
        this.tripId = tripId;
        this.tripHeadsign = tripHeadsign;
        this.tripShortName = tripShortName;
        this.directionId = directionId;
        this.blockId = blockId;
        this.shapeId = shapeId;
        this.wheelchairAccessible = wheelchairAccessible;
        this.bikesAllowed = bikesAllowed;
    }

    /**
     * When combining a regular trip with a supplemental trip need to create a whole new object
     * since this class is Immutable to make it safer to use.
     *
     * @param originalTrip
     * @param supplementTrip
     */
    public GtfsTrip(GtfsTrip originalTrip, GtfsTrip supplementTrip) {
        super(originalTrip);

        // Use short variable names

        tripId = supplementTrip.tripId == null ? originalTrip.tripId : supplementTrip.tripId;
        routeId = supplementTrip.routeId == null ? originalTrip.routeId : supplementTrip.routeId;
        serviceId = supplementTrip.serviceId == null ? originalTrip.serviceId : supplementTrip.serviceId;
        tripHeadsign = supplementTrip.tripHeadsign == null ? originalTrip.tripHeadsign : supplementTrip.tripHeadsign;
        tripShortName =
                supplementTrip.tripShortName == null ? originalTrip.tripShortName : supplementTrip.tripShortName;
        directionId = supplementTrip.directionId == null ? originalTrip.directionId : supplementTrip.directionId;
        blockId = supplementTrip.blockId == null ? originalTrip.blockId : supplementTrip.blockId;
        shapeId = supplementTrip.shapeId == null ? originalTrip.shapeId : supplementTrip.shapeId;
        wheelchairAccessible = supplementTrip.wheelchairAccessible == null
                ? originalTrip.wheelchairAccessible
                : supplementTrip.wheelchairAccessible;
        bikesAllowed = supplementTrip.bikesAllowed == null ? originalTrip.bikesAllowed : supplementTrip.bikesAllowed;
    }

    /**
     * Creates a GtfsTrip object but only with the tripShortName and blockId set. This is useful for
     * creating a supplemental trips.txt file that contains only block ID information.
     *
     * @param tripShortName
     * @param blockId
     */
    public GtfsTrip(String tripShortName, String blockId) {
        // Creating supplemental data so can call default constructor
        // since line number, filename, etc are not valid.
        super();

        this.routeId = null;
        this.serviceId = null;
        this.tripId = null;
        this.tripHeadsign = null;
        this.tripShortName = tripShortName;
        this.directionId = null;
        this.blockId = blockId;
        this.shapeId = null;
        this.wheelchairAccessible = null;
        this.bikesAllowed = null;
    }

    /**
     * Many agencies don't specify a trip_short_name. For these use the trip_id or if the
     * transitclock.gtfs.tripShortNameRegEx is set to determine a group then use that group in the
     * tripId. For example, if tripId is "345-long unneeded description" and the regex is set to
     * "(.*?)-" then returned trip short name will be 345.
     *
     * @param tripShortName
     * @param tripId
     * @return The tripShortName if it is not null. Other returns a the first group specified by the
     *     regex on tripId, or the tripId if no regex defined or if no match.
     */
    private static String getTripShortName(String tripShortName, String tripId) {
        // If tripShortName provided then use it
        if (tripShortName != null) return tripShortName;

        // The tripShortName wasn't provided. If a regular expression specified
        // then use it. If regex not specified then return tripId.
        if (GtfsConfig.tripShortNameRegEx.getValue() == null) return tripId;

        // Initialize the pattern if need to, but do so just once
        if (GtfsConfig.tripShortNameRegExPattern == null)
            GtfsConfig.tripShortNameRegExPattern = Pattern.compile(GtfsConfig.tripShortNameRegEx.getValue());

        // Create the matcher
        Matcher m = GtfsConfig.tripShortNameRegExPattern.matcher(tripId);

        // If insufficient match then return tripId
        if (!m.find()) return tripId;
        if (m.groupCount() < 1) return tripId;

        // Return the first group. Note: group #0 is the entire string. Need to
        // use group #1 for the group match.
        return m.group(1);
    }

    /**
     * In case block IDs from GTFS needs to be modified to match block IDs from AVL feed. Uses the
     * property transitclock.gtfs.blockIdRegEx if it is not null.
     *
     * @param originalBlockId
     * @return the processed block ID
     */
    public static String getBlockId(String originalBlockId) {
        // If nothing to do then return original value
        if (originalBlockId == null) return originalBlockId;
        if (GtfsConfig.blockIdRegEx.getValue() == null) return originalBlockId;

        // Initialize the pattern if need to, but do so just once
        if (GtfsConfig.blockIdRegExPattern == null)
            GtfsConfig.blockIdRegExPattern = Pattern.compile(GtfsConfig.blockIdRegEx.getValue());

        // Create the matcher
        Matcher m = GtfsConfig.blockIdRegExPattern.matcher(originalBlockId);

        // If insufficient match return original value
        if (!m.find() || m.groupCount() < 1) return originalBlockId;

        // Return the first group. Note: group #0 is the entire string. Need to
        // use group #1 for the group match.
        return m.group(1);
    }
}
