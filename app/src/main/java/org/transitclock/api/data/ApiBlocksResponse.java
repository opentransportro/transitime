/* (C)2023 */
package org.transitclock.api.data;

import java.util.Collection;
import java.util.List;

import org.transitclock.service.dto.IpcBlock;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ApiBlocksResponse {
    @JsonProperty("data")
    private List<ApiBlock> data;

    public ApiBlocksResponse(Collection<IpcBlock> blocks) {
        data = blocks.stream()
                .map(ApiBlock::new)
                .toList();
    }
}
