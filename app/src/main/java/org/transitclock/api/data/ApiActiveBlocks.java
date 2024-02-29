/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.transitclock.service.dto.IpcActiveBlock;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Collection of ActiveBlocks
 *
 * @author SkiBu Smith
 */
@Data
@XmlRootElement(name = "blocks")
public class ApiActiveBlocks {

    @XmlElement(name = "blocks")
    private List<ApiActiveBlock> activeBlocks;

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiActiveBlocks() {}

    public ApiActiveBlocks(Collection<IpcActiveBlock> ipcActiveBlocks, String agencyId)
            throws IllegalAccessException, InvocationTargetException {
        activeBlocks = new ArrayList<ApiActiveBlock>();
        for (IpcActiveBlock ipcActiveBlock : ipcActiveBlocks) {
            activeBlocks.add(new ApiActiveBlock(ipcActiveBlock, agencyId));
        }

        // Sort the active blocks by routeId so that can more easily display
        // the results in order that is clear to user
        activeBlocks.sort(comparator);
    }

    // Compare route IDs
    // Route IDs the same so compare block IDs
    /** For sorting the active blocks by route and then block ID */
    private static final Comparator<ApiActiveBlock> comparator = Comparator
            .comparing((ApiActiveBlock o) -> o.getApiTripSummary().getRouteId())
            .thenComparing(ApiActiveBlock::getId);
}
