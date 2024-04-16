package org.transitclock.properties;

import lombok.Data;

@Data
public class PredictionAccuracyProperties {
    // config param: transitclock.predAccuracy.pollingRateMsec
    // How frequently to query predictions for determining prediction accuracy.
    private Integer pollingRateMsec = 240000;

    // config param: transitclock.predAccuracy.maxPredTimeMinutes
    // Maximum time into the future for a pediction for it to be stored in memory for prediction accuracy analysis.
    private Integer maxPredTimeMinutes = 15;

    // config param: transitclock.predAccuracy.maxPredStalenessMinutes
    // Maximum time in minutes a prediction cam be into the past before it is removed from memory because no corresponding arrival/departure time was determined.
    private Integer maxPredStalenessMinutes = 15;

    // config param: transitclock.predAccuracy.stopsPerTrip
    // Number of stops per trip pattern that should collect prediction data for each polling cycle.
    private Integer stopsPerTrip = 5;

    // config param: transitclock.predAccuracy.maxRandomStopSelectionsPerTrip
    // Max number of random stops to look at to get the stopsPerTrip.
    private Integer maxRandomStopSelectionsPerTrip = 100;

    // config param: transitclock.predAccuracy.maxLatenessComparedToPredictionMsec
    // How late in msec a vehicle can arrive/departure a stop compared to the prediction and still have the prediction be considered a match.
    private Integer maxLatenessComparedToPredictionMsec = 1500000;

    // config param: transitclock.predAccuracy.maxEarlynessComparedToPredictionMsec
    // How early in msec a vehicle can arrive/departure a stop compared to the prediction and still have the prediction be considered a match.
    private Integer maxEarlynessComparedToPredictionMsec = 900000;

}
