/* (C)2023 */
package org.transitclock.logging;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * For providing special logging markers that cause specific behavior. Key addition to Transitime
 * logging is that can easily generate an e-mail when there is an error by using an e-mail "marker".
 * An example is: <code>
 * logger.error(Markers.email(), message);
 * </code>
 *
 * @author SkiBu Smith
 */
public class Markers {
    private static Marker emailMarker = MarkerFactory.getMarker("EMAIL");

    /**
     * When this marker is used with a logging message then the message is e-mailed to the
     * configured users.
     *
     * @return
     */
    public static Marker email() {
        return emailMarker;
    }
}
