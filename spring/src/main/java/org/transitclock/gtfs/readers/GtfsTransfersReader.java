/* (C)2023 */
package org.transitclock.gtfs.readers;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.transitclock.gtfs.GtfsTransfer;
import org.transitclock.utils.csv.CsvBaseReader;

import java.text.ParseException;


@Component
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
