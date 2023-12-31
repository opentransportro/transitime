/* (C)2023 */
package org.transitclock.utils;

import java.text.DecimalFormat;

/**
 * Very simple class for timing duration of events. Main use is at the beginning of the process to
 * construct an IntervalTimer and then at the end to call <code>elapsedMsec()</code> to determine
 * elapsed time.
 *
 * @author SkiBu Smith
 */
public class IntervalTimer {
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.###");

    // The time the interval timer created or reset
    private long initialNanotime;



    /** Records the current time so that elapsed time can later be determined. */
    public IntervalTimer() {
        initialNanotime = System.nanoTime();
    }

    /** Resets the timer to the current time. */
    public void resetTimer() {
        initialNanotime = System.nanoTime();
    }

    /**
     * Time elapsed in msec since IntervalTimer created or resetTimer() called
     *
     * @return elapsed time in msec
     */
    public long elapsedMsec() {
        return (System.nanoTime() - initialNanotime) / Time.NSEC_PER_MSEC;
    }

    /**
     * Time elapsed in nanoseconds since IntervalTimer created or resetTimer() called
     *
     * @return elapsed time in nanoseconds
     */
    public long elapsedNanoSec() {
        return System.nanoTime() - initialNanotime;
    }

    /**
     * For outputting elapsed time in milliseconds with 3 digits after the decimal point, as in
     * 123.456 msec.
     *
     * @return String of elapsed time in milliseconds
     */
    public String elapsedMsecStr() {
        long microSecs = (System.nanoTime() - initialNanotime) / 1000;
        return decimalFormat.format((double) microSecs / 1000);
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
