/* (C)2023 */
package org.transitclock.api.data.siri;

import java.text.DateFormat;
import java.util.Date;
import jakarta.xml.bind.annotation.XmlElement;
import org.transitclock.service.dto.IpcVehicle;

/**
 * Describes the trip
 *
 * @author SkiBu Smith
 */
public class SiriFramedVehicleJourneyRef {

    // The GTFS service date for the trip the vehicle is serving
    @XmlElement(name = "DataFrameRef")
    private String dataFrameRef;

    // Trip ID from GTFS
    @XmlElement(name = "DatedVehicleJourneyRef")
    private String datedVehicleJourneyRef;


    /**
     * Need a no-arg constructor for Jersey for JSON. Otherwise get really obtuse "MessageBodyWriter
     * not found for media type=application/json" exception.
     */
    protected SiriFramedVehicleJourneyRef() {}

    /**
     * Constructor
     *
     * @param vehicle
     * @param dateFormatter
     */
    public SiriFramedVehicleJourneyRef(IpcVehicle vehicle, DateFormat dateFormatter) {
        // FIXME Note: dataFrameRef is not correct. It should use
        // the service date, not the GPS time. When assignment spans
        // midnight this will be wrong. But of course this isn't too
        // important because if a client would actually want such info
        // they would want service ID, not the date. Sheesh.
        dataFrameRef = dateFormatter.format(new Date(vehicle.getGpsTime()));
        datedVehicleJourneyRef = vehicle.getTripId();
    }
}
