/* (C)2023 */
package org.transitclock.api.data;

import org.transitclock.utils.Geo;
import org.transitclock.utils.MathUtils;
import org.transitclock.utils.Time;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * For representing travel time for a single segment.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiTravelTimeForSegment {

    @JsonProperty
    private int segmentIndex;

    @JsonProperty
    private int segmentTimeMsec;

    @JsonProperty
    private Double speedInMph;

    @JsonProperty
    private Double speedInKph;

    @JsonProperty
    private Double speedInMetersPerSec;


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
