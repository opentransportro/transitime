/* (C)2023 */
package org.transitclock.gtfs.readers;

import java.text.ParseException;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.gtfs.GtfsData;
import org.transitclock.gtfs.model.GtfsFrequency;
import org.transitclock.utils.csv.CsvBaseReader;

/**
 * GTFS reader for the frequencies.txt file
 *
 * @author SkiBu Smith
 */
public class GtfsFrequenciesReader extends CsvBaseReader<GtfsFrequency> {

    public GtfsFrequenciesReader(String dirName) {
        super(dirName, "frequencies.txt", false, false);
    }

    @Override
    public GtfsFrequency handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        if (GtfsData.tripNotFiltered(record.get("trip_id")))
            return new GtfsFrequency(record, supplemental, getFileName());
        else return null;
    }
}
