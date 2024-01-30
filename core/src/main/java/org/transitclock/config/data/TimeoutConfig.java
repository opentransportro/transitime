package org.transitclock.config.data;

import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.utils.Time;

public class TimeoutConfig {
    public static final IntegerConfigValue pollingRateSecs = new IntegerConfigValue(
            "transitclock.timeout.pollingRateSecs",
            30,
            "Specifies in seconds how frequently the TimeoutHandler "
                    + "should actually look for timeouts. Don't want to do "
                    + "this too frequently because because "
                    + "TimeoutHandler.handlePossibleTimeout() is called with "
                    + "every new AVL report and it has to look at every "
                    + "vehicle to see if has been timed out.");

    public static final IntegerConfigValue allowableNoAvlSecs = new IntegerConfigValue(
            "transitclock.timeout.allowableNoAvlSecs",
            6 * Time.SEC_PER_MIN,
            "For AVL timeouts. If don't get an AVL report for the "
                    + "vehicle in this amount of time in seconds then the "
                    + "vehicle will be made non-predictable.");

    public static final IntegerConfigValue allowableNoAvlAfterSchedDepartSecs = new IntegerConfigValue(
            "transitclock.timeout.allowableNoAvlAfterSchedDepartSecs",
            6 * Time.SEC_PER_MIN,
            "If a vehicle is at a wait stop, such as "
                    + "sitting at a terminal, and doesn't provide an AVL report "
                    + "for this number of seconds then the vehicle is made "
                    + "unpredictable. Important because sometimes vehicles "
                    + "don't report AVL at terminals because they are powered "
                    + "down. But don't want to continue to provide predictions "
                    + "for long after scheduled departure time if vehicle "
                    + "taken out of service.");

    public static final BooleanConfigValue removeTimedOutVehiclesFromVehicleDataCache = new BooleanConfigValue(
            "transitclock.timeout.removeTimedOutVehiclesFromVehicleDataCache",
            false,
            "When timing out vehicles, the default behavior is to make "
                    + "the vehicle unpredictable but leave it in the "
                    + "VehicleDataCache. When set to true, a timeout will also "
                    + "remove the vehicle from the VehicleDataCache. This can "
                    + "be useful in situations where it is not desirable to "
                    + "include timed out vehicles in data feeds, e.g. the GTFS "
                    + "Realtime vehicle positions feed.");
}
