package org.transitclock.config.data;

import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.DoubleConfigValue;

public class TraveltimesConfig {

//    public static boolean shouldResetEarlyTerminalDepartures() {
//        return resetEarlyTerminalDepartures.getValue();
//    }
//
//    // For when determining stop times. Throws out
//    // outliers if they are less than 0.7 or greater than 1/0.7
//    // of the average.
//    public static DoubleConfigValue fractionLimitForStopTimes = new DoubleConfigValue(
//            "transitclock.traveltimes.fractionLimitForStopTimes",
//            0.7,
//            "For when determining stop times. Throws out outliers.");

//    // For when determining travel times for segments. Throws out
//    // outliers if they are less than 0.7 or greater than 1/0.7
//    // of the average.
//    public static DoubleConfigValue fractionLimitForTravelTimes = new DoubleConfigValue(
//            "transitclock.traveltimes.fractionLimitForTravelTimes",
//            0.7,
//            "For when determining travel times. Throws out outliers.");
//
//    public static BooleanConfigValue resetEarlyTerminalDepartures = new BooleanConfigValue(
//            "transitclock.travelTimes.resetEarlyTerminalDepartures",
//            true,
//            "For some agencies vehicles won't be departing terminal "
//                    + "early. If an early departure is detected for such an "
//                    + "agency then will use the schedule time since the "
//                    + "arrival time is likely a mistake.");
//
//    public static double getMaxTravelTimeSegmentLength() {
//        return maxTravelTimeSegmentLength.getValue();
//    }
//
//    public static DoubleConfigValue maxTravelTimeSegmentLength = new DoubleConfigValue(
//            "transitclock.traveltimes.maxTravelTimeSegmentLength",
//            250.0,
//            "The longest a travel time segment can be. If a stop path "
//                    + "is longer than this distance then it will be divided "
//                    + "into multiple travel time segments of even length.");
//
//    public static double getMinSegmentSpeedMps() {
//        return minSegmentSpeedMps.getValue();
//    }
//
//    public static DoubleConfigValue minSegmentSpeedMps = new DoubleConfigValue(
//            "transitclock.traveltimes.minSegmentSpeedMps",
//            0.0,
//            "If a travel time segment is determined to have a lower "
//                    + "speed than this value in meters/sec then the travel time"
//                    + " will be increased to meet this limit. Purpose is to "
//                    + "make sure that don't get invalid travel times due to "
//                    + "bad data.");
//
//    public static DoubleConfigValue maxSegmentSpeedMps = new DoubleConfigValue(
//            "transitclock.traveltimes.maxSegmentSpeedMps",
//            27.0, // 27.0m/s = 60mph
//            "If a travel time segment is determined to have a higher "
//                    + "speed than this value in meters/second then the travel "
//                    + "time will be decreased to meet this limit. Purpose is "
//                    + "to make sure that don't get invalid travel times due to "
//                    + "bad data.");
}
