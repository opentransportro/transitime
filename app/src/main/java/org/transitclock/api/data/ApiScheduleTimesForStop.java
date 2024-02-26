/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * Contains the schedule times for a stop for each trip for the route/direction/service. For when
 * outputting stops vertically.
 *
 * @author SkiBu Smith
 */
public class ApiScheduleTimesForStop {

    @XmlAttribute
    private String stopId;

    @XmlAttribute
    private String stopName;

    @XmlElement(name = "time")
    private List<ApiScheduleTime> times;


    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiScheduleTimesForStop() {}

    public ApiScheduleTimesForStop(String stopId, String stopName) {
        this.stopId = stopId;
        this.stopName = stopName;
        this.times = new ArrayList<ApiScheduleTime>();
    }

    public void add(Integer time) {
        this.times.add(new ApiScheduleTime(time));
    }
}
