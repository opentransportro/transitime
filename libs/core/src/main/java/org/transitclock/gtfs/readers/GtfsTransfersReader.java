/* (C)2023 */
package org.transitclock.gtfs.readers;

import java.text.ParseException;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.gtfs.model.GtfsTransfer;
import org.transitclock.utils.csv.CsvBaseReader;

/**
 * @author SkiBu Smith
 */
public class GtfsTransfersReader extends CsvBaseReader<GtfsTransfer> {

    public GtfsTransfersReader(String dirName) {
        super(dirName, "transfers.txt", false, false);
    }

    @Override
    public GtfsTransfer handleRecord(CSVRecord record, boolean supplemental)
            throws ParseException, NumberFormatException {
        return new GtfsTransfer(record, supplemental, getFileName());
    }
}
