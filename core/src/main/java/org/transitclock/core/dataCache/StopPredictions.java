/* (C)2023 */
package org.transitclock.core.dataCache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.transitclock.domain.structs.PredictionForStopPath;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StopPredictions implements Serializable {
    public List<PredictionForStopPath> predictions;

    public void addPrediction(PredictionForStopPath prediction) {
        if (this.predictions == null) {
            predictions = new ArrayList<>();
        }
        predictions.add(prediction);
    }
}
