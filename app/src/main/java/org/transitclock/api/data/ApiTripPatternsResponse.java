/* (C)2023 */
package org.transitclock.api.data;

import java.util.Collection;
import java.util.List;

import org.transitclock.service.dto.IpcTripPattern;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * A list of ApiTripPattern objects
 *
 * @author SkiBu Smith
 */
@Data
public class ApiTripPatternsResponse {

    @JsonProperty("data")
    private List<ApiTripPattern> data;


    public ApiTripPatternsResponse(Collection<IpcTripPattern> ipcTripPatterns) {
        data = ipcTripPatterns
                .stream()
                .map(tp -> new ApiTripPattern(tp, true))
                .toList();
    }
}
