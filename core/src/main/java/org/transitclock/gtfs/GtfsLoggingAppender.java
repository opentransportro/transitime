/* (C)2023 */
package org.transitclock.gtfs;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import java.util.ArrayList;
import java.util.List;

/**
 * Special logging appender. Idea is to overload the logback logging so that when error() or warn()
 * called can do the usual logging but also record errors and warnings to lists and then be able to
 * output those lists in coherent forms at the end of processing. This important information can
 * then be more effectively presented to the user, hopefully causing them to fix the configuration
 * problems.
 *
 * <p>To use create a logbackGtfs.xml file with the following: <appender name="GTFS_CUSTOM_APPENDER"
 * class="org.transitclock.gtfs.GtfsLoggingAppender"> </appender>
 *
 * <p><logger name="org.transitclock.gtfs" level="debug" additivity="true"> <appender-ref
 * ref="GTFS_CUSTOM_APPENDER" /> </logger>
 *
 * <p>And use the special gtfsLogback by setting a VM arg to something like:
 * -Dlogback.configurationFile=C:/Users/Mike/git/testProject/testProject/src/main/config/logbackGtfs.xml
 *
 * @author SkiBu Smith
 */
public class GtfsLoggingAppender extends AppenderBase<ILoggingEvent> {
    // Where to store the errors and warnings
    private static List<String> warnings = new ArrayList<String>();
    private static List<String> errors = new ArrayList<String>();

    /********************** Member Functions **************************/

    /**
     * Logs WARN and ERROR level messages
     *
     * @see ch.qos.logback.core.AppenderBase#append(java.lang.Object)
     */
    @Override
    protected void append(ILoggingEvent event) {
        Level level = event.getLevel();
        if (level == Level.WARN) {
            warnings.add(event.getFormattedMessage());
        } else if (level == Level.ERROR) {
            errors.add(event.getFormattedMessage());
        }
    }

    /**
     * @return list of errors processed so far
     */
    public static List<String> getErrors() {
        return errors;
    }

    /**
     * @return list of warnings processed so far
     */
    public static List<String> getWarnings() {
        return warnings;
    }

    /** For debugging. Dumps all the errors and warnings to stdout */
    public static void outputMessagesToSysErr() {
        System.err.println("Errors:");
        if (errors.isEmpty()) System.err.println("  None");
        for (String s : errors) System.err.println("  " + s);

        System.err.println();

        System.err.println("Warnings:");
        if (warnings.isEmpty()) System.err.println("  None");
        for (String s : warnings) System.err.println("  " + s);
    }
}
