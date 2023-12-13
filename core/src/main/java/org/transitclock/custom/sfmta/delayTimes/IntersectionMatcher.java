/* (C)2023 */
package org.transitclock.custom.sfmta.delayTimes;

import java.util.List;
import org.transitclock.db.structs.Location;
import org.transitclock.db.structs.Vector;
import org.transitclock.utils.Geo;

/**
 * @author SkiBu Smith
 */
public class IntersectionMatcher {

    /********************** Member Functions **************************/
    public static void match(double lat, double lon, double allowableDistance, List<Intersection> intersections) {
        Location loc = new Location(lat, lon);
        for (Intersection i : intersections) {
            Location l1 = new Location(i.lat1, i.lon1);
            Location lStop = new Location(i.latStop, i.lonStop);
            Location l2 = new Location(i.lat2, i.lon2);

            Vector v1 = new Vector(l1, lStop);
            Vector v2 = new Vector(lStop, l2);
            double distanceToV1 = Geo.distanceIfMatch(loc, v1);
            if (!Double.isNaN(distanceToV1) && distanceToV1 < allowableDistance) {
                // Matches to v1
            }
            double distanceToV2 = Geo.distanceIfMatch(loc, v2);
            if (!Double.isNaN(distanceToV2) && distanceToV2 < allowableDistance) {
                // Matches to v2
            }
        }
    }
}
