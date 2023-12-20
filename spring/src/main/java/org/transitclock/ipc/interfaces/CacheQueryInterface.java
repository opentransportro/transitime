/* (C)2023 */
package org.transitclock.ipc.interfaces;

import org.transitclock.ipc.data.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;

/**
 * Defines the RMI interface used for obtaining cache runtime information.
 *
 * @author Sean Og Crudden
 */
public interface CacheQueryInterface extends Remote {

    /**
     * Returns a list of current arrival or departure events for a specified stop that are in the
     * cache.
     *
     * @param stopId
     * @return List of IpcArrivalDeparture objects for the stop, one for each event.
     * @throws RemoteException
     */
    public List<IpcArrivalDeparture> getStopArrivalDepartures(String stopId) throws RemoteException;

    /**
     * Returns the number of entries in the cacheName cache
     *
     * @param cacheName
     * @return
     * @throws RemoteException
     */
    public Integer entriesInCache(String cacheName) throws RemoteException;

    /**
     * Returns the historical average value for the trip stopPathIndex that is held in the
     * HistoricalAverageCache
     *
     * @param tripId
     * @param stopPathIndex
     * @return IpcHistoricalAverage
     * @throws RemoteException
     */
    public IpcHistoricalAverage getHistoricalAverage(String tripId, Integer stopPathIndex) throws RemoteException;

    /**
     * Return the arrivals and departures for a trip on a specific day and start time
     *
     * @param tripId
     * @param date
     * @param starttime
     * @return
     * @throws RemoteException
     */
    public List<IpcArrivalDeparture> getTripArrivalDepartures(String tripId, LocalDate date, Integer starttime)
            throws RemoteException;

    /**
     * @return a list of the keys that have values in the historical average cache for schedules
     *     based services
     * @throws RemoteException
     */
    public List<IpcHistoricalAverageCacheKey> getScheduledBasedHistoricalAverageCacheKeys() throws RemoteException;

    /**
     * @return a list of the keys that have values in the historical average cache for frequency
     *     based services.
     * @throws RemoteException
     */
    public List<IpcHistoricalAverageCacheKey> getFrequencyBasedHistoricalAverageCacheKeys() throws RemoteException;

    /**
     * @return a list of the keys that have values in the Kalman error value cache
     * @throws RemoteException
     */
    public List<IpcKalmanErrorCacheKey> getKalmanErrorCacheKeys() throws RemoteException;

    /**
     * @return a list of the keys for the holding times in the cache
     * @throws RemoteException
     */
    public List<IpcHoldingTimeCacheKey> getHoldingTimeCacheKeys() throws RemoteException;

    /**
     * Return the latest Kalman error value for a the stop path of a trip.
     *
     * @param tripId
     * @param stopPathIndex
     * @return
     * @throws RemoteException
     */
    public Double getKalmanErrorValue(String tripId, Integer stopPathIndex) throws RemoteException;
}
