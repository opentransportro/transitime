/* (C)2023 */
package org.transitclock.gtfs.readers;

import java.text.ParseException;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.gtfs.model.GtfsShape;
import org.transitclock.utils.csv.CsvBaseReader;

/**
 * GTFS reader for supplemental shapes.txt file. Useful for moving or deleting shape points.
 *
 * @author Michael Smith
 */
public class GtfsShapesSupplementReader extends CsvBaseReader<GtfsShape> {

    public GtfsShapesSupplementReader(String dirName) {
        super(dirName, "shapes.txt", false, true);
    }

    @Override
    public GtfsShape handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        return new GtfsShape(record, supplemental, getFileName());
    }
}
