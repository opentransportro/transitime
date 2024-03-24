/* (C)2023 */
package org.transitclock.api.data;

import java.util.Collection;
import java.util.List;

import org.transitclock.service.dto.IpcBlock;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * A list of terse blocks, without trip pattern or schedule info
 *
 * @author Michael Smith
 */
@Data
public class ApiBlocksTerseResponse {
    @JsonProperty("data")
    private List<ApiBlockTerse> data;

    public ApiBlocksTerseResponse(Collection<IpcBlock> blocks) {
        data = blocks.stream()
                .map(ApiBlockTerse::new)
                .toList();
    }
}
