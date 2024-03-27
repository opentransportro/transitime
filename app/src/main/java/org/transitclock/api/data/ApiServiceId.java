/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;

import java.util.List;
import java.util.Map;

/**
 * A short description of a serviceId. For when outputting list of block IDs for service.
 *
 * @author SkiBu Smith
 */
public class ApiServiceId {

    @XmlAttribute
    private String id;

    @XmlAttribute
    private List<String> blockIds;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiServiceId() {
    }

    public ApiServiceId(String serviceId, List<String> blockIds) {
    this.id = serviceId;
    this.blockIds = blockIds;
    }
}
