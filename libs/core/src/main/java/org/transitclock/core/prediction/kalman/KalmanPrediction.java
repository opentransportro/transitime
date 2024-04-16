/* (C)2023 */
package org.transitclock.core.prediction.kalman;

import org.transitclock.properties.PredictionProperties;

/**
 * @author Sean Óg Crudden
 */
public class KalmanPrediction {

    private final PredictionProperties.Data.Kalman kalmanProperties;

    public KalmanPrediction(PredictionProperties.Data.Kalman kalmanProperties) {
        this.kalmanProperties = kalmanProperties;
    }

    /**
     * @param last_vehicle_segment The last vehicles info for the time taken to cover the same
     *     segment
     * @param historical_segments The last 3 days for info relating to the time taken for the
     *     vehicle handling the same service/trip took.
     * @param last_prediction_error From the previous segments calculation result. (I am 99.9% sure
     *     you just start the chain of calcuations with an estimate)
     * @return KalmanPredictionResult which contains the predicted time and the
     *     last_prediction_error to be used in the next prediction calculation.
     * @throws Exception
     */
    public KalmanPredictionResult predict(TripSegment last_vehicle_segment,
                                          TripSegment[] historical_segments,
                                          double last_prediction_error) throws Exception {
        KalmanPredictionResult result = null;

        double average = historicalAverage(historical_segments);

        double variance = historicalVariance(historical_segments, average);

        double gain = gain(average, variance, last_prediction_error);

        double loop_gain = 1 - gain;

        result = new KalmanPredictionResult(
                prediction(gain, loop_gain, historical_segments, last_vehicle_segment, average),
                filterError(variance, gain));

        return result;
    }

    private double historicalAverage(TripSegment[] historical_segments) throws Exception {
        if (historical_segments.length > 0) {
            long total = 0;
            for (TripSegment historicalSegment : historical_segments) {
                long duration = historicalSegment.getDestination().getTime() - historicalSegment.getOrigin().getTime();
                total = total + duration;
            }
            return (double) (total / historical_segments.length);
        } else {
            throw new Exception("Cannot average nothing");
        }
    }

    private double historicalVariance(TripSegment[] historical_segments, double average) {
        double total = 0;

        for (TripSegment historicalSegment : historical_segments) {
            long duration = historicalSegment.getDestination().getTime()
                    - historicalSegment.getOrigin().getTime();

            double diff = duration - average;

            double long_diff_squared = diff * diff;

            total = total + long_diff_squared;
        }
        return total / historical_segments.length;
    }

    private double filterError(double variance, double loop_gain) {
        return variance * loop_gain;
    }

    private double gain(double average, double variance, double last_prediction_error) {
        return (last_prediction_error + variance) / (last_prediction_error + (2 * variance));
    }

    private double prediction(
            double gain,
            double loop_gain,
            TripSegment[] historical_segments,
            TripSegment last_vechicle_segment,
            double average_duration) {

        double historical_duration = average_duration;

        /* This may be better use the historical average rather than just the vehicle on previous day. This would damping issues with last days value being dramatically different. */
        if (!kalmanProperties.getUseaverage()) {
            historical_duration = historical_segments[historical_segments.length - 1]
                            .getDestination()
                            .getTime()
                    - historical_segments[historical_segments.length - 1]
                            .getOrigin()
                            .getTime();
        }

        long last_vehicle_duration = last_vechicle_segment.getDestination().getTime()
                - last_vechicle_segment.getOrigin().getTime();

        // prediction
        return (loop_gain * last_vehicle_duration) + (gain * historical_duration);
    }
}
