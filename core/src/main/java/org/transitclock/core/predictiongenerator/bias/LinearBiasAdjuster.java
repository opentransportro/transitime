/* (C)2023 */
package org.transitclock.core.predictiongenerator.bias;

import lombok.Getter;
import lombok.ToString;
import org.transitclock.config.data.CoreConfig;
import org.transitclock.utils.Time;

/**
 * @author scrudden
 *     <p>This will adjust a prediction based on a percentage which increases linearly as the
 *     horizon gets bigger.
 *     <p>The rate of increase of the percentage can be set using the constructor.
 */
@Getter
@ToString
public class LinearBiasAdjuster implements BiasAdjuster {

    private double rate = -Double.NaN;
    private double percentage = Double.NaN;

    public LinearBiasAdjuster() {
        this.rate = CoreConfig.rateChangePercentage.getValue();
    }

    public LinearBiasAdjuster(double rateChangePercentage) {
        this.rate = rateChangePercentage;
    }

    /* going to adjust by a larger percentage as horizon gets bigger.*/

    @Override
    public long adjustPrediction(long prediction) {
        percentage = (prediction / 100) * rate;

        double new_prediction = prediction + (((percentage / 100) * prediction) * CoreConfig.linearUpdown.getValue());
        return (long) new_prediction;
    }
}
