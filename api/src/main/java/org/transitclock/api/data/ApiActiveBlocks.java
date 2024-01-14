/* (C)2023 */
package org.transitclock.api.data;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.transitclock.ipc.data.IpcActiveBlock;

/**
 * Collection of ActiveBlocks
 *
 * @author SkiBu Smith
 */
@XmlRootElement(name = "blocks")
public class ApiActiveBlocks {

    @XmlElement(name = "blocks")
    private List<ApiActiveBlock> activeBlocks;

    /********************** Member Functions **************************/

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
        Collections.sort(activeBlocks, comparator);
    }

    /** For sorting the active blocks by route and then block ID */
    private static final Comparator<ApiActiveBlock> comparator = new Comparator<ApiActiveBlock>() {
        @Override
        public int compare(ApiActiveBlock o1, ApiActiveBlock o2) {
            // Compare route IDs
            int result = o1.getApiTripSummary()
                    .getRouteId()
                    .compareTo(o2.getApiTripSummary().getRouteId());
            if (result != 0) return result;

            // Route IDs the same so compare block IDs
            return o1.getId().compareTo(o2.getId());
        }
    };
}
