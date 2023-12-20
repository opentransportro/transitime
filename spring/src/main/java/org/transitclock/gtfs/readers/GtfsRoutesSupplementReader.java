/* (C)2023 */
package org.transitclock.gtfs.readers;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.transitclock.gtfs.GtfsRoute;
import org.transitclock.utils.csv.CsvBaseReader;

import java.text.ParseException;


@Component
public class GtfsRoutesSupplementReader extends CsvBaseReader<GtfsRoute> {

    public GtfsRoutesSupplementReader(String dirName) {
        super(dirName, "routes.txt", false, true);
    }

    @Override
    public GtfsRoute handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        return new GtfsRoute(record, supplemental, getFileName());
    }
}
