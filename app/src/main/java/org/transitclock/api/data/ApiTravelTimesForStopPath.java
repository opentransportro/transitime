/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;

import org.transitclock.domain.structs.HowSet;
import org.transitclock.domain.structs.TravelTimesForStopPath;
import org.transitclock.utils.MathUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents travel times for a stop path
 *
 * @author SkiBu Smith
 */
@Data
public class ApiTravelTimesForStopPath {

    @JsonProperty
    private int stopPathIndex;

    @JsonProperty
    private String stopPathId;

    @JsonProperty
    private Double travelTimeSegmentLength;

    @JsonProperty
    private int stopTimeMsec;

    @JsonProperty
    private int totalTravelTimeMsec;

    @JsonProperty
    private HowSet howSet;

    @JsonProperty
    private List<ApiTravelTimeForSegment> travelTimesForSegments;


    /**
     * Constructor
     *
     * @param travelTimesForStopPath
     */
    public ApiTravelTimesForStopPath(int stopPathIndex, TravelTimesForStopPath travelTimesForStopPath) {
        this.stopPathIndex = stopPathIndex;
        this.stopPathId = travelTimesForStopPath.getStopPathId();
        double travelTimeSegLengthValue = MathUtils.round(travelTimesForStopPath.getTravelTimeSegmentLength(), 1);
        this.travelTimeSegmentLength = Double.isNaN(travelTimeSegLengthValue) ? null : travelTimeSegLengthValue;
        this.stopTimeMsec = travelTimesForStopPath.getStopTimeMsec();
        this.totalTravelTimeMsec = travelTimesForStopPath.getStopPathTravelTimeMsec();
        this.howSet = travelTimesForStopPath.getHowSet();

        this.travelTimesForSegments = new ArrayList<>();
        for (int segmentIndex = 0;
                segmentIndex < travelTimesForStopPath.getNumberTravelTimeSegments();
                ++segmentIndex) {
            int travelTimeForSegment = travelTimesForStopPath.getTravelTimeSegmentMsec(segmentIndex);
            ApiTravelTimeForSegment travelTime = new ApiTravelTimeForSegment(
                    segmentIndex, travelTimeForSegment, travelTimesForStopPath.getTravelTimeSegmentLength());
            this.travelTimesForSegments.add(travelTime);
        }
    }
}
