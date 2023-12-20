/* (C)2023 */
package org.transitclock.core.domain;

import lombok.Data;
import org.transitclock.utils.Geo;

import java.io.Serializable;

/**
 * Defines a latitude longitude pair that together specify a location.
 *
 * @author SkiBu Smith
 */
@Data
public class Location implements Serializable {

    private final double lat;

    private final double lon;

    public Location(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double distance(Location l2) {
        return Geo.distance(this, l2);
    }

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
