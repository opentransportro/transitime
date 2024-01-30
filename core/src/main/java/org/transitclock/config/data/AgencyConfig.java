/* (C)2023 */
package org.transitclock.config.data;

import org.transitclock.config.StringConfigValue;

/**
 * Configuration data commonly used for an agency. By splitting out these commonly used parameters
 * they can be accessed without needing to access CoreConfig which logs lots and lots of params and
 * clutters things up.
 *
 * @author SkiBu Smith
 */
public class AgencyConfig {
    /**
     * Specifies the ID of the agency. Used for the database name and in the logback configuration
     * to specify the directory where to put the log files.
     *
     * @return
     */
    public static String getAgencyId() {
        return projectId.getValue();
    }

    private static StringConfigValue projectId = new StringConfigValue(
            "transitclock.core.agencyId",
            null,
            "Specifies the ID of the agency. Used for the database "
                    + "name and in the logback configuration to specify the "
                    + "directory where to put the log files.");
}
