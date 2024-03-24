/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.transitclock.service.dto.IpcActiveBlock;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Collection of ActiveBlocks
 *
 * @author SkiBu Smith
 */
@Data
public class ApiActiveBlocksResponse {

    @JsonProperty("data")
    private List<ApiActiveBlock> data;

    public ApiActiveBlocksResponse(Collection<IpcActiveBlock> ipcActiveBlocks, String agencyId) {
        data = ipcActiveBlocks
                .stream()
                .map(b -> new ApiActiveBlock(b, agencyId))
                .sorted(Comparator
                                .comparing((ApiActiveBlock o) -> o.getTrip().getRouteId())
                                .thenComparing(ApiActiveBlock::getId))
                .toList();
    }
}
