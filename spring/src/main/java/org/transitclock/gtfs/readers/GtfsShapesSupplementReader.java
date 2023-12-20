/* (C)2023 */
package org.transitclock.gtfs.readers;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.transitclock.gtfs.GtfsShape;
import org.transitclock.utils.csv.CsvBaseReader;

import java.text.ParseException;


@Component
public class GtfsShapesSupplementReader extends CsvBaseReader<GtfsShape> {

    public GtfsShapesSupplementReader(String dirName) {
        super(dirName, "shapes.txt", false, true);
    }

    @Override
    public GtfsShape handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        return new GtfsShape(record, supplemental, getFileName());
    }
}
