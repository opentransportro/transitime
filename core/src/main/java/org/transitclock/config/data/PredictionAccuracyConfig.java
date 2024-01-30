package org.transitclock.config.data;

import org.transitclock.config.IntegerConfigValue;
import org.transitclock.utils.Time;

public class PredictionAccuracyConfig {
    public static final IntegerConfigValue timeBetweenPollingPredictionsMsec = new IntegerConfigValue(
            "transitclock.predAccuracy.pollingRateMsec",
            4 * Time.MS_PER_MIN,
            "How frequently to query predictions for determining " + "prediction accuracy.");


    public static final IntegerConfigValue maxPredTimeMinutes = new IntegerConfigValue(
            "transitclock.predAccuracy.maxPredTimeMinutes",
            15,
            "Maximum time into the future for a pediction for it to "
                    + "be stored in memory for prediction accuracy analysis.");

    public static final IntegerConfigValue maxPredStalenessMinutes = new IntegerConfigValue(
            "transitclock.predAccuracy.maxPredStalenessMinutes",
            15,
            "Maximum time in minutes a prediction cam be into the "
                    + "past before it is removed from memory because no "
                    + "corresponding arrival/departure time was determined.");


    public static final IntegerConfigValue stopsPerTrip = new IntegerConfigValue(
            "transitclock.predAccuracy.stopsPerTrip",
            5,
            "Number of stops per trip pattern that should collect prediction data for each polling cycle.");

    public static final IntegerConfigValue maxRandomStopSelectionsPerTrip = new IntegerConfigValue(
            "transitclock.predAccuracy.maxRandomStopSelectionsPerTrip",
            100,
            "Max number of random stops to look at to get the stopsPerTrip.");


    public static final IntegerConfigValue maxLatenessComparedToPredictionMsec = new IntegerConfigValue(
            "transitclock.predAccuracy.maxLatenessComparedToPredictionMsec",
            25 * Time.MS_PER_MIN,
            "How late in msec a vehicle can arrive/departure a stop "
                    + "compared to the prediction and still have the prediction "
                    + "be considered a match.");



    public static final IntegerConfigValue maxEarlynessComparedToPredictionMsec = new IntegerConfigValue(
            "transitclock.predAccuracy.maxEarlynessComparedToPredictionMsec",
            15 * Time.MS_PER_MIN,
            "How early in msec a vehicle can arrive/departure a stop "
                    + "compared to the prediction and still have the prediction "
                    + "be considered a match.");
}
