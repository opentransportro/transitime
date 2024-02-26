/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import org.transitclock.service.dto.IpcStop;

/**
 * Full description of a stop.
 *
 * <p>Note: extending from ApiLocation since have a lat & lon. Would be nice to have ApiLocation as
 * a member but when try this get a internal server 500 error.
 *
 * @author SkiBu Smith
 */
@XmlType(propOrder = {"id", "lat", "lon", "name", "code", "minor", "pathLength"})
public class ApiStop extends ApiTransientLocation {

    @XmlAttribute
    private String id;

    @XmlAttribute
    private String name;

    @XmlAttribute
    private Integer code;

    // For indicating that in UI should deemphasize this stop because it
    // is not on a main trip pattern.
    @XmlAttribute(name = "minor")
    private Boolean minor;

    @XmlAttribute
    private Double pathLength;


    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiStop() {}

    public ApiStop(IpcStop stop) {
        super(stop.getLoc().getLat(), stop.getLoc().getLon());
        this.id = stop.getId();
        this.name = stop.getName();
        this.code = stop.getCode();
        this.pathLength = stop.getStopPathLength() == null ? 0.0 : stop.getStopPathLength();
        // If true then set to null so that this attribute won't then be
        // output as XML/JSON, therefore making output a bit more compact.
        this.minor = stop.isUiStop() ? null : true;
    }
}
