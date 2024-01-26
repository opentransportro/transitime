/* (C)2023 */
package org.transitclock.utils;

/**
 * Very simple class for timing duration of events. Main use is at the beginning of the process to
 * construct an IntervalTimer and then at the end to call <code>elapsedMsec()</code> to determine
 * elapsed time.
 *
 * @author SkiBu Smith
 */
public class PlaybackIntervalTimer {

    private long initialtime;


    /** Records the current time so that elapsed time can later be determined. */
    public PlaybackIntervalTimer() {
        initialtime = SystemTime.getMillis();
    }

    /** Resets the timer to the current time. */
    public void resetTimer() {
        initialtime = SystemTime.getMillis();
    }

    /**
     * Time elapsed in msec since IntervalTimer created or resetTimer() called
     *
     * @return elapsed time in msec
     */
    public long elapsedMsec() {
        return Math.abs(SystemTime.getMillis() - initialtime);
    }

    /**
     * For outputting elapsed time in milliseconds
     *
     * @return String of elapsed time in milliseconds
     */
    private String elapsedMsecStr() {
        return "" + (SystemTime.getMillis() - initialtime);
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
