/* (C)2023 */
package org.transitclock.db.structs;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import net.jcip.annotations.Immutable;
import org.transitclock.utils.Geo;

/**
 * Defines a latitude longitude pair that together specify a location.
 *
 * @author SkiBu Smith
 */
@Immutable
@Embeddable
public class Location implements Serializable {

    @Column
    private final double lat;

    @Column
    private final double lon;

    // Hibernate requires this object to be serializable since it is
    // store in a blob when it is contained in an array.
    private static final long serialVersionUID = -5500471845805265340L;

    /********************** Member Functions **************************/
    public Location(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    /** Hibernate requires a no-arg constructor */
    @SuppressWarnings("unused")
    private Location() {
        lat = 0.0;
        lon = 0.0;
    }

    /**
     * If don't have hashCode() and equals() then the objects that include this object will generate
     * a warning when these methods are implemented.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(lat);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(lon);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * If don't have hashCode() and equals() then the objects that include this object will generate
     * a warning when these methods are implemented.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Location other = (Location) obj;
        if (Double.doubleToLongBits(lat) != Double.doubleToLongBits(other.lat)) return false;
        if (Double.doubleToLongBits(lon) != Double.doubleToLongBits(other.lon)) return false;
        return true;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
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

    /**
     * Outputs only "[lat, lon]". This is a bit different from all the other toString() methods but
     * the nice thing is that then one can just cut & paste a location into a map in order to
     * visualize it.
     */
    public String toString() {
        return "[" + Geo.format(lat) + ", " + Geo.format(lon) + "]";
    }
}
