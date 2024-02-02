/* (C)2023 */
package org.transitclock.api.data;

import java.util.List;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * A list of Agencies
 *
 * @author SkiBu Smith
 */
@XmlRootElement(name = "agencies")
public class ApiAgencies {

    @XmlElement(name = "agency")
    private List<ApiAgency> agenciesData;

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiAgencies() {}

    public ApiAgencies(List<ApiAgency> apiAgencies) {
        agenciesData = apiAgencies;
    }
}
