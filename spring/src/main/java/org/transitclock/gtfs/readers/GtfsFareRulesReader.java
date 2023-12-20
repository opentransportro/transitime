/* (C)2023 */
package org.transitclock.gtfs.readers;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.transitclock.gtfs.GtfsFareRule;
import org.transitclock.utils.csv.CsvBaseReader;

import java.text.ParseException;


@Component
public class GtfsFareRulesReader extends CsvBaseReader<GtfsFareRule> {

    public GtfsFareRulesReader(String dirName) {
        super(dirName, "fare_rules.txt", false, false);
    }

    @Override
    public GtfsFareRule handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        return new GtfsFareRule(record, supplemental, getFileName());
    }
}
