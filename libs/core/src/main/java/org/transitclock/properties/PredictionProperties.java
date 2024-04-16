package org.transitclock.properties;

import lombok.Data;

@Data
public class PredictionProperties {
    // config param: transitclock.prediction.closestvehiclestopsahead
    // Num stops ahead a vehicle must be to be considers in the closest vehicle calculation
    private Integer closestvehiclestopsahead = 5;

    // config param: transitclock.prediction.returnArrivalPredictionForEndOfTrip
    // This set to false will not return arrival predictions of the last stop on a trip.
    private Boolean returnArrivalPredictionForEndOfTrip = false;

    @lombok.Data
    public static class Data {
        @lombok.Data
        public static class Kalman {
            // config param: transitclock.prediction.data.kalman.mindays
            // Min number of days trip data that needs to be available before Kalman prediciton is used instead of default transiTime prediction.
            private Integer mindays = 3;

            // config param: transitclock.prediction.data.kalman.maxdays
            // Max number of historical days trips to include in Kalman prediction calculation.
            private Integer maxdays = 3;

            // config param: transitclock.prediction.data.kalman.maxdaystoseach
            // Max number of days to look back for data. This will also be effected by how old the data in the cache is.
            private Integer maxdaystosearch = 30;

            // config param: transitclock.prediction.data.kalman.initialerrorvalue
            // Initial Kalman error value to use to start filter.
            private Double initialerrorvalue = 100.0;

            // config param: transitclock.prediction.data.kalman.usekalmanforpartialstoppaths
            // Will use Kalman prediction to get to first stop of prediction.
            private Boolean usekalmanforpartialstoppaths = true;


            // config param: transitclock.prediction.data.kalman.percentagePredictionMethodDifferencene
            // If the difference in prediction method estimates is greater than this percentage log a Vehicle Event
            private Integer percentagePredictionMethodDifferencene = 50;

            // config param: transitclock.prediction.data.kalman.tresholdForDifferenceEventLog
            // This is the threshold in milliseconds that the difference has to be over before it will consider the percentage difference.
            private Integer thresholdForDifferenceEventLog = 60000;

            // config param: transitclock.prediction.kalman.useaverage
            // Will use average travel time as opposed to last historical vehicle in Kalman prediction calculation.
            private Boolean useaverage = true;

        }

        @lombok.Data
        public static class Average {
            // config param: transitclock.prediction.data.average.mindays
            // Min number of days trip data that needs to be available before historical average prediciton is used instead of default transiTime prediction.
            private Integer mindays = 1;
        }

        private Data.Kalman kalman = new Data.Kalman();
        private Data.Average average = new Data.Average();
    }

    private Data data = new Data();


    // config param: transitclock.schedBasedPreds.pollingRateMsec
    // How frequently to look for blocks that do not have associated vehicle.
    private Integer pollingRateMsec = 240000;

    // config param: transitclock.schedBasedPreds.processImmediatelyAtStartup
    // Whether should start creating schedule based predictions right at startup. Usually want to give AVL data a polling cycle to generate AVL based predictions so the default is false. But for a purely schedule based system want to set this to true so get the predictions immediately.
    private Boolean processImmediatelyAtStartup = false;

    // config param: transitclock.schedBasedPreds.beforeStartTimeMinutes
    // How many minutes before a block start time should create a schedule based vehicle for that block.
    private Integer beforeStartTimeMinutes = 60;

    // config param: transitclock.schedBasedPreds.afterStartTimeMinutes
    // If predictions created for a block based on the schedule will remove those predictions this specified number of minutes after the block start time. If using schedule based predictions to provide predictions for even when there is no GPS feed then this can be disabled by using a negative value. But if using schedule based predictions until a GPS based vehicle is matched then want the schedule based predictions to be available until it is clear that no GPS based vehicle is in service. This is especially important when using the automatic assignment method because it can take a few minutes.
    private Integer afterStartTimeMinutes = 8;

    // config param: transitclock.schedBasedPreds.cancelTripOnTimeout
    // Whether should mark a ScheduleBasePred as canceled.This won't remove a trip after afterStartTimeMinutes. Instead it will change the state to cancelled.
    private Boolean cancelTripOnTimeout = true;

    @lombok.Data
    public static class Rls {
        // config param: transitclock.prediction.rls.maxDwellTimeAllowedInModel
        // Max dwell time to be considered in dwell RLS algotithm.
        private Long maxDwellTimeAllowedInModel = 120000L;

        // config param: transitclock.prediction.rls.minDwellTimeAllowedInModel
        // Min dwell time to be considered in dwell RLS algotithm.
        private Long minDwellTimeAllowedInModel = 1000L;

        // config param: transitclock.prediction.rls.maxHeadwayAllowedInModel
        // Max headway to be considered in dwell RLS algotithm.
        private Long maxHeadwayAllowedInModel = 3600000L;

        // config param: transitclock.prediction.rls.minHeadwayAllowedInModel
        // Min headway to be considered in dwell RLS algotithm.
        private Long minHeadwayAllowedInModel = 1000L;

        // config param: transitclock.prediction.rls.minSceheduleAdherence
        // If schedule adherence of vehicle is outside this then not considerd in dwell RLS algorithm.
        private Integer minSceheduleAdherence = 600;

        // config param: transitclock.prediction.rls.maxSceheduleAdherence
        // If schedule adherence of vehicle is outside this then not considerd in dwell RLS algorithm.
        private Integer maxSceheduleAdherence = 600;

        // config param: transitclock.prediction.rls.lambda
        // This sets the rate at which the RLS algorithm forgets old values. Value are between 0 and 1. With 0 being the most forgetful.
        private Double lambda = 0.75;
    }

    private Rls rls = new Rls();

    @lombok.Data
    public static class Travel {
        // config param: transitclock.prediction.travel.maxTravelTimeAllowedInModel
        // Max travel time to be considered in algorithm. Milliseconds.
        private Long maxTravelTimeAllowedInModel = 1200000L;

        // config param: transitclock.prediction.travel.minTravelTimeAllowedInModel
        // Min travel time to be considered in algorithm. Milliseconds.
        private Long minTravelTimeAllowedInModel = 1000L;
    }

    private Travel travel = new Travel();


    @lombok.Data
    public static class Dwell {

        @lombok.Data
        public static class Average {

            // config param: transitclock.prediction.dwell.average.samplesize
            // Max number of samples to keep for mean calculation.
            private Integer samplesize = 5;

            // config param: transitclock.prediction.dwell.average.fractionlimit
            // For when determining stop times. Throws out outliers if they are less than 0.7 or greater than 1/0.7 of the average.
            private Double fractionlimit = 0.7;
        }


        private Dwell.Average average = new Dwell.Average();
    }

    private Dwell dwell = new Dwell();
}
