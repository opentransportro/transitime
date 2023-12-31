/* (C)2023 */
package org.transitclock.avl.csv;

import java.text.ParseException;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.utils.csv.CsvBaseReader;

/**
 * For reading in AVL data from a CSV file.
 *
 * @author SkiBu Smith
 */
public class AvlCsvReader extends CsvBaseReader<AvlReport> {

    public AvlCsvReader(String fileName) {
        super(fileName);
    }

    @Override
    public AvlReport handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        return AvlCsvRecord.getAvlReport(record, getFileName());
    }
}
