/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * For outputting simple list of unsorted service IDs with lists of sorted block IDs
 *
 * @author SkiBu Smith
 */
@XmlRootElement
public class ApiServiceIds {

    @XmlElement(name= "serviceIds")
    private List<ApiServiceId> apiServiceIds;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiServiceIds() {
    }

    /**
     * Creates the API unsorted version of list of IDs.
     *
     * @param serviceIds
     */
    public ApiServiceIds(Map<String, List<String>> serviceIds) {
        apiServiceIds = new ArrayList<>();
    serviceIds.forEach((key, list) -> apiServiceIds
            .add(new ApiServiceId(key,list)));
    }
}
