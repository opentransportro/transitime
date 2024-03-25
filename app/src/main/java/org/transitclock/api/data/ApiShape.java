/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;

import org.transitclock.domain.structs.Location;
import org.transitclock.service.dto.IpcShape;
import org.transitclock.utils.Geo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * A portion of a shape that defines a trip pattern. A List of ApiLocation objects.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiShape {

    private static final int LOOP_ENDING_MAX_DISTANCE = 200;
    private static final String LOOP_PATTERN = "circular";
    private static final String LINAR_PATTER = "linear";

    @JsonProperty("tripPattern")
    private String tripPatternId;

    @JsonProperty
    private String headsign;

    // For indicating that in UI should deemphasize this shape because it
    // is not on a main trip pattern.
    @JsonProperty
    private Boolean minor;

    @JsonProperty("points")
    private List<ApiLocation> points;

    @JsonProperty
    private double length;

    @JsonProperty
    private String directionId;

    // To define what kind of pattern is: circular (loop, one ending), linear (normal line with two
    // different endings)
    @JsonProperty
    private String patternType = "linear";


    public ApiShape(IpcShape shape) {
        this.tripPatternId = shape.getTripPatternId();
        this.headsign = shape.getHeadsign();
        this.length = shape.getLength();
        this.directionId = shape.getDirectionId();
        // If true then set to null so that this attribute won't then be
        // output as XML/JSON, therefore making output a bit more compact.
        this.minor = shape.isUiShape() ? null : true;
        this.points = new ArrayList<ApiLocation>();
        for (Location loc : shape.getLocations()) {
            this.points.add(new ApiLocation(loc.getLat(), loc.getLon()));
        }
        int size = shape.getLocations().size();
        if (size > 0
                && Geo.distance(
                                shape.getLocations().get(0),
                                shape.getLocations().get(size - 1))
                        < LOOP_ENDING_MAX_DISTANCE) patternType = LOOP_PATTERN;
        else patternType = LINAR_PATTER;
    }
}
