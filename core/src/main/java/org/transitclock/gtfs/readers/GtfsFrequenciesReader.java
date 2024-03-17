/* (C)2023 */
package org.transitclock.gtfs.readers;

import java.text.ParseException;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.gtfs.GtfsData;
import org.transitclock.gtfs.GtfsFilter;
import org.transitclock.gtfs.model.GtfsFrequency;
import org.transitclock.utils.csv.CsvBaseReader;

/**
 * GTFS reader for the frequencies.txt file
 *
 * @author SkiBu Smith
 */
public class GtfsFrequenciesReader extends CsvBaseReader<GtfsFrequency> {
    private final GtfsFilter filter;
    public GtfsFrequenciesReader(String dirName, GtfsFilter filter) {
        super(dirName, "frequencies.txt", false, false);
        this.filter = filter;
    }

    @Override
    public GtfsFrequency handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        if (filter.tripNotFiltered(record.get("trip_id")))
            return new GtfsFrequency(record, supplemental, getFileName());
        else return null;
    }
}
