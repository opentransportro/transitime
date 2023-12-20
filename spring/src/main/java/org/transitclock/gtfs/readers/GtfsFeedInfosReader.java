/* (C)2023 */
package org.transitclock.gtfs.readers;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.transitclock.gtfs.GtfsFeedInfo;
import org.transitclock.utils.csv.CsvBaseReader;

import java.text.ParseException;


@Component
public class GtfsFeedInfosReader extends CsvBaseReader<GtfsFeedInfo> {

    public GtfsFeedInfosReader(String dirName) {
        super(dirName, "feed_info.txt", false, false);
    }

    @Override
    public GtfsFeedInfo handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        return new GtfsFeedInfo(record, supplemental, getFileName());
    }
}
