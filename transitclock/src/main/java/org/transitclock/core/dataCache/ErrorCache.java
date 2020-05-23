package org.transitclock.core.dataCache;

import org.transitclock.core.Indices;

import java.util.List;

public interface ErrorCache {

    KalmanError getErrorValue(Indices indices);

    KalmanError getErrorValue(KalmanErrorCacheKey key);

    void putErrorValue(Indices indices, Double value);

    void putErrorValue(KalmanErrorCacheKey key, Double value);

    List<KalmanErrorCacheKey> getKeys();


}