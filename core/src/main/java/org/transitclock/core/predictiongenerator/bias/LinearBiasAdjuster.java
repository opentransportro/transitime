/* (C)2023 */
package org.transitclock.core.predictiongenerator.bias;

import lombok.Getter;
import lombok.ToString;
import org.transitclock.config.DoubleConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.configData.CoreConfig;
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

    public static void main(String[] args) {
        LinearBiasAdjuster adjuster = new LinearBiasAdjuster(0.0006);
        long result = adjuster.adjustPrediction(20 * Time.MS_PER_MIN);
        System.out.println("Percentage is :"
                + adjuster.getPercentage()
                + " giving a result to :"
                + Math.round((float) result / Time.MS_PER_SEC));

        result = adjuster.adjustPrediction(15 * Time.MS_PER_MIN);
        System.out.println("Percentage is :"
                + adjuster.getPercentage()
                + " giving a result to :"
                + Math.round((float) result / Time.MS_PER_SEC));

        result = adjuster.adjustPrediction(10 * Time.MS_PER_MIN);
        System.out.println("Percentage is :"
                + adjuster.getPercentage()
                + " giving a result to :"
                + Math.round((float) result / Time.MS_PER_SEC));

        result = adjuster.adjustPrediction(5 * Time.MS_PER_MIN);
        System.out.println("Percentage is :"
                + adjuster.getPercentage()
                + " giving a result to :"
                + Math.round((float) result / Time.MS_PER_SEC));

        result = adjuster.adjustPrediction(Time.MS_PER_MIN);
        System.out.println("Percentage is :"
                + adjuster.getPercentage()
                + " giving a result to :"
                + Math.round((float) result / Time.MS_PER_SEC));
    }
}
