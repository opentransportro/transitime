/* (C)2023 */
package org.transitclock.core;

import org.transitclock.utils.Geo;
import org.transitclock.utils.Time;

/**
 * Describes where an AVL report matches to an assignment temporally. It is a SpatialMatch but also
 * include the temporal difference of how far off the expected time the match is. The temporal
 * difference is not respect to the schedule. It is with respect to where the vehicle should be
 * based on the previous AVL report. So if a vehicle is really behind schedule but is now traveling
 * as fast as expected then the temporal difference will be close to zero.
 *
 * @author SkiBu Smith
 */
public class TemporalMatch extends SpatialMatch {

    private final TemporalDifference temporalDifference;

    /********************** Member Functions **************************/
    public TemporalMatch(SpatialMatch spatialMatch, TemporalDifference temporalDifference) {
        super(spatialMatch);
        this.temporalDifference = temporalDifference;
    }

    @Override
    public String toString() {
        return "TemporalMatch ["
                + "temporalDifference="
                + temporalDifference
                + ", avlTime="
                + Time.dateTimeStrMsec(avlTime)
                + ", blockId="
                + block.getId()
                + ", tripIndex="
                + tripIndex
                + ", gtfsStopSeq="
                + getStopPath().getGtfsStopSeq()
                + ", stopPathIndex="
                + stopPathIndex
                + ", segmentIndex="
                + segmentIndex
                + ", isLayover="
                + isLayover()
                + ", distanceToSegment="
                + Geo.distanceFormat(distanceToSegment)
                + ", distanceAlongSegment="
                + Geo.distanceFormat(distanceAlongSegment)
                + ", distanceAlongStopPath="
                + Geo.distanceFormat(getDistanceAlongStopPath())
                + ", atStop="
                + atStop
                + ", trip="
                + getTrip().toShortString()
                + "]";
    }

    /************************ Getter Methods ***************************/
    public TemporalDifference getTemporalDifference() {
        return temporalDifference;
    }
}
