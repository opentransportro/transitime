/* (C)2023 */
package org.transitclock.gtfs.readers;

import java.text.ParseException;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.gtfs.model.GtfsFareAttribute;
import org.transitclock.utils.csv.CsvBaseReader;

/**
 * GTFS reader for the fare_attributes.txt file
 *
 * @author SkiBu Smith
 */
public class GtfsFareAttributesReader extends CsvBaseReader<GtfsFareAttribute> {

    public GtfsFareAttributesReader(String dirName) {
        super(dirName, "fare_attributes.txt", false, false);
    }

    @Override
    public GtfsFareAttribute handleRecord(CSVRecord record, boolean supplemental)
            throws ParseException, NumberFormatException {
        return new GtfsFareAttribute(record, supplemental, getFileName());
    }
}
