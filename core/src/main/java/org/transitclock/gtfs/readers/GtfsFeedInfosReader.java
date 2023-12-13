/* (C)2023 */
package org.transitclock.gtfs.readers;

import java.text.ParseException;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.gtfs.gtfsStructs.GtfsFeedInfo;
import org.transitclock.utils.csv.CsvBaseReader;

/**
 * GTFS reader for the feed_info.txt file
 *
 * @author SkiBu Smith
 */
public class GtfsFeedInfosReader extends CsvBaseReader<GtfsFeedInfo> {

    public GtfsFeedInfosReader(String dirName) {
        super(dirName, "feed_info.txt", false, false);
    }

    @Override
    public GtfsFeedInfo handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        return new GtfsFeedInfo(record, supplemental, getFileName());
    }
}
