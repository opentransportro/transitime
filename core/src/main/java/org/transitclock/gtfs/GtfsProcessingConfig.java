package org.transitclock.gtfs;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "with")
public class GtfsProcessingConfig {
    // Set by constructor. Specifies where to find data files
    private final String gtfsDirectoryName;
    private final String supplementDir;

    private final double pathOffsetDistance;
    private final double maxStopToPathDistance;
    private final double maxDistanceForEliminatingVertices;
    private final int defaultWaitTimeAtStopMsec;
    private final double maxSpeedKph;
    private final double maxTravelTimeSegmentLength;
    private final boolean trimPathBeforeFirstStopOfTrip;
    private final double maxDistanceBetweenStops;
    private final boolean disableSpecialLoopBackToBeginningCase;

    private final Integer stopCodeBaseValue;
    private final String outputPathsAndStopsForGraphingRouteIds;
    private final double minDistanceBetweenStopsToDisambiguateHeadsigns;

    public boolean hasSupplementDir() {
        return supplementDir != null;
    }
}
