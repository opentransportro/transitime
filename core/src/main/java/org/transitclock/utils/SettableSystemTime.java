/* (C)2023 */
package org.transitclock.utils;

/**
 * For when need a system time that can be set to a specific value. For playback or for debugging.
 *
 * @author SkiBu Smith
 */
public class SettableSystemTime implements SystemTime {
    private long time;

    /********************** Member Functions **************************/
    public SettableSystemTime(long time) {
        this.time = time;
    }

    public SettableSystemTime() {
        this.time = 0;
    }

    /* (non-Javadoc)
     * @see org.transitclock.utils.SystemTime#get()
     */
    @Override
    public long get() {
        return time;
    }

    /**
     * So can update the time
     *
     * @param time
     */
    public void set(long time) {
        this.time = time;
    }
}
