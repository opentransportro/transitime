/* (C)2023 */
package org.transitclock.domain.structs;

import java.util.List;
import lombok.Getter;

/**
 * This class is to be used as a key for a TripPattern map. It contains just enough information, the
 * shapeId and the stopPaths list, needed to uniquely identify a TripPattern.
 *
 * @author SkiBu Smith
 */
@Getter
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

    /**
     * Needed so can have Set/Map of TripPatterns and looking up elements will be quick and proper.
     * Note that this method does not do a full hashcode on stopPaths. This is because the StopPath
     * elements are not finalized when building up trip patterns. The tripPatternId still hasn't
     * been set. So don't want to compare the entire StopPath objects. Instead, only use
     * StopPath.basicHashCode(). This difference is very important so using an automatically
     * generated method here would not suffice.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((shapeId == null) ? 0 : shapeId.hashCode());
        int pathHashCode = 1;
        for (StopPath path : stopPaths) {
            pathHashCode = 31 * pathHashCode + path.basicHashCode();
        }
        result += pathHashCode;
        return result;
    }

    /**
     * Needed so can have Set/Map of TripPatterns and looking up elements will be quick and proper.
     * Note that this method does not do a full equals() on stopPaths. This is because the StopPath
     * elements are not finalized when building up trip patterns. The tripPatternId still hasn't
     * been set. So don't want to compare the entire StopPath objects. Instead, only use
     * StopPath.basicEquals(). This difference is very important so using an automatically generated
     * method here would not suffice.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof TripPatternKey other)) return false;
        if (getClass() != obj.getClass()) return false;
        if (shapeId == null) {
            if (other.shapeId != null) return false;
        } else if (!shapeId.equals(other.shapeId)) return false;
        if (stopPaths == null) {
            return other.stopPaths == null;
        } else {
            if (stopPaths.size() != other.stopPaths.size()) return false;
            for (int i = 0; i < stopPaths.size(); ++i) {
                StopPath path = stopPaths.get(i);
                StopPath otherPath = other.stopPaths.get(i);
                if (!path.basicEquals(otherPath)) return false;
            }
        }
        return true;
    }
}
