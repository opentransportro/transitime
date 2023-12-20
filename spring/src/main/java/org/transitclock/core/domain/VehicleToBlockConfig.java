/* (C)2023 */
package org.transitclock.core.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Data
@Document(collection = "VehicleToBlockConfigs")
public class VehicleToBlockConfig implements Serializable {

    @Id
    private final String vehicleId;

    private final Date assignmentDate;

    private String blockId;

    private String tripId;

    private Date validFrom;

    private Date validTo;

    public VehicleToBlockConfig(String vehicleId,
                                String blockId,
                                String tripId,
                                Date assignmentDate,
                                Date validFrom,
                                Date validTo) {
        this.vehicleId = vehicleId;
        this.blockId = blockId;
        this.tripId = tripId;
        this.assignmentDate = assignmentDate;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }
}
