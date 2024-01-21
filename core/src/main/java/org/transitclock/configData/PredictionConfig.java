package org.transitclock.configData;

import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.DoubleConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.LongConfigValue;
import org.transitclock.utils.Time;

public class PredictionConfig {
    public static final IntegerConfigValue closestVehicleStopsAhead = new IntegerConfigValue(
            "transitclock.prediction.closestvehiclestopsahead",
            5,
            "Num stops ahead a vehicle must be to be considers in the closest vehicle calculation");


    public static BooleanConfigValue returnArrivalPredictionForEndOfTrip = new BooleanConfigValue(
            "transitclock.prediction.returnArrivalPredictionForEndOfTrip",
            false,
            "This set to false will not return arrival predictions of the last stop on a" + " trip.");

    /*
     * TODO I think this needs to be a minimum of three and if just two will use
     * historical value.
     */
    public static final IntegerConfigValue minKalmanDays = new IntegerConfigValue(
            "transitclock.prediction.data.kalman.mindays",
            3,
            "Min number of days trip data that needs to be available before Kalman"
                    + " prediciton is used instead of default transiTime prediction.");

    public static final IntegerConfigValue maxKalmanDays = new IntegerConfigValue(
            "transitclock.prediction.data.kalman.maxdays",
            3,
            "Max number of historical days trips to include in Kalman prediction" + " calculation.");

    public static final IntegerConfigValue maxKalmanDaysToSearch = new IntegerConfigValue(
            "transitclock.prediction.data.kalman.maxdaystoseach",
            30,
            "Max number of days to look back for data. This will also be effected by how"
                    + " old the data in the cache is.");

    public static final DoubleConfigValue initialErrorValue = new DoubleConfigValue(
            "transitclock.prediction.data.kalman.initialerrorvalue",
            100d,
            "Initial Kalman error value to use to start filter.");

    /* May be better to use the default implementation as it splits things down into segments. */
    public static final BooleanConfigValue useKalmanForPartialStopPaths = new BooleanConfigValue(
            "transitclock.prediction.data.kalman.usekalmanforpartialstoppaths",
            true,
            "Will use Kalman prediction to get to first stop of prediction.");

    public static final IntegerConfigValue percentagePredictionMethodDifferenceneEventLog = new IntegerConfigValue(
            "transitclock.prediction.data.kalman.percentagePredictionMethodDifferencene",
            50,
            "If the difference in prediction method estimates is greater than this"
                    + " percentage log a Vehicle Event");


    public static final BooleanConfigValue useaverage = new BooleanConfigValue(
            "transitclock.prediction.kalman.useaverage",
            true,
            "Will use average travel time as opposed to last historical vehicle in Kalman"
                    + " prediction calculation.");


    public static final IntegerConfigValue tresholdForDifferenceEventLog = new IntegerConfigValue(
            "transitclock.prediction.data.kalman.tresholdForDifferenceEventLog",
            60000,
            "This is the threshold in milliseconds that the difference has to be over"
                    + " before it will consider the percentage difference.");



    public static final IntegerConfigValue timeBetweenPollingMsec = new IntegerConfigValue(
            "transitclock.schedBasedPreds.pollingRateMsec",
            4 * Time.MS_PER_MIN,
            "How frequently to look for blocks that do not have " + "associated vehicle.");

    public static final BooleanConfigValue processImmediatelyAtStartup = new BooleanConfigValue(
            "transitclock.schedBasedPreds.processImmediatelyAtStartup",
            false,
            "Whether should start creating schedule based predictions "
                    + "right at startup. Usually want to give AVL data a "
                    + "polling cycle to generate AVL based predictions so the "
                    + "default is false. But for a purely schedule based "
                    + "system want to set this to true so get the predictions "
                    + "immediately.");

    public static final IntegerConfigValue beforeStartTimeMinutes = new IntegerConfigValue(
            "transitclock.schedBasedPreds.beforeStartTimeMinutes",
            60,
            "How many minutes before a block start time should create " + "a schedule based vehicle for that block.");

    public static final IntegerConfigValue afterStartTimeMinutes = new IntegerConfigValue(
            "transitclock.schedBasedPreds.afterStartTimeMinutes",
            8, // Can take a while to automatically assign a vehicle
            "If predictions created for a block based on the schedule "
                    + "will remove those predictions this specified "
                    + "number of minutes after the block start time. If using "
                    + "schedule based predictions "
                    + "to provide predictions for even when there is no GPS "
                    + "feed then this can be disabled by using a negative value. "
                    + "But if using schedule based predictions until a GPS "
                    + "based vehicle is matched then want the schedule based "
                    + "predictions to be available until it is clear that no "
                    + "GPS based vehicle is in service. This is especially "
                    + "important when using the automatic assignment method "
                    + "because it can take a few minutes.");

    // ADD IN ORDER TO MANAGE CANCELLED TRIPS
    public static final BooleanConfigValue cancelTripOnTimeout = new BooleanConfigValue(
            "transitclock.schedBasedPreds.cancelTripOnTimeout",
            true,
            "Whether should mark a ScheduleBasePred as canceled.This won't remove a trip"
                    + " after afterStartTimeMinutes. Instead it will change the state to"
                    + " cancelled.");





    public static LongConfigValue maxDwellTimeAllowedInModel = new LongConfigValue(
            "transitclock.prediction.rls.maxDwellTimeAllowedInModel",
            (long) (2 * Time.MS_PER_MIN),
            "Max dwell time to be considered in dwell RLS algotithm.");
    public static LongConfigValue minDwellTimeAllowedInModel = new LongConfigValue(
            "transitclock.prediction.rls.minDwellTimeAllowedInModel",
            (long) 1000,
            "Min dwell time to be considered in dwell RLS algotithm.");
    public static LongConfigValue maxHeadwayAllowedInModel = new LongConfigValue(
            "transitclock.prediction.rls.maxHeadwayAllowedInModel",
            1 * Time.MS_PER_HOUR,
            "Max headway to be considered in dwell RLS algotithm.");
    public static LongConfigValue minHeadwayAllowedInModel = new LongConfigValue(
            "transitclock.prediction.rls.minHeadwayAllowedInModel",
            (long) 1000,
            "Min headway to be considered in dwell RLS algotithm.");
    public static IntegerConfigValue minSceheduleAdherence = new IntegerConfigValue(
            "transitclock.prediction.rls.minSceheduleAdherence",
            (int) (10 * Time.SEC_PER_MIN),
            "If schedule adherence of vehicle is outside this then not considerd in dwell" + " RLS algorithm.");
    public static IntegerConfigValue maxSceheduleAdherence = new IntegerConfigValue(
            "transitclock.prediction.rls.maxSceheduleAdherence",
            (int) (10 * Time.SEC_PER_MIN),
            "If schedule adherence of vehicle is outside this then not considerd in dwell" + " RLS algorithm.");

    public static DoubleConfigValue lambda = new DoubleConfigValue(
            "transitclock.prediction.rls.lambda",
            0.75,
            "This sets the rate at which the RLS algorithm forgets old values. Value are"
                    + " between 0 and 1. With 0 being the most forgetful.");

    /// travel time data filter


    public static LongConfigValue maxTravelTimeAllowedInModel = new LongConfigValue(
            "transitclock.prediction.travel.maxTravelTimeAllowedInModel",
            (long) (20 * Time.MS_PER_MIN),
            "Max travel time to be considered in algorithm. Milliseconds.");
    public static LongConfigValue minTravelTimeAllowedInModel = new LongConfigValue(
            "transitclock.prediction.travel.minTravelTimeAllowedInModel",
            (long) 1000,
            "Min travel time to be considered in algorithm. Milliseconds.");



    /// HistoricalAveragePredictionGeneratorImpl
    public static final IntegerConfigValue minDays = new IntegerConfigValue(
            "transitclock.prediction.data.average.mindays",
            1,
            "Min number of days trip data that needs to be available before historical"
                    + " average prediciton is used instead of default transiTime prediction.");



    /** */
    public static IntegerConfigValue samplesize = new IntegerConfigValue(
            "transitclock.prediction.dwell.average.samplesize",
            5,
            "Max number of samples to keep for mean calculation.");
    public static DoubleConfigValue fractionLimitForStopTimes = new DoubleConfigValue(
            "transitclock.prediction.dwell.average.fractionlimit",
            0.7,
            "For when determining stop times. Throws out outliers if they are less than 0.7"
                    + " or greater than 1/0.7 of the average.");


}
