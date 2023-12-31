/* (C)2023 */
package org.transitclock.db.structs;

import org.transitclock.utils.Geo;

/**
 * Inherits from Vector but automatically calculates the heading. Useful for if heading is
 * frequently used.
 *
 * @author SkiBu Smith
 */
public class VectorWithHeading extends Vector {

    // Heading in degrees clockwise from due North. Note that this is
    // not the same as the "angle" which is degrees clockwise from the
    // equator.
    private final float headingInDegrees;

    /**
     * Construct a vector and determine its heading. Heading will be between -180 and 180 degrees.
     *
     * @param l1
     * @param l2
     */
    public VectorWithHeading(Location l1, Location l2) {
        super(l1, l2);
        float heading = (float) heading();
        if (heading < 0.0) {
            heading += 360.0;
        }
        headingInDegrees = heading;
    }

    /**
     * @return Heading in degrees clockwise from due North. Note that this is not the same as the
     *     "angle" which is degrees clockwise from the equator. The value is calculated in the
     *     constructor so that this method is efficient if the heading is retrieved multiple times
     *     for a vector.
     */
    public float getHeading() {
        return headingInDegrees;
    }

    /**
     * Returns true if heading is within allowableDelta of segment.
     *
     * @param vehicleHeading Heading of vehicle. If Float.NaN then method will return true
     * @param allowableDelta
     * @return Whether heading is within allowableDelta of segment
     */
    public boolean headingOK(float vehicleHeading, float allowableDelta) {
        return Geo.headingOK(vehicleHeading, headingInDegrees, allowableDelta);
    }

    @Override
    public String toString() {
        return "VectorWithHeading [" + "l1="
                + l1 + ", " + "l2="
                + l2 + ", " + "headingInDegrees="
                + headingInDegrees + ", " + "length="
                + length() + "]";
    }
}
