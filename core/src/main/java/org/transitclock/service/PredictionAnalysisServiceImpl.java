/* (C)2023 */
package org.transitclock.service;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.transitclock.core.dataCache.StopPathCacheKey;
import org.transitclock.core.dataCache.StopPathPredictionCache;
import org.transitclock.domain.structs.PredictionForStopPath;
import org.transitclock.service.dto.IpcPredictionForStopPath;
import org.transitclock.service.contract.PredictionAnalysisInterface;

/**
 * @author Sean Óg Crudden Server to allow stored travel time predictions to be queried. TODO May
 *     not be set to run by default as really only for analysis of predictions. TODO This needs to
 *     be changed to also work with frequency based services.
 */
@Slf4j
public class PredictionAnalysisServiceImpl implements PredictionAnalysisInterface {
    // Should only be accessed as singleton class
    private static PredictionAnalysisServiceImpl singleton;

    public static PredictionAnalysisInterface instance() {
        return singleton;
    }

    protected PredictionAnalysisServiceImpl() {
    }

    /**
     * Starts up the PredictionAnalysisServer so that RMI calls can be used to query travel times
     * prediction stored. This will automatically cause the object to continue to run and serve
     * requests.
     *
     * @return the singleton PredictionAnalysisServer object. Usually does not need to used since
     *     the server will be fully running.
     */
    public static synchronized PredictionAnalysisServiceImpl start() {
        if (singleton == null) {
            singleton = new PredictionAnalysisServiceImpl();
        }
        return singleton;
    }

    @Override
    public List<IpcPredictionForStopPath> getRecordedTravelTimePredictions(
            String tripId, Integer stopPathIndex, Date startdate, Date enddate, String algorithm)
            throws RemoteException {
        List<PredictionForStopPath> result = PredictionForStopPath.getPredictionForStopPathFromDB(
                startdate, enddate, algorithm, tripId, stopPathIndex);
        List<IpcPredictionForStopPath> results = new ArrayList<>();
        for (PredictionForStopPath prediction : result) {
            IpcPredictionForStopPath ipcPrediction = new IpcPredictionForStopPath(prediction);
            results.add(ipcPrediction);
        }

        return results;
    }

    @Override
    public List<IpcPredictionForStopPath> getCachedTravelTimePredictions(
            String tripId, Integer stopPathIndex, Date startdate, Date enddate, String algorithm)
            throws RemoteException {
        StopPathCacheKey key = new StopPathCacheKey(tripId, stopPathIndex, true);
        List<PredictionForStopPath> predictions =
                StopPathPredictionCache.getInstance().getPredictions(key);
        List<IpcPredictionForStopPath> results = new ArrayList<IpcPredictionForStopPath>();
        if (predictions != null) {
            for (PredictionForStopPath prediction : predictions) {
                IpcPredictionForStopPath ipcPrediction = new IpcPredictionForStopPath(prediction);
                if (algorithm != null && !algorithm.isEmpty()) {
                    if (algorithm.equals(prediction.getAlgorithm())) {
                        results.add(ipcPrediction);
                    }
                } else {
                    results.add(ipcPrediction);
                }
            }
        }
        return results;
    }
}
