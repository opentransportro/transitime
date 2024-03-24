/* (C)2023 */
package org.transitclock.api.data;

import java.util.List;

import org.transitclock.service.dto.IpcDirection;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A single direction, containing stops
 *
 * @author SkiBu Smith
 */
public class ApiDirection {

    @JsonProperty
    private String id;

    @JsonProperty
    private String title;

    @JsonProperty
    private List<ApiStop> stops;

    /**
     * Constructs a ApiDirection using an IpcDirection
     *
     * @param direction
     */
    public ApiDirection(IpcDirection direction) {
        this.id = direction.getDirectionId();
        this.title = direction.getDirectionTitle();
        this.stops = direction.getStops()
                .stream()
                .map(ApiStop::new)
                .toList();
    }
}
