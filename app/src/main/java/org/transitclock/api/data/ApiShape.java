/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import org.transitclock.domain.structs.Location;
import org.transitclock.service.dto.IpcShape;
import org.transitclock.utils.Geo;

import java.util.ArrayList;
import java.util.List;

/**
 * A portion of a shape that defines a trip pattern. A List of ApiLocation objects.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiShape {

    @XmlAttribute(name = "tripPattern")
    private String tripPatternId;

    @XmlAttribute
    private String headsign;

    // For indicating that in UI should deemphasize this shape because it
    // is not on a main trip pattern.
    @XmlAttribute(name = "minor")
    private Boolean minor;

    @XmlElement(name = "loc")
    private List<ApiLocation> locations;

    @XmlAttribute
    private double length;

    @XmlAttribute
    private String directionId;

    // To define what kind of pattern is: circular (loop, one ending), linear (normal line with two
    // different endings)
    @XmlAttribute
    private String patternType = "linear";

    private static final int LOOP_ENDING_MAX_DISTANCE = 200;
    private static final String LOOP_PATTERN = "circular";
    private static final String LINAR_PATTER = "linear";

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiShape() {}

    public ApiShape(IpcShape shape) {
        this.tripPatternId = shape.getTripPatternId();
        this.headsign = shape.getHeadsign();
        this.length = shape.getLength();
        this.directionId = shape.getDirectionId();
        // If true then set to null so that this attribute won't then be
        // output as XML/JSON, therefore making output a bit more compact.
        this.minor = shape.isUiShape() ? null : true;
        this.locations = new ArrayList<ApiLocation>();
        for (Location loc : shape.getLocations()) {
            this.locations.add(new ApiLocation(loc.getLat(), loc.getLon()));
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
