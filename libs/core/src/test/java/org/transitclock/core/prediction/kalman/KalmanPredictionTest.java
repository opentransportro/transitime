/* (C)2023 */
package org.transitclock.core.prediction.kalman;

import org.transitclock.ApplicationProperties.Prediction.Data.Kalman;

import org.junit.jupiter.api.Test;

class KalmanPredictionTest {

    @Test
    void predict() {
        KalmanPrediction kalmanPrediction = new KalmanPrediction(new Kalman());

        Vehicle vehicle = new Vehicle("RIY 30");

        VehicleStopDetail originDetail = new VehicleStopDetail(null, 0, vehicle);
        VehicleStopDetail destinationDetail_1_k = new VehicleStopDetail(null, 380, vehicle);
        VehicleStopDetail destinationDetail_2_k = new VehicleStopDetail(null, 420, vehicle);
        VehicleStopDetail destinationDetail_3_k = new VehicleStopDetail(null, 400, vehicle);

        VehicleStopDetail destinationDetail_0_k_1 = new VehicleStopDetail(null, 300, vehicle);

        TripSegment ts_day_1_k = new TripSegment(originDetail, destinationDetail_1_k);
        TripSegment ts_day_2_k = new TripSegment(originDetail, destinationDetail_2_k);
        TripSegment ts_day_3_k = new TripSegment(originDetail, destinationDetail_3_k);

        TripSegment ts_day_0_k_1 = new TripSegment(originDetail, destinationDetail_0_k_1);

        TripSegment historical_segments_k[] = {ts_day_1_k, ts_day_2_k, ts_day_3_k};

        TripSegment last_vehicle_segment = ts_day_0_k_1;

        try {
            KalmanPredictionResult result =
                    kalmanPrediction.predict(last_vehicle_segment, historical_segments_k, 72.40);

            if (result != null) {
                if ((result.getResult() > 355 && result.getResult() < 356)
                        && (result.getFilterError() > 149 && result.getFilterError() < 150)) {
                    System.out.println("Successful Kalman Filter Prediction.");
                } else {
                    System.out.println("UnSuccessful Kalman Filter Prediction.");
                }
            } else {
                System.out.println("No result.");
            }
        } catch (Exception e) {
            System.out.println("Whoops");
            e.printStackTrace();
        }
    }
}
