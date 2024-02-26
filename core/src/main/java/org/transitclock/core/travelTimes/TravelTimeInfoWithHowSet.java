/* (C)2023 */
package org.transitclock.core.travelTimes;

import java.util.List;
import org.transitclock.domain.structs.TravelTimesForStopPath.HowSet;
import org.transitclock.domain.structs.Trip;

/**
 * Extends TravelTimeInfo class but adds how the travel time was set. This way can see if travel
 * time set via AVL data directly, is for a different trip, or is for a different service class,
 * etc.
 *
 * @author SkiBu Smith
 */
public class TravelTimeInfoWithHowSet extends TravelTimeInfo {

    private final HowSet howSet;


    /**
     * Simple constructor.
     *
     * @param trip
     * @param stopPathIndex
     * @param stopTime
     * @param travelTimes
     * @param travelTimeSegLength
     * @param howSet
     */
    public TravelTimeInfoWithHowSet(
            Trip trip,
            int stopPathIndex,
            int stopTime,
            List<Integer> travelTimes,
            double travelTimeSegLength,
            HowSet howSet) {
        super(trip, stopPathIndex, stopTime, travelTimes, travelTimeSegLength);
        this.howSet = howSet;
    }

    public TravelTimeInfoWithHowSet(TravelTimeInfo travelTimeInfo, HowSet howSet) {
        super(travelTimeInfo);
        this.howSet = howSet;
    }

    /**
     * Returns how the travel time info was obtained
     *
     * @return
     */
    public HowSet howSet() {
        return howSet;
    }
}
