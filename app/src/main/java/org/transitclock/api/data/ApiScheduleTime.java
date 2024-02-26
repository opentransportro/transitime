/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import org.transitclock.utils.Time;

/**
 * Represents a schedule time for a stop. Intended to be used for displaying a schedule for a route.
 *
 * @author SkiBu Smith
 */
public class ApiScheduleTime {

    @XmlAttribute
    private String timeStr;

    @XmlAttribute
    private Integer timeSecs;


    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiScheduleTime() {}

    public ApiScheduleTime(Integer time) {
        this.timeStr = time == null ? null : Time.timeOfDayShortStr(time);
        this.timeSecs = time;
    }
}
