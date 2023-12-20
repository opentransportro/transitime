/* (C)2023 */
package org.transitclock.utils;

import org.transitclock.applications.Core;

import java.text.DecimalFormat;

/**
 * Very simple class for timing duration of events. Main use is at the beginning of the process to
 * construct an IntervalTimer and then at the end to call <code>elapsedMsec()</code> to determine
 * elapsed time.
 *
 * @author SkiBu Smith
 */
public class PlaybackIntervalTimer {

    // The time the interval timer created or reset
    private long initialtime;

    private static DecimalFormat decimalFormat = new DecimalFormat("#.###");

    /********************** Member Functions **************************/

    /** Records the current time so that elapsed time can later be determined. */
    public PlaybackIntervalTimer() {
        initialtime = Core.getInstance().getSystemTime();
    }

    /** Resets the timer to the current time. */
    public void resetTimer() {
        initialtime = Core.getInstance().getSystemTime();
    }

    /**
     * Time elapsed in msec since IntervalTimer created or resetTimer() called
     *
     * @return elapsed time in msec
     */
    public long elapsedMsec() {
        return Math.abs(Core.getInstance().getSystemTime() - initialtime);
    }

    /**
     * For outputting elapsed time in milliseconds
     *
     * @return String of elapsed time in milliseconds
     */
    private String elapsedMsecStr() {

        return "" + (Core.getInstance().getSystemTime() - initialtime);
    }

    /**
     * toString() is defined so that it works well when debug logging. This way can pass in
     * reference to the timer object. Only if debug logging is enabled will the toString() be called
     * causing the string to actually be generated.
     */
    @Override
    public String toString() {
        return elapsedMsecStr();
    }
}
