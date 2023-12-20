/* (C)2023 */
package org.transitclock.gtfs.readers;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.transitclock.gtfs.GtfsAgency;
import org.transitclock.utils.csv.CsvBaseReader;

import java.text.ParseException;

@Component
public class GtfsAgenciesSupplementReader extends CsvBaseReader<GtfsAgency> {

    public GtfsAgenciesSupplementReader(String dirName) {
        super(dirName, "agency.txt", false, true);
    }

    @Override
    public GtfsAgency handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        return new GtfsAgency(record, supplemental, getFileName());
    }
}
