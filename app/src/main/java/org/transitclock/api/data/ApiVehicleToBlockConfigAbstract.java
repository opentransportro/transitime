/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.Data;
import org.transitclock.service.dto.IpcVehicleToBlockConfig;

import java.util.Date;

/**
 * This class exists so that can have multiple subclasses that inherent from each other while still
 * being able to set the propOrder for each class. Specifically, ApiVehicleDetails is supposed to be
 * a subclass of ApiVehicle. But want the vehicle id to be output as the first attribute. But the
 * attributes for the subclass are output first and one can't normally set the propOrder of parent
 * class attributes. One gets an internal error if one tries to do so.
 *
 * <p>The solution is to use the abstract class ApiVehicleAbstract. Then can implement ApiVehicle
 * and ApiVehicleDetails to inherit from ApiVehicleAbstract and those classes can each set propOrder
 * as desired. Yes, this is rather complicated, but it works.
 *
 * @author SkiBu Smith
 */
@Data
@XmlTransient
public abstract class ApiVehicleToBlockConfigAbstract {

    @XmlAttribute
    protected long id;

    @XmlAttribute
    protected String vehicleId;

    @XmlAttribute
    protected Date validFrom;

    @XmlAttribute
    protected Date validTo;

    @XmlAttribute
    protected Date assignmentDate;

    @XmlAttribute
    protected String tripId;

    @XmlAttribute
    protected String blockId;

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiVehicleToBlockConfigAbstract() {}

    /**
     * Takes a Vehicle object for client/server communication and constructs a ApiVehicle object for
     * the API.
     *
     * @param vehicle
     * @param uiType If should be labeled as "minor" in output for UI.
     */
    public ApiVehicleToBlockConfigAbstract(IpcVehicleToBlockConfig vTBC) {
        id = vTBC.getId();
        vehicleId = vTBC.getVehicleId();
        tripId = vTBC.getTripId();
        blockId = vTBC.getBlockId();
        validFrom = vTBC.getValidFrom();
        validTo = vTBC.getValidTo();
        assignmentDate = vTBC.getAssignmentDate();
    }
}
