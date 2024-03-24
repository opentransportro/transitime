/* (C)2023 */
package org.transitclock.api.data;

import org.transitclock.domain.structs.Extent;
import org.transitclock.utils.MathUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Describes the extent of a route or agency via a min & max lat & lon.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiExtent {

    @JsonProperty
    @JsonFormat(shape = Shape.NUMBER)
    private double minLat;

    @JsonProperty
    @JsonFormat(shape = Shape.NUMBER)
    private double minLon;

    @JsonProperty
    @JsonFormat(shape = Shape.NUMBER)
    private double maxLat;

    @JsonProperty
    @JsonFormat(shape = Shape.NUMBER)
    private double maxLon;

    public ApiExtent(Extent extent) {
        this.minLat = MathUtils.round(extent.getMinLat(), 5);
        this.minLon = MathUtils.round(extent.getMinLon(), 5);
        this.maxLat = MathUtils.round(extent.getMaxLat(), 5);
        this.maxLon = MathUtils.round(extent.getMaxLon(), 5);
    }
}
