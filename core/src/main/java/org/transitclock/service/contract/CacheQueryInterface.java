/* (C)2023 */
package org.transitclock.service.contract;

import org.transitclock.service.dto.IpcArrivalDeparture;
import org.transitclock.service.dto.IpcHistoricalAverage;
import org.transitclock.service.dto.IpcHistoricalAverageCacheKey;
import org.transitclock.service.dto.IpcHoldingTimeCacheKey;
import org.transitclock.service.dto.IpcKalmanErrorCacheKey;

import java.time.LocalDate;
import java.util.List;

/**
 * Defines the RMI interface used for obtaining cache runtime information.
 *
 * @author Sean Og Crudden
 */
public interface CacheQueryInterface {

    /**
     * Returns a list of current arrival or departure events for a specified stop that are in the
     * cache.
     *
     * @param stopId
     * @return List of IpcArrivalDeparture objects for the stop, one for each event.
     */
    List<IpcArrivalDeparture> getStopArrivalDepartures(String stopId);

    /**
     * Returns the number of entries in the cacheName cache
     *
     * @param cacheName
     * @return
     */
    Integer entriesInCache(String cacheName);

    /**
     * Returns the historical average value for the trip stopPathIndex that is held in the
     * HistoricalAverageCache
     *
     * @param tripId
     * @param stopPathIndex
     * @return IpcHistoricalAverage
     */
    IpcHistoricalAverage getHistoricalAverage(String tripId, Integer stopPathIndex);

    /**
     * Return the arrivals and departures for a trip on a specific day and start time
     *
     * @param tripId
     * @param date
     * @param starttime
     * @return
     */
    List<IpcArrivalDeparture> getTripArrivalDepartures(String tripId, LocalDate date, Integer starttime);

    /**
     * @return a list of the keys that have values in the historical average cache for schedules
     *     based services
     */
    List<IpcHistoricalAverageCacheKey> getScheduledBasedHistoricalAverageCacheKeys();

    /**
     * @return a list of the keys that have values in the historical average cache for frequency
     *     based services.
     */
    List<IpcHistoricalAverageCacheKey> getFrequencyBasedHistoricalAverageCacheKeys();

    /**
     * @return a list of the keys that have values in the Kalman error value cache
     */
    List<IpcKalmanErrorCacheKey> getKalmanErrorCacheKeys();

    /**
     * @return a list of the keys for the holding times in the cache
     */
    List<IpcHoldingTimeCacheKey> getHoldingTimeCacheKeys();

    /**
     * Return the latest Kalman error value for a the stop path of a trip.
     *
     * @param tripId
     * @param stopPathIndex
     * @return
     */
    Double getKalmanErrorValue(String tripId, Integer stopPathIndex);
}
