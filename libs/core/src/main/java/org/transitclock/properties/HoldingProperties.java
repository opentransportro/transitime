package org.transitclock.properties;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class HoldingProperties {
    // config param: transitclock.holding.storeHoldingTimes
    // This is set to true to record all holding times.
    private Boolean storeHoldingTimes = true;

    // config param: transitclock.holding.maxPredictionsForHoldingTimeCalculation
    // This is the maximim number of arrival predictions to include in holding time calculation
    private Integer maxPredictionsForHoldingTimeCalculation = 3;

    // config param: transitclock.holding.usearrivalevents
    // Generate a holding time on arrival events.
    private Boolean usearrivalevents = true;

    // config param: transitclock.holding.usearrivalpredictions
    // Generate a holding time on arrival predictions.
    private Boolean usearrivalpredictions = true;

    // config param: transitclock.holding.regenerateondeparture
    // Regenerate a holding time for all vehicles at control point when a vehicle departs the control point.
    private Boolean regenerateondeparture = false;

    // config param: transitclock.holding.plannedHeadwayMsec
    // Planned Headway
    private Integer plannedHeadwayMsec = 540000;

    // config param: transitclock.holding.controlStops
    // This is a list of stops to generate holding times for.
    private List<String> controlStops = new ArrayList<>();

}
