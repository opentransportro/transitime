/* (C)2023 */
package org.transitclock.db.structs;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Immutable;

/**
 * A rectangle specified by min and max latitudes and longitudes.
 *
 * @author SkiBu Smith
 */
@Immutable
@Embeddable
@EqualsAndHashCode
@ToString
@Getter
@NoArgsConstructor
public class Extent implements Serializable {

    @Column
    private double minLat = Double.POSITIVE_INFINITY;

    @Column
    private double maxLat = Double.NEGATIVE_INFINITY;

    @Column
    private double minLon = Double.POSITIVE_INFINITY;

    @Column
    private double maxLon = Double.NEGATIVE_INFINITY;

    // This value is actually dependent on latitude a bit since the earth
    // is not a perfect sphere. But it doesn't vary that much. So using
    // a hard coded value for latitude of 38 degrees, which is approximately
    // San Francisco. For Mexico City at latitude 19 degrees the difference
    // is a bit less than 0.3%, so pretty small for when doing quick
    // calculations.
    private static final double METERS_PER_DEGREE = 110996.45;

    /**
     * Once an Extent has been constructed need to simply add associated Locations (or Extents).
     * Once all locations have been added the Extent will be the rectangle spanning all of those
     * Locations and Extents.
     *
     * @param l
     */
    public void add(Location l) {
        if (l.getLat() < minLat) minLat = l.getLat();
        if (l.getLat() > maxLat) maxLat = l.getLat();
        if (l.getLon() < minLon) minLon = l.getLon();
        if (l.getLon() > maxLon) maxLon = l.getLon();
    }

    /**
     * Once an Extent has been constructed need to simply add associated Extents (or Locations).
     * Once all have been added the Extent will be the rectangle spanning all of those Locations and
     * Extents.
     *
     * @param l
     */
    public void add(Extent e) {
        if (e.minLat < minLat) minLat = e.minLat;
        if (e.maxLat > maxLat) maxLat = e.maxLat;
        if (e.minLon < minLon) minLon = e.minLon;
        if (e.maxLon > maxLon) maxLon = e.maxLon;
    }

    /**
     * Returns true if the location is with the specified distance of this extent. This is not a
     * perfectly accurate calculation due to METERS_PER_DEGREE being a constant and not taking into
     * account changes in diameter of the earth depending on latitude. Also, looks at latitude and
     * longitude separately. So there is a corner case where latitude and longitude might be OK
     * individually, so the method returns true, but together the distance would be actually be
     * further away then the specified distance.
     *
     * @param loc
     * @param distance
     * @return
     */
    public boolean isWithinDistance(Location loc, double distance) {
        // First do quick check on latitude
        double distanceInDegreesLatitude = distance / METERS_PER_DEGREE;
        if (minLat > loc.getLat() + distanceInDegreesLatitude || maxLat < loc.getLat() - distanceInDegreesLatitude)
            return false;

        // Latitude was OK so check longitude
        double distanceInDegreesLongitude =
                distance / (METERS_PER_DEGREE * Math.cos(Math.toRadians((minLat + maxLat) / 2)));
        return !(minLon > loc.getLon() + distanceInDegreesLongitude)
                && !(maxLon < loc.getLon() - distanceInDegreesLongitude);
    }
}
