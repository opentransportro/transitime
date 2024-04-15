/* (C)2023 */
package org.transitclock.utils.csv;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.transitclock.utils.IntervalTimer;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

/**
 * For parsing a CSV file. Does all of the hard work. This class is abstract because it needs to be
 * subclassed to read in specific CSV file type.
 *
 * @author SkiBu Smith
 */
@Getter
@Slf4j
public abstract class CsvBaseReader<T> {

    // Full file name of CSV file to be read
    private final String fileName;

    // Keeps track whether this file is required or not as per
    // the CSV spec.
    private final boolean required;

    // Whether file is a supplemental one or not. For supplemental
    // files some of the elements specified as required in the CSV
    // spec can actually be missing since the data from supplemental
    // file is going to be combined with the main file.
    private final boolean supplemental;

    // The CSV objects read from the file
    protected List<T> gtfsObjects;

    protected CsvBaseReader(String dirName, String fileName, boolean required, boolean supplemental) {
        this.fileName = dirName + "/" + fileName;
        this.required = required;
        this.supplemental = supplemental;
    }

    /**
     * Constructor with fewer params. More useful for non-CSV files. Sets required to true and
     * supplemental to false.
     *
     * @param fileName
     */
    protected CsvBaseReader(String fileName) {
        this.fileName = fileName;
        this.required = true;
        this.supplemental = false;
    }

    /**
     * Called for every record in file. Must be overridden by subclass since an object of the
     * appropriate type needs to be created.
     *
     * @param record
     * @return The created GTFS object, or null if object filtered out
     */
    protected abstract T handleRecord(CSVRecord record, boolean supplemental)
            throws ParseException, NumberFormatException;

    /**
     * Parse the CSV file. Reads in the header info and then each line. Calls the abstract
     * handleRecord() method for each record. Adds each resulting CSV object to the gtfsObjecgts
     * array.
     */
    private void parse() {
        CSVRecord record = null;
        try {
            IntervalTimer timer = new IntervalTimer();

            logger.debug("Parsing CSV file {} ...", fileName);

            // Open the file for reading. Use UTF-8 format since that will work
            // for both regular ASCII format and UTF-8 extended format files
            // since UTF-8 was designed to be backwards compatible with ASCII.
            // This way will work for Chinese and other character sets. Use
            // InputStreamReader so can specify that using UTF-8 format. Use
            // BufferedReader so that can determine if first character is an
            // optional BOM (Byte Order Mark) character used to indicate that
            // file is in UTF-8 format. BufferedReader allows us to read in
            // first character and then discard if it is a BOM character or
            // reset the reader to back to the beginning if it is not. This
            // way the CSV parser will process the file starting with the first
            // true character.

            Reader in =
                    new BufferedReader(new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8));

            // Deal with the possible BOM character at the beginning of the file
            in.mark(1);
            int firstRead = in.read();
            final int BOM_CHARACTER = 0xFEFF;
            if (firstRead != BOM_CHARACTER) in.reset();

            // Get ready to parse the CSV file.
            // Allow lines to be comments if they start with "-" so that can
            // easily comment out problems and also test what happens when
            // certain data is missing. Using the '-' character so can
            // comment out line that starts with "--", which is what is
            // used for SQL.
            CSVFormat formatter = CSVFormat.Builder
                    .create()
                    .setHeader()
                    .setCommentMarker('-')
                    .build();

            // Parse the file
            Iterable<CSVRecord> records = formatter.parse(in);

            logger.debug("Finished CSV parsing of file {}. Took {} msec.", fileName, timer.elapsedMsec());

            int lineNumberWhenLogged = 0;
            timer = new IntervalTimer();
            IntervalTimer loggingTimer = new IntervalTimer();

            for (CSVRecord strings : records) {
                // Determine the record to process
                record = strings;

                // If blank line then skip it. This way avoid error messages since
                // expected data column won't exist
                if (record.size() == 0) continue;

                // Process the record using appropriate handler
                // and create the corresponding CSV object
                T gtfsObject;
                try {
                    gtfsObject = handleRecord(record, supplemental);
                } catch (ParseException e) {
                    logger.error(
                            "ParseException occurred for record {} "
                                    + "(comment lines not included when determing record #) for "
                                    + "filename {} . {}",
                            record.getRecordNumber(),
                            fileName,
                            e.getMessage());

                    // Continue even though there was an error so that all errors
                    // logged at once.
                    continue;
                } catch (NumberFormatException e) {
                    logger.error(
                            "NumberFormatException occurred for record {} "
                                    + "(comment lines not included when determing record #) "
                                    + "for filename {} . {}",
                            record.getRecordNumber(),
                            fileName,
                            e.getMessage());

                    // Continue even though there was an error so that all errors
                    // logged at once.
                    continue;
                }

                // Add the newly created CSV object to the object list
                if (gtfsObject != null) gtfsObjects.add(gtfsObject);

                // Log info if it has been a while. Check only every 20,000
                // lines to see if the 10 seconds has gone by. If so, then log
                // number of lines. By only looking at timer every 20,000 lines
                // not slowing things down by for every line doing system call
                // for to get current time.
                final int LINES_TO_PROCESS_BEFORE_CHECKING_IF_SHOULD_LOG = 20000;
                final long SECONDS_ELSAPSED_UNTIL_SHOULD_LOG = 5;
                if (record.getRecordNumber() >= lineNumberWhenLogged + LINES_TO_PROCESS_BEFORE_CHECKING_IF_SHOULD_LOG) {
                    lineNumberWhenLogged = (int) record.getRecordNumber();
                    if (loggingTimer.elapsedMsec() > SECONDS_ELSAPSED_UNTIL_SHOULD_LOG * Time.MS_PER_SEC) {
                        logger.info("  Processed {} lines. Took {} msec...", lineNumberWhenLogged, timer.elapsedMsec());
                        loggingTimer = new IntervalTimer();
                    }
                }
            } // End of while iterating over records

            // Close up the file reader
            in.close();

            // Determine number of records for logging message
            long numberRecords = 0;
            if (record != null) numberRecords = record.getRecordNumber();

            logger.info(
                    "Finished parsing {} records from file {} . Took {} msec.",
                    numberRecords,
                    fileName,
                    timer.elapsedMsec());
        } catch (FileNotFoundException e) {
            if (required) logger.error("Required CSV file {} not found.", fileName);
            else logger.info("CSV file {} not found but OK because this file " + "not required.", fileName);
        } catch (IOException e) {
            logger.error("IOException occurred when reading in filename {}.", fileName, e);
        }
    }

    /**
     * The way one gets the list of CSV objects. Uses default size for creating ArrayList of 100.
     *
     * @return List of CSV objects. Can be empty but not null.
     */
    public List<T> get() {
        return get(100);
    }

    /**
     * The way one gets the list of CSV objects.
     *
     * @param initialSize Initial size of array that returns the objects. For when expect a really
     *     large array, such as for stop_times then can initialize to large value.
     * @return List of CSV objects. Can be empty but not null.
     */
    public List<T> get(int initialSize) {
        gtfsObjects = new ArrayList<>(initialSize);

        parse();

        return gtfsObjects;
    }
}
