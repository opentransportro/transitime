/* (C)2023 */
package org.transitclock.service.contract;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import org.transitclock.service.dto.IpcPredictionForStopPath;

/**
 * Defines the RMI interface used for obtaining data required to look at the quality of predictions.
 *
 * @author Sean Og Crudden
 */
public interface PredictionAnalysisInterface extends Remote {
    List<IpcPredictionForStopPath> getRecordedTravelTimePredictions(
            String tripId, Integer stopPathIndex, Date startdate, Date enddate, String algorithm)
            throws RemoteException;

    List<IpcPredictionForStopPath> getCachedTravelTimePredictions(
            String tripId, Integer stopPathIndex, Date startdate, Date enddate, String algorithm)
            throws RemoteException;
}
