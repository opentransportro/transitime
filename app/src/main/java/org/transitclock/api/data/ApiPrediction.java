/* (C)2023 */
package org.transitclock.api.data;


import org.transitclock.service.dto.IpcPrediction;
import org.transitclock.utils.Time;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Contains data for a single prediction.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiPrediction {

    // Epoch time in seconds (not msec, so shorter)
    @JsonProperty(value = "time")
    private long time;

    @JsonProperty(value = "sec")
    private int seconds;

    @JsonProperty(value = "min")
    private int minutes;

    @JsonProperty(value = "scheduleBased")
    protected Boolean schedBasedPreds;

    // Most of the time will be predicting arrival predictions. Therefore
    // isDeparture will only be displayed for the more rare times that
    // departure prediction is being provided.
    @JsonProperty(value = "departure")
    private Boolean isDeparture;

    @JsonProperty(value = "trip")
    private String tripId;

    @JsonProperty(value = "blockId")
    private String blockId;

    @JsonProperty(value = "tripPattern")
    private String tripPatternId;

    @JsonProperty(value = "vehicle")
    private String vehicleId;

    // Only output if true
    @JsonProperty(value = "atEndOfTrip")
    private Boolean isAtEndOfTrip;

    // Only output if true
    @JsonProperty(value = "delayed")
    private Boolean isDelayed;

    // Only output if true
    @JsonProperty(value = "lateAndSubsequentTripSoMarkAsUncertain")
    private Boolean isLateAndSubsequentTripSoMarkAsUncertain;

    // Only output if true
    @JsonProperty(value = "notYetDeparted")
    private Boolean basedOnScheduledDeparture;

    // Only output if passenger count is valid
    @JsonProperty(value = "passengerCount")
    private String passengerCount;

    @JsonProperty(value = "isDeparture")
    private String isDepartureDuplicate; // same field different name

    @JsonProperty(value = "affectedByLayover")
    private String affectedByLayover;

    public ApiPrediction(IpcPrediction prediction) {
        time = prediction.getPredictionTime() / Time.MS_PER_SEC;
        seconds = (int) (prediction.getPredictionTime() - System.currentTimeMillis()) / Time.MS_PER_SEC;
        // Always round down minutes to be conservative and so that user
        // doesn't miss bus.
        minutes = seconds / 60;

        // Only set schedBasedPreds if it is schedule based so that the
        // attribute is not output for the majority of the times that it
        // is not schedule based.
        schedBasedPreds = prediction.isSchedBasedPred() ? true : null;

        if (!prediction.isArrival()) isDeparture = true;

        tripId = prediction.getTripId();
        tripPatternId = prediction.getTripPatternId();

        vehicleId = prediction.getVehicleId();

        if (prediction.isAtEndOfTrip()) isAtEndOfTrip = true;

        // Only set basedOnScheduledDeparture if true so that it is not output
        // if false since it will then be null
        if (prediction.isAffectedByWaitStop()) basedOnScheduledDeparture = true;

        // Only set passengerCount if it is valid so that it is not output if it
        // is not valid since will then be null
        if (prediction.isPassengerCountValid()) passengerCount = String.valueOf(prediction.getPassengerCount());

        // Only set if true so only output for rare case
        if (prediction.isDelayed()) isDelayed = true;

        // Only set if true so only output for rare case
        if (prediction.isLateAndSubsequentTripSoMarkAsUncertain())
            isLateAndSubsequentTripSoMarkAsUncertain = Boolean.TRUE;

        affectedByLayover = Boolean.toString(prediction.isAffectedByWaitStop());

        isDepartureDuplicate = Boolean.toString(!prediction.isArrival());

        blockId = prediction.getBlockId();
        isLateAndSubsequentTripSoMarkAsUncertain = true;
    }
}
