/* (C)2023 */
package org.transitclock.gtfs.readers;

import java.text.ParseException;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.gtfs.gtfsStructs.GtfsRoute;
import org.transitclock.utils.csv.CsvBaseReader;

/**
 * GTFS reader for the routeSupplement.txt file
 *
 * @author SkiBu Smith
 */
public class GtfsRoutesSupplementReader extends CsvBaseReader<GtfsRoute> {

    public GtfsRoutesSupplementReader(String dirName) {
        super(dirName, "routes.txt", false, true);
    }

    @Override
    public GtfsRoute handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        return new GtfsRoute(record, supplemental, getFileName());
    }
}
