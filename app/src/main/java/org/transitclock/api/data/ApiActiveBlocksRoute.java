/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;

import org.transitclock.service.dto.IpcActiveBlock;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * A route for when outputting active blocks
 *
 * @author SkiBu Smith
 */
@Data
public class ApiActiveBlocksRoute {

    // ID of route
    @JsonProperty
    private String id;

    // Route short name
    @JsonProperty
    private String shortName;

    // Name of route
    @JsonProperty
    private String name;

    // The active blocks for the route
    @JsonProperty("block")
    private List<ApiActiveBlock> activeBlocks;

    public ApiActiveBlocksRoute(String id, String shortName, String name) {
        this.id = id;
        this.shortName = shortName;
        this.name = name;
        activeBlocks = new ArrayList<>();
    }

    public void add(IpcActiveBlock ipcActiveBlock, String agencyId) {
        activeBlocks.add(new ApiActiveBlock(ipcActiveBlock, agencyId));
    }
}
