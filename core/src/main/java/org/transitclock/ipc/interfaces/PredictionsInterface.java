/* (C)2023 */
package org.transitclock.ipc.interfaces;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import org.transitclock.db.structs.Location;
import org.transitclock.ipc.data.IpcPredictionsForRouteStopDest;

/**
 * Defines the RMI interface used for obtaining predictions.
 *
 * @author SkiBu Smith
 */
public interface PredictionsInterface extends Remote {

    /** This class is for use as key into hash maps that store prediction data. */
    class RouteStop implements Serializable {
        private final String routeIdOrShortName;
        private final String stopIdOrCode;

        private static final long serialVersionUID = -4558270897399749207L;

        public RouteStop(String routeIdOrShortName, String stopId) {
            this.routeIdOrShortName = routeIdOrShortName;
            this.stopIdOrCode = stopId;
        }

        public String getRouteIdOrShortName() {
            return routeIdOrShortName;
        }

        public String getStopIdOrCode() {
            return stopIdOrCode;
        }

        @Override
        public String toString() {
            return "RouteStop [" + "routeIdOrShortName=" + routeIdOrShortName + ", stopIdOrCode=" + stopIdOrCode + "]";
        }
    }

    /**
     * Returns list of current predictions for the specified route/stop.
     *
     * @param routeIdOrShortName Can be either the GTFS route_id or the route_short_name.
     * @param stopId
     * @param predictionsPerStop Max number of predictions to return for route/stop
     * @return List of PredictionsForRouteStop objects for the route/stop, one for each destination
     * @throws RemoteException
     */
    List<IpcPredictionsForRouteStopDest> get(String routeShortName, String stopId, int predictionsPerStop)
            throws RemoteException;

    /**
     * For each route/stop specified returns a list of predictions for that stop. Since expensive
     * RMI calls are being done this method is much more efficient for obtaining multiple
     * predictions then if a separate get() call is done for each route/stop.
     *
     * @param routeStops List of route/stops to return predictions for. Uses route short name or
     *     route ID
     * @param predictionsPerStop Max number of predictions to return per route/stop
     * @return List of PredictionsForRouteStop objects for the route/stop. There is a separate one
     *     for each destination for each route/stop.
     * @throws RemoteException
     */
    List<IpcPredictionsForRouteStopDest> get(List<RouteStop> routeStops, int predictionsPerStop) throws RemoteException;

    /**
     * Returns predictions based on the specified location.
     *
     * @param loc The user's location
     * @param maxDistance How far a stop can be away from the loc
     * @param predictionsPerStop
     * @return List of PredictionsForRouteStop objects for the location. There is a separate one for
     *     each destination for each route/stop.
     * @throws RemoteException
     */
    List<IpcPredictionsForRouteStopDest> get(Location loc, double maxDistance, int predictionsPerStop)
            throws RemoteException;

    /**
     * Returns all predictions. This is intended for clients such as the GTFS-RT vehicle update feed
     * that outputs all predictions by trip.
     *
     * @param predictionMaxFutureSecs
     * @return List of all PredictionsForRouteStop objects for system. There is a separate one for
     *     every route/stop/destination.
     * @throws RemoteException
     */
    List<IpcPredictionsForRouteStopDest> getAllPredictions(int predictionMaxFutureSecs) throws RemoteException;
}
