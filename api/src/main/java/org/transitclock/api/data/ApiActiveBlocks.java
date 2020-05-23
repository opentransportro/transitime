/*
 * This file is part of Transitime.org
 *
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transitclock.api.data;

import org.transitclock.ipc.data.IpcActiveBlock;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Collection of ActiveBlocks
 *
 * @author SkiBu Smith
 */
@XmlRootElement(name = "blocks")
public class ApiActiveBlocks {

    /**
     * For sorting the active blocks by route and then block ID
     */
    private static final Comparator<ApiActiveBlock> comparator =
            new Comparator<ApiActiveBlock>() {
                @Override
                public int compare(ApiActiveBlock o1, ApiActiveBlock o2) {
                    // Compare route IDs
                    int result = o1.getApiTripSummary().getRouteId().compareTo(o2.getApiTripSummary().getRouteId());
                    if (result != 0)
                        return result;

                    // Route IDs the same so compare block IDs
                    return o1.getId().compareTo(o2.getId());
                }
            };

    /********************** Member Functions **************************/
    @XmlElement(name = "blocks")
    private List<ApiActiveBlock> activeBlocks;

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse
     * "MessageBodyWriter not found for media type=application/json" exception.
     */
    protected ApiActiveBlocks() {
    }

    public ApiActiveBlocks(Collection<IpcActiveBlock> ipcActiveBlocks,
                           String agencyId) throws IllegalAccessException, InvocationTargetException {
        activeBlocks = new ArrayList<ApiActiveBlock>();
        for (IpcActiveBlock ipcActiveBlock : ipcActiveBlocks) {
            activeBlocks.add(new ApiActiveBlock(ipcActiveBlock, agencyId));
        }

        // Sort the active blocks by routeId so that can more easily display
        // the results in order that is clear to user
        Collections.sort(activeBlocks, comparator);
    }

}
