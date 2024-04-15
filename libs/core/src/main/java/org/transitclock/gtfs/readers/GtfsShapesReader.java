/* (C)2023 */
package org.transitclock.gtfs.readers;

import java.text.ParseException;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.gtfs.model.GtfsShape;
import org.transitclock.utils.csv.CsvBaseReader;

/**
 * GTFS reader for the shapes.txt file
 *
 * @author SkiBu Smith
 */
public class GtfsShapesReader extends CsvBaseReader<GtfsShape> {

    public GtfsShapesReader(String dirName) {
        super(dirName, "shapes.txt", false, false);
    }

    @Override
    public GtfsShape handleRecord(CSVRecord record, boolean supplemental) throws ParseException, NumberFormatException {
        return new GtfsShape(record, supplemental, getFileName());
    }
}
