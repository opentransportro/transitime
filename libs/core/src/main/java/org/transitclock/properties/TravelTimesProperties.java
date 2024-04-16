package org.transitclock.properties;

import lombok.Data;

@Data
public class TravelTimesProperties {
    // config param: transitclock.traveltimes.fractionLimitForStopTimes
    // For when determining stop times. Throws out outliers.
    private Double fractionLimitForStopTimes = 0.7;

    // config param: transitclock.traveltimes.fractionLimitForTravelTimes
    // For when determining travel times. Throws out outliers.
    private Double fractionLimitForTravelTimes = 0.7;

    // config param: transitclock.travelTimes.resetEarlyTerminalDepartures
    // For some agencies vehicles won't be departing terminal early. If an early departure is detected for such an agency then will use the schedule time since the arrival time is likely a mistake.
    private Boolean resetEarlyTerminalDepartures = true;

    // config param: transitclock.traveltimes.maxTravelTimeSegmentLength
    // The longest a travel time segment can be. If a stop path is longer than this distance then it will be divided into multiple travel time segments of even length.
    private Double maxTravelTimeSegmentLength = 250.0;

    // config param: transitclock.traveltimes.minSegmentSpeedMps
    // If a travel time segment is determined to have a lower speed than this value in meters/sec then the travel time will be increased to meet this limit. Purpose is to make sure that don't get invalid travel times due to bad data.
    private Double minSegmentSpeedMps = 0.0;

    // config param: transitclock.traveltimes.maxSegmentSpeedMps
    // If a travel time segment is determined to have a higher speed than this value in meters/second then the travel time will be decreased to meet this limit. Purpose is to make sure that don't get invalid travel times due to bad data.
    private Double maxSegmentSpeedMps = 27.0;

}
