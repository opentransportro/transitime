/* (C)2023 */
package org.transitclock.service;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.transitclock.SingletonContainer;
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
@Service
@Slf4j
public class PredictionAnalysisServiceImpl implements PredictionAnalysisInterface {
    private final StopPathPredictionCache stopPathPredictionCache;

    public PredictionAnalysisServiceImpl(StopPathPredictionCache stopPathPredictionCache) {
        this.stopPathPredictionCache = stopPathPredictionCache;
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
        List<PredictionForStopPath> predictions = stopPathPredictionCache.getPredictions(key);
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
