package org.transitclock.utils;

public class ExceptionUtils {
    /**
     * Recursively finds the root cause of the throwable. Useful for complicated inconsistent
     * exceptions like what one gets with JDBC drivers.
     *
     * @param t The throwable to get the root cause of
     * @return The root cause of the throwable passed in
     */
    public static Throwable getRootCause(Throwable t) {
        Throwable subcause = t.getCause();
        if (subcause == null || subcause == t) {
            return t;
        }

        return getRootCause(subcause);
    }
}
