package org.transitclock.config.data;

import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringListConfigValue;

public class HoldingConfig {
    public static BooleanConfigValue storeHoldingTimes = new BooleanConfigValue(
            "transitclock.holding.storeHoldingTimes", true, "This is set to true to record all holding times.");

    public static IntegerConfigValue maxPredictionsForHoldingTimeCalculation = new IntegerConfigValue(
            "transitclock.holding.maxPredictionsForHoldingTimeCalculation",
            3,
            "This is the maximim number of arrival predictions to include in holding time" + " calculation");

    public static BooleanConfigValue useArrivalEvents = new BooleanConfigValue(
            "transitclock.holding.usearrivalevents", true, "Generate a holding time on arrival events.");

    public static BooleanConfigValue useArrivalPredictions = new BooleanConfigValue(
            "transitclock.holding.usearrivalpredictions", true, "Generate a holding time on arrival predictions.");

    public static BooleanConfigValue regenerateOnDeparture = new BooleanConfigValue(
            "transitclock.holding.regenerateondeparture",
            false,
            "Regenerate a holding time for all vehicles at control point when a vehicle"
                    + " departs the control point.");

    public static IntegerConfigValue plannedHeadwayMsec =
            new IntegerConfigValue("transitclock.holding.plannedHeadwayMsec", 60 * 1000 * 9, "Planned Headway");
    public static StringListConfigValue controlStopList = new StringListConfigValue(
            "transitclock.holding.controlStops", null, "This is a list of stops to generate holding times for.");


}
