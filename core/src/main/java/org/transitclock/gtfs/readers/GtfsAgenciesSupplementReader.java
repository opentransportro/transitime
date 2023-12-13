/* (C)2023 */
package org.transitclock.gtfs.readers;

import java.text.ParseException;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.gtfs.gtfsStructs.GtfsAgency;
import org.transitclock.utils.csv.CsvBaseReader;

/**
 * GTFS reader for supplemental agency.txt file
 *
 * @author SkiBu Smith
 */
public class GtfsAgenciesSupplementReader extends CsvBaseReader<GtfsAgency> {

    public GtfsAgenciesSupplementReader(String dirName) {
        super(dirName, "agency.txt", false, true);
    }

    @Override
    public GtfsAgency handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        return new GtfsAgency(record, supplemental, getFileName());
    }
}
