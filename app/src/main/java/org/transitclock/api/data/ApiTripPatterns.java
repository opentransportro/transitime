/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.transitclock.service.dto.IpcTripPattern;

/**
 * A list of ApiTripPattern objects
 *
 * @author SkiBu Smith
 */
@XmlRootElement
public class ApiTripPatterns {

    @XmlElement(name = "tripPatterns")
    private List<ApiTripPattern> tripPatterns;


    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiTripPatterns() {}

    public ApiTripPatterns(Collection<IpcTripPattern> ipcTripPatterns) {
        tripPatterns = new ArrayList<ApiTripPattern>();
        for (IpcTripPattern ipcTripPattern : ipcTripPatterns) {
            // Including stop paths in output since that is likely
            // what user wants since they are trying to understand
            // the trip patterns for a route.
            tripPatterns.add(new ApiTripPattern(ipcTripPattern, true));
        }
    }
}
