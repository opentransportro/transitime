/* (C)2023 */
package org.transitclock.core.domain;

import lombok.Data;
import lombok.experimental.Delegate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.transitclock.gtfs.GtfsTransfer;

import java.io.Serializable;

/**
 * Contains data from the transfers.txt GTFS file. This class is for reading/writing that data to
 * the db.
 *
 * @author SkiBu Smith
 */
@Data
@Document(collection = "Transfers")
public class Transfer implements Serializable {

    @Data
    public static class Key {
        private final int configRev;
        private final String fromStopId;
        private final String toStopId;
    }

    @Delegate
    private final Key key;

    private final String transferType;

    private final Integer minTransferTime;

    /**
     * Constructor
     *
     * @param configRev
     * @param gt
     */
    public Transfer(int configRev, GtfsTransfer gt) {
        this.key = new Key(configRev, gt.getFromStopId(), gt.getToStopId());
        this.transferType = gt.getTransferType();
        this.minTransferTime = gt.getMinTransferTime();
    }
}
