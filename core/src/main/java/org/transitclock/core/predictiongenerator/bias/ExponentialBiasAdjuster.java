/* (C)2023 */
package org.transitclock.core.predictiongenerator.bias;

import lombok.Getter;
import lombok.ToString;
import org.transitclock.config.data.CoreConfig;
import org.transitclock.utils.Time;

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

    public static void main(String[] args) {
        ExponentialBiasAdjuster adjuster = new ExponentialBiasAdjuster();
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

        result = adjuster.adjustPrediction(1 * Time.MS_PER_MIN);
        System.out.println("Percentage is :"
                + adjuster.getPercentage()
                + " giving a result to :"
                + Math.round((float) result / Time.MS_PER_SEC));
    }
}
