/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.transitclock.service.dto.IpcPrediction;
import org.transitclock.utils.Time;

/**
 * Contains data for a single prediction.
 *
 * @author SkiBu Smith
 */@Data
@XmlRootElement
public class ApiPrediction {

    // Epoch time in seconds (not msec, so shorter)
    @XmlAttribute(name = "time")
    private long time;

    @XmlAttribute(name = "sec")
    private int seconds;

    @XmlAttribute(name = "min")
    private int minutes;

    @XmlAttribute(name = "scheduleBased")
    protected Boolean schedBasedPreds;

    // Most of the time will be predicting arrival predictions. Therefore
    // isDeparture will only be displayed for the more rare times that
    // departure prediction is being provided.
    @XmlAttribute(name = "departure")
    private Boolean isDeparture;

    @XmlAttribute(name = "trip")
    private String tripId;

    @XmlAttribute(name = "blockId")
    private String blockId;

    @XmlAttribute(name = "tripPattern")
    private String tripPatternId;

    @XmlAttribute(name = "vehicle")
    private String vehicleId;

    // Only output if true
    @XmlAttribute(name = "atEndOfTrip")
    private Boolean isAtEndOfTrip;

    // Only output if true
    @XmlAttribute(name = "delayed")
    private Boolean isDelayed;

    // Only output if true
    @XmlAttribute(name = "lateAndSubsequentTripSoMarkAsUncertain")
    private Boolean isLateAndSubsequentTripSoMarkAsUncertain;

    // Only output if true
    @XmlAttribute(name = "notYetDeparted")
    private Boolean basedOnScheduledDeparture;

    // Only output if passenger count is valid
    @XmlAttribute(name = "passengerCount")
    private String passengerCount;

    @XmlAttribute(name = "isDeparture")
    private String isDepartureDuplicate; // same field different name

    @XmlAttribute(name = "affectedByLayover")
    private String affectedByLayover;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    public ApiPrediction() {}

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
