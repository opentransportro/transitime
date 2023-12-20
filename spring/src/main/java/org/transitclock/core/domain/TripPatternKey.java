/* (C)2023 */
package org.transitclock.core.domain;

import lombok.Data;

import java.util.List;

/**
 * This class is to be used as a key for a TripPattern map. It contains just enough information, the
 * shapeId and the stopPaths list, needed to uniquely identify a TripPattern.
 *
 * @author SkiBu Smith
 */
@Data
public class TripPatternKey {

    private final String shapeId;

    private final List<StopPath> stopPaths;

    /**
     * Construct TripPatternBase. Can then be used as key to map to see of the TripPattern already
     * exists.
     *
     * @param shapeId from the trip.txt GTFS file
     * @param paths Specifies the stops for the trip pattern. Must not be null.
     */
    public TripPatternKey(String shapeId, List<StopPath> paths) {
        if (paths == null) {
            throw new RuntimeException("stopPaths param must be " + "non-null for TripPatternBase() constructor");
        }

        this.shapeId = shapeId;
        this.stopPaths = paths;
    }


    /** Hibernate requires a no-arg constructor */
    protected TripPatternKey() {
        shapeId = null;
        stopPaths = null;
    }
}
