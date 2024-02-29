/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import org.transitclock.service.dto.IpcDirection;
import org.transitclock.service.dto.IpcStop;

import java.util.ArrayList;
import java.util.List;

/**
 * A single direction, containing stops
 *
 * @author SkiBu Smith
 */
public class ApiDirection {

    @XmlAttribute
    private String id;

    @XmlAttribute
    private String title;

    @XmlElement(name = "stop")
    private List<ApiStop> stops;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiDirection() {}

    /**
     * Constructs a ApiDirection using an IpcDirection
     *
     * @param direction
     */
    public ApiDirection(IpcDirection direction) {
        this.id = direction.getDirectionId();
        this.title = direction.getDirectionTitle();

        this.stops = new ArrayList<ApiStop>(direction.getStops().size());
        for (IpcStop stop : direction.getStops()) {
            this.stops.add(new ApiStop(stop));
        }
    }
}
