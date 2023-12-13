/* (C)2023 */
package org.transitclock.gtfs.gtfsStructs;

import org.apache.commons.csv.CSVRecord;
import org.transitclock.utils.csv.CsvBase;

/**
 * @author SkiBu Smith
 */
public class GtfsTransfer extends CsvBase {

    private final String fromStopId;
    private final String toStopId;
    private final String transferType;
    private final Integer minTransferTime;

    /********************** Member Functions **************************/

    /**
     * @param record
     * @param supplemental
     * @param fileName for logging errors
     */
    public GtfsTransfer(CSVRecord record, boolean supplemental, String fileName) throws NumberFormatException {
        super(record, supplemental, fileName);

        fromStopId = getRequiredValue(record, "from_stop_id");
        toStopId = getRequiredValue(record, "to_stop_id");
        transferType = getRequiredValue(record, "transfer_type");

        String timeStr = getOptionalValue(record, "min_transfer_time");
        if (timeStr != null) minTransferTime = Integer.parseInt(timeStr);
        else minTransferTime = null;
    }

    public String getFromStopId() {
        return fromStopId;
    }

    public String getToStopId() {
        return toStopId;
    }

    public String getTransferType() {
        return transferType;
    }

    public Integer getMinTransferTime() {
        return minTransferTime;
    }

    @Override
    public String toString() {
        return "GtfsTransfer ["
                + "lineNumber="
                + lineNumber
                + ", fromStopId="
                + fromStopId
                + ", toStopId="
                + toStopId
                + ", transferType="
                + transferType
                + ", minTransferTime="
                + minTransferTime
                + "]";
    }
}
