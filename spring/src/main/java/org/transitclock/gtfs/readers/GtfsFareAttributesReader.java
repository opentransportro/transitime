/* (C)2023 */
package org.transitclock.gtfs.readers;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.transitclock.gtfs.GtfsFareAttribute;
import org.transitclock.utils.csv.CsvBaseReader;

import java.text.ParseException;


@Component
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
