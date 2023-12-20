/* (C)2023 */
package org.transitclock.gtfs.readers;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.transitclock.gtfs.GtfsShape;
import org.transitclock.utils.csv.CsvBaseReader;

import java.text.ParseException;


@Component
public class GtfsShapesReader extends CsvBaseReader<GtfsShape> {

    public GtfsShapesReader(String dirName) {
        super(dirName, "shapes.txt", false, false);
    }

    @Override
    public GtfsShape handleRecord(CSVRecord record, boolean supplemental) throws ParseException, NumberFormatException {
        return new GtfsShape(record, supplemental, getFileName());
    }
}
