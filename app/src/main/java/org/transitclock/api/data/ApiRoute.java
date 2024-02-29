/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.Data;
import org.transitclock.service.dto.IpcRouteSummary;

/**
 * A short description of a route. For when outputting list of routes for agency.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiRoute {

    @XmlAttribute
    private String id;

    @XmlAttribute
    private String name;

    @XmlAttribute
    private String shortName;

    @XmlAttribute
    private String longName;

    @XmlAttribute
    private String type;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiRoute() {}

    public ApiRoute(IpcRouteSummary route) {
        this.id = route.getId();
        this.name = route.getName();
        this.shortName = route.getShortName();
        this.longName = route.getLongName();
        this.type = route.getType();
    }
}
