/* (C)2023 */
package org.transitclock.api.data;

import java.util.List;

import org.transitclock.domain.structs.Agency;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * A list of Agencies
 *
 * @author SkiBu Smith
 */
@Data
public class ApiAgenciesResponse {

    @JsonProperty("data")
    private List<ApiAgency> data;

    public ApiAgenciesResponse(List<ApiAgency> apiAgencies) {
        data = apiAgencies;
    }

    public ApiAgenciesResponse(String agencyId, List<Agency> agencies) {
        data = agencies.stream()
                .map(a -> new ApiAgency(agencyId, a))
                .toList();
    }
}
