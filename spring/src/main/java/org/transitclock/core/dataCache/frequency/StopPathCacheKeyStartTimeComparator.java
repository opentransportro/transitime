/* (C)2023 */
package org.transitclock.core.dataCache.frequency;

import org.transitclock.core.dataCache.StopPathCacheKey;

import java.util.Comparator;

public class StopPathCacheKeyStartTimeComparator implements Comparator<StopPathCacheKey> {

    @Override
    public int compare(StopPathCacheKey key1, StopPathCacheKey key2) {
        return key1.getStartTime().compareTo(key2.getStartTime());
    }
}
