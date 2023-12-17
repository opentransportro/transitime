/* (C)2023 */
package org.transitclock.gtfs.gtfsStructs;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.utils.csv.CsvBase;

/**
 * A GTFS fare_rules object.
 *
 * @author SkiBu Smith
 */
@EqualsAndHashCode
@ToString
@Getter
public class GtfsFareRule extends CsvBase {

    private final String fareId;
    private final String routeId;
    private final String originId;
    private final String destinationId;
    private final String containsId;

    /********************** Member Functions **************************/

    /**
     * Creates a GtfsFareRule object by reading the data from the CSVRecord.
     *
     * @param record
     * @param supplemental
     * @param fileName for logging errors
     */
    public GtfsFareRule(CSVRecord record, boolean supplemental, String fileName) {
        super(record, supplemental, fileName);

        fareId = getRequiredValue(record, "fare_id");
        routeId = getOptionalValue(record, "route_id");
        originId = getOptionalValue(record, "origin_id");
        destinationId = getOptionalValue(record, "destination_id");
        containsId = getOptionalValue(record, "contains_id");
    }

}
