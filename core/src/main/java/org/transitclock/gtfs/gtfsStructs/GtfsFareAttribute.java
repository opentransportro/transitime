/* (C)2023 */
package org.transitclock.gtfs.gtfsStructs;

import lombok.Getter;
import lombok.ToString;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.utils.csv.CsvBase;

/**
 * A GTFS fare_attributes object.
 *
 * @author SkiBu Smith
 */
@ToString
@Getter
public class GtfsFareAttribute extends CsvBase {

    private final String fareId;
    private final float price;
    private final String currencyType;
    private final String paymentMethod;
    private final String transfers;
    private final Integer transferDuration;

    /********************** Member Functions **************************/

    /**
     * Creates a GtfsFareAttribute object by reading the data from the CSVRecord.
     *
     * @param record
     * @param supplemental
     * @param fileName for logging errors
     */
    public GtfsFareAttribute(CSVRecord record, boolean supplemental, String fileName) throws NumberFormatException {
        super(record, supplemental, fileName);

        fareId = getRequiredValue(record, "fare_id");
        price = Float.parseFloat(getRequiredValue(record, "price"));
        currencyType = getRequiredValue(record, "currency_type");
        paymentMethod = getRequiredValue(record, "payment_method");
        // Note: "transfers" is listed as required in the GTFS doc but it can
        // be empty. Therefore it is actually optional.
        transfers = getOptionalValue(record, "transfers");

        String transferDurationStr = getOptionalValue(record, "transfer_duration");
        if (transferDurationStr != null) transferDuration = Integer.parseInt(transferDurationStr);
        else transferDuration = null;
    }

}
