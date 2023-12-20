/* (C)2023 */
package org.transitclock.utils;

/**
 * Implements SystemTime and returns the system current time when get() is called.
 *
 * @author SkiBu Smith
 */
public class SystemCurrentTime implements SystemTime {

    /**
     * @return System epoch time in milliseconds
     */
    @Override
    public long get() {
        return System.currentTimeMillis();
    }
}
