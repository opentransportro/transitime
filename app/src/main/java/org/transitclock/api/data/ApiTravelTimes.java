/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;

import org.transitclock.domain.structs.TravelTimesForStopPath;
import org.transitclock.domain.structs.TravelTimesForTrip;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author SkiBu Smith
 */
@Data
public class ApiTravelTimes {

    @JsonProperty
    private int configRev;

    @JsonProperty
    private int travelTimeRev;

    @JsonProperty
    private String tripPatternId;

    @JsonProperty
    private String tripCreatedForId;

    @JsonProperty
    private List<ApiTravelTimesForStopPath> travelTimesForStopPaths;

    /**
     * Constructor
     *
     * @param travelTimes
     */
    public ApiTravelTimes(TravelTimesForTrip travelTimes) {
        this.configRev = travelTimes.getConfigRev();
        this.travelTimeRev = travelTimes.getTravelTimesRev();
        this.tripPatternId = travelTimes.getTripPatternId();
        this.tripCreatedForId = travelTimes.getTripCreatedForId();

        this.travelTimesForStopPaths = new ArrayList<>();

        for (int stopPathIndex = 0;
                stopPathIndex < travelTimes.getTravelTimesForStopPaths().size();
                ++stopPathIndex) {
            TravelTimesForStopPath travelTimesForStopPath = travelTimes.getTravelTimesForStopPath(stopPathIndex);
            this.travelTimesForStopPaths.add(new ApiTravelTimesForStopPath(stopPathIndex, travelTimesForStopPath));
        }
    }
}
