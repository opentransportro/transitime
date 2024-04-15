/* (C)2023 */
package org.transitclock.gtfs.readers;

import java.text.ParseException;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.gtfs.model.GtfsFareRule;
import org.transitclock.utils.csv.CsvBaseReader;

/**
 * GTFS reader for the fare_rules.txt file
 *
 * @author SkiBu Smith
 */
public class GtfsFareRulesReader extends CsvBaseReader<GtfsFareRule> {

    public GtfsFareRulesReader(String dirName) {
        super(dirName, "fare_rules.txt", false, false);
    }

    @Override
    public GtfsFareRule handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        return new GtfsFareRule(record, supplemental, getFileName());
    }
}
