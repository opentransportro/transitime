/* (C)2023 */
package org.transitclock.utils;

/**
 * So that can have access to a system time whether in normal mode where the system clock can be
 * used, or playback mode where use the last AVL time.
 *
 * @author SkiBu Smith
 */
public interface SystemTime {

    /**
     * @return Returns current system epoch time in milliseconds. If in playback mode or such this
     *     might not actually be the clock time for the computer.
     */
    public abstract long get();
}
