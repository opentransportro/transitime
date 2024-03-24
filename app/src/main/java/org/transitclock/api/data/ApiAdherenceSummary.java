/* (C)2023 */
package org.transitclock.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiAdherenceSummary {

    @JsonProperty
    private Integer late;

    @JsonProperty
    private Integer ontime;

    @JsonProperty
    private Integer early;

    @JsonProperty
    private Integer nodata;

    @JsonProperty
    private Integer blocks;
}
