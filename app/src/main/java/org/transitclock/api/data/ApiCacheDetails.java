/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * For when have list of VehicleDetails. By using this class can control the element name when data
 * is output.
 *
 * @author SkiBu Smith
 */
@XmlRootElement
public class ApiCacheDetails {

    public ApiCacheDetails(String name, Integer size) {
        super();
        this.size = size;
        this.name = name;
    }

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "size")
    private Integer size;


    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiCacheDetails() {}
}
