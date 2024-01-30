/* (C)2023 */
package org.transitclock.domain.structs;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import org.hibernate.annotations.Immutable;
import org.transitclock.utils.Geo;

/**
 * Defines a latitude longitude pair that together specify a location.
 *
 * @author SkiBu Smith
 */
@Immutable
@Embeddable
@Data
public class Location implements Serializable {

    @Column
    private final double lat;

    @Column
    private final double lon;

    public Location(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    protected Location() {
        lat = 0.0;
        lon = 0.0;
    }

    /**
     * Returns distance in meters between this location and the location l2 passed in.
     *
     * @param l2
     * @return Distance in meters
     */
    public double distance(Location l2) {
        return Geo.distance(this, l2);
    }

    /**
     * Returns distance in meters between location and the closest match to the specified Vector.
     *
     * @param v
     * @return
     */
    public double distance(Vector v) {
        return Geo.distance(this, v);
    }

    /**
     * Returns length along vector where this location is closest to the vector.
     *
     * @param v
     * @return
     */
    public double matchDistanceAlongVector(Vector v) {
        return Geo.matchDistanceAlongVector(this, v);
    }
}
