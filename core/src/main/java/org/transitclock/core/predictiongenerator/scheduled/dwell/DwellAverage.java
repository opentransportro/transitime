/* (C)2023 */
package org.transitclock.core.predictiongenerator.scheduled.dwell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.transitclock.configData.PredictionConfig;
import org.transitclock.statistics.Statistics;

/**
 * @author scrudden This is a running average for dwell times.
 */
public class DwellAverage implements DwellModel {
    private final List<Integer> values = new ArrayList<>();

    // For this model headway or demand is not taken into account.
    @Override
    public void putSample(Integer value, Integer headway, Integer demand) {

        if (values.size() < PredictionConfig.samplesize.getValue()) {
            values.add(value);
            Collections.rotate(values, 1);
        } else {
            Collections.rotate(values, 1);
            values.set(0, value);
        }
    }

    @Override
    public Integer predict(Integer headway, Integer demand) {

        return Statistics.filteredMean(values, PredictionConfig.fractionLimitForStopTimes.getValue());
    }
}
