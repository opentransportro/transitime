/* (C)2023 */
package org.transitclock.core.dataCache;

import java.util.List;
import org.transitclock.core.Indices;

public interface ErrorCache {

    KalmanError getErrorValue(Indices indices);

    KalmanError getErrorValue(KalmanErrorCacheKey key);

    void putErrorValue(Indices indices, Double value);

    void putErrorValue(KalmanErrorCacheKey key, Double value);

    List<KalmanErrorCacheKey> getKeys();
}
