/* (C)2023 */
package org.transitclock.ipc.data;

import org.transitclock.core.domain.VehicleToBlockConfig;

import java.io.Serializable;
import java.util.Date;

/**
 * For transmitting via Interprocess Communication vehicle configuration info.
 *
 * @author SkiBu Smith
 */
public class IpcVehicleToBlockConfig implements Serializable {

    /** */
    private static final long serialVersionUID = 8343324870221439002L;

    private final long id;
    private final String vehicleId;
    private final Date validFrom;
    private final Date validTo;
    private final Date assignmentDate;
    private final String blockId;
    private final String tripId;

    /********************** Member Functions **************************/
    public IpcVehicleToBlockConfig(VehicleToBlockConfig vehicleToBlockConfig) {
        this.id = vehicleToBlockConfig.getId();
        this.vehicleId = vehicleToBlockConfig.getVehicleId();
        this.validFrom = vehicleToBlockConfig.getValidFrom();
        this.validTo = vehicleToBlockConfig.getValidTo();
        this.assignmentDate = vehicleToBlockConfig.getAssignmentDate();
        this.tripId = vehicleToBlockConfig.getTripId();
        this.blockId = vehicleToBlockConfig.getBlockId();
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public long getId() {
        return id;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public Date getValidTo() {
        return validTo;
    }

    public Date getAssignmentDate() {
        return assignmentDate;
    }

    public String getBlockId() {
        return blockId;
    }

    public String getTripId() {
        return tripId;
    }
}
