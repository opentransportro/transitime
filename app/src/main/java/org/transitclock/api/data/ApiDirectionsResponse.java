/* (C)2023 */
package org.transitclock.api.data;

import java.util.List;

import org.transitclock.service.dto.IpcDirectionsForRoute;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * A list of directions.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiDirectionsResponse {

    @JsonProperty
    private List<ApiDirection> data;

    public ApiDirectionsResponse(IpcDirectionsForRoute stopsForRoute) {
        data = stopsForRoute.getDirections()
                .stream()
                .map(ApiDirection::new)
                .toList();
    }
}
