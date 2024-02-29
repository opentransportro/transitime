/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import org.transitclock.domain.structs.TravelTimesForStopPath;
import org.transitclock.domain.structs.TravelTimesForStopPath.HowSet;
import org.transitclock.utils.MathUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents travel times for a stop path
 *
 * @author SkiBu Smith
 */
@Data
public class ApiTravelTimesForStopPath {

    @XmlAttribute
    private int stopPathIndex;

    @XmlAttribute
    private String stopPathId;

    @XmlAttribute
    private Double travelTimeSegmentLength;

    @XmlAttribute
    private int stopTimeMsec;

    @XmlAttribute
    private int totalTravelTimeMsec;

    @XmlAttribute
    private HowSet howSet;

    @XmlElement(name = "travelTimesForSegment")
    private List<ApiTravelTimeForSegment> travelTimesForSegments;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiTravelTimesForStopPath() {}

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

        this.travelTimesForSegments = new ArrayList<ApiTravelTimeForSegment>();
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
