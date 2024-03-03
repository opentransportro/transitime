/* (C)2023 */
package org.transitclock.core.prediction.bias;

import lombok.Getter;
import lombok.ToString;
import org.transitclock.config.data.CoreConfig;

/**
 * @author scrudden This will adjust a prediction based on a percentage which increases
 *     exponentially as the horizon gets bigger.
 */
@Getter
@ToString
public class ExponentialBiasAdjuster implements BiasAdjuster {
    private double percentage = Double.NaN;
    private double number = Double.NaN;


    public ExponentialBiasAdjuster() {
        this.number = CoreConfig.baseNumber.getValue();
    }

    /*
     * y=a(b^x)+c
     */
    @Override
    public long adjustPrediction(long prediction) {
        double tothepower = (prediction / 1000) / 60;
        percentage = ((Math.pow(number, tothepower)) * CoreConfig.multiple.getValue()) - CoreConfig.constant.getValue();

        double new_prediction = prediction + (CoreConfig.updown.getValue() * (((percentage / 100) * prediction)));
        return (long) new_prediction;
    }
}
