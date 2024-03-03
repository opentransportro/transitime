/* (C)2023 */
package org.transitclock.monitoring;

import java.io.Serializable;

/**
 * Contains status for an individual monitor. To be passed via IPC to client.
 *
 * @author SkiBu Smith
 */
public class MonitorResult implements Serializable {

    private final String type;
    private final String message;


    public MonitorResult(String type, String message) {
        this.type = type;
        this.message = message;
    }

    @Override
    public String toString() {
        return "MonitorResult [type=" + type + ", message=" + message + "]";
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
