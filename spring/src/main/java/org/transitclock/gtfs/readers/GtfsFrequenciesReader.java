/* (C)2023 */
package org.transitclock.gtfs.readers;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.transitclock.gtfs.GtfsFrequency;
import org.transitclock.utils.csv.CsvBaseReader;

import java.text.ParseException;


@Component
public class GtfsFrequenciesReader extends CsvBaseReader<GtfsFrequency> {

    public GtfsFrequenciesReader(String dirName) {
        super(dirName, "frequencies.txt", false, false);
    }

    @Override
    public GtfsFrequency handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        return new GtfsFrequency(record, supplemental, getFileName());
    }
}
