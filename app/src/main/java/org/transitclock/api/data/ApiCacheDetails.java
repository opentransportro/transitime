/* (C)2023 */
package org.transitclock.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * For when have list of VehicleDetails. By using this class can control the element name when data
 * is output.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiCacheDetails {

    @JsonProperty
    private String name;

    @JsonProperty
    private Integer size;

    public ApiCacheDetails(String name, Integer size) {
        this.size = size;
        this.name = name;
    }
}
