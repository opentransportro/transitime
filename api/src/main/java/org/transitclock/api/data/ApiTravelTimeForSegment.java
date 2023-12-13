/* (C)2023 */
package org.transitclock.api.data;

import javax.xml.bind.annotation.XmlAttribute;
import org.transitclock.utils.Geo;
import org.transitclock.utils.MathUtils;
import org.transitclock.utils.Time;

/**
 * For representing travel time for a single segment.
 *
 * @author SkiBu Smith
 */
public class ApiTravelTimeForSegment {

    @XmlAttribute
    private int segmentIndex;

    @XmlAttribute
    private int segmentTimeMsec;

    @XmlAttribute
    private Double speedInMph;

    @XmlAttribute
    private Double speedInKph;

    @XmlAttribute
    private Double speedInMetersPerSec;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiTravelTimeForSegment() {}

    /**
     * Constructor
     *
     * @param segmentIndex
     * @param segmentTimeMsec
     * @param segmentLength
     */
    public ApiTravelTimeForSegment(int segmentIndex, int segmentTimeMsec, double segmentLength) {
        this.segmentIndex = segmentIndex;
        this.segmentTimeMsec = segmentTimeMsec;

        // If segment time is 0 then speeds will default to null and will
        // not be output. Better than trying to divide by zero since
        // can't output NaN with JSON.
        if (segmentTimeMsec != 0) {
            double speedInMetersPerSec = segmentLength * Time.MS_PER_SEC / segmentTimeMsec;
            this.speedInMph = MathUtils.round(speedInMetersPerSec / Geo.MPH_TO_MPS, 1);
            this.speedInKph = MathUtils.round(speedInMetersPerSec / Geo.KPH_TO_MPS, 1);
            this.speedInMetersPerSec = MathUtils.round(speedInMetersPerSec, 1);
        }
    }
}
