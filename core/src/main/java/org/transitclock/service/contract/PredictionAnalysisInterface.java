/* (C)2023 */
package org.transitclock.service.contract;

import org.transitclock.service.dto.IpcPredictionForStopPath;

import java.util.Date;
import java.util.List;

/**
 * Defines the RMI interface used for obtaining data required to look at the quality of predictions.
 *
 * @author Sean Og Crudden
 */
public interface PredictionAnalysisInterface {
    List<IpcPredictionForStopPath> getRecordedTravelTimePredictions(
            String tripId, Integer stopPathIndex, Date startdate, Date enddate, String algorithm);

    List<IpcPredictionForStopPath> getCachedTravelTimePredictions(
            String tripId, Integer stopPathIndex, Date startdate, Date enddate, String algorithm);
}
