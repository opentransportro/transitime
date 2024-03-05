/* (C)2023 */
package org.transitclock.config.data;

import org.transitclock.config.StringConfigValue;


public class TraccarConfig {
    /**
     * Traccar properties for log in by "TraccarAVLModule"
     *
     * @return
     */
    public static final StringConfigValue TRACCAREMAIL = new StringConfigValue("transitclock.avl.traccar.email", null,
            "This is the username for the traccar server api.");

    public static final StringConfigValue TRACCARPASSWORD = new StringConfigValue("transitclock.avl.traccar.password",
            null, "This is the password for the traccar server api");

    public static final StringConfigValue TRACCARBASEURL = new StringConfigValue("transitclock.avl.traccar.baseurl",
            null, "This is the url for the traccar server api.");

    public static final StringConfigValue TRACCARSOURCE = new StringConfigValue("transitclock.avl.traccar.source",
            "TRACCAR", "This is the value recorded in the source for the AVL Report.");

}
