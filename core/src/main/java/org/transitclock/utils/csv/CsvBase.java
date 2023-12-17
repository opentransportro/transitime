/* (C)2023 */
package org.transitclock.utils.csv;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVRecord;

/**
 * Base class for CSV struct classes. The main CSV struct classes must inherit from this class.
 * Keeps track of line numbers. Also handles errors when necessary data is missing in CSV file.
 *
 * @author SkiBu Smith
 */
@Getter
@Slf4j
public class CsvBase {

    protected final int lineNumber;
    private final String fileName;

    private final boolean supplementalFileSoSomeRequiredItemsCanBeMissing;


    /**
     * Main constructor for when creating CSV object from a CSV file.
     *
     * @param record
     * @param supplementalFile
     * @param fileName for logging errors
     */
    protected CsvBase(CSVRecord record, boolean supplementalFile, String fileName) {
        this.lineNumber = (int) record.getRecordNumber();
        this.supplementalFileSoSomeRequiredItemsCanBeMissing = supplementalFile;
        this.fileName = fileName;
    }

    /**
     * For when creating a new object by combining in supplemental object with a regular object or
     * when generating additional information that is to be put into CSV file.
     *
     * @param original
     */
    protected CsvBase(CsvBase original) {
        this.lineNumber = original.lineNumber;
        this.supplementalFileSoSomeRequiredItemsCanBeMissing = original.supplementalFileSoSomeRequiredItemsCanBeMissing;
        this.fileName = original.getFileName();
    }

    /** Only for when creating a supplemental file. Sets the members to special values. */
    protected CsvBase() {
        this.lineNumber = -1;
        this.supplementalFileSoSomeRequiredItemsCanBeMissing = true;
        this.fileName = null;
    }

    /**
     * For reading a value that is required in CSV and is required even if reading in a supplemental
     * file. If data is missing the error is logged.
     *
     * @param record The data for the row in the CSV file
     * @param name The name of the column in the CSV file
     * @return The value, or null if it was not defined
     */
    protected String getRequiredValue(CSVRecord record, String name) {
        boolean required = true;
        return getValue(record, name, required);
    }

    /**
     * For reading a value that is required in CSV, but is not needed if reading in a supplemental
     * file that will be combined with a regular CSV file. If data is missing and not a supplemental
     * file then error is logged.
     *
     * @param record The data for the row in the CSV file
     * @param name The name of the column in the CSV file
     * @return The value, or null if it was not defined
     */
    protected String getRequiredUnlessSupplementalValue(CSVRecord record, String name) {
        boolean required = !supplementalFileSoSomeRequiredItemsCanBeMissing;
        return getValue(record, name, required);
    }

    /**
     * For reading in a value that is not required in CSV.
     *
     * @param record The data for the row in the CSV file
     * @param name The name of the column in the CSV file
     * @return The value, or null if it was not defined
     */
    protected String getOptionalValue(CSVRecord record, String name) {
        boolean required = false;
        return getValue(record, name, required);
    }

    /**
     * For boolean extensions to CSV, such as whether a stop or route is hidden.
     *
     * @param record The data for the row in the CSV file
     * @param name The name of the column in the CSV file
     * @return true or false if column set in CSV file. Otherwise null.
     */
    protected Boolean getOptionalBooleanValue(CSVRecord record, String name) {
        String booleanStr = getOptionalValue(record, name);
        if (booleanStr != null) {
            return booleanStr.equals("1") || booleanStr.equals("t") || booleanStr.equals("true");
        }

        return null;
    }

    /**
     * For Integer extensions to CSV, such as for route order and break time.
     *
     * @param record The data for the row in the CSV file
     * @param name The name of the column in the CSV file
     * @return The optional Integer value or null if it is not set or could not be parsed.
     */
    protected Integer getOptionalIntegerValue(CSVRecord record, String name) {
        String integerStr = getOptionalValue(record, name);

        if (integerStr == null) return null;

        try {
            return Integer.parseInt(integerStr);
        } catch (NumberFormatException e) {
            logger.error("NumberFormatException when parsing integer \"{}\"", integerStr);
            return null;
        }
    }

    /**
     * For Double extensions to CSV, such as for route max_distance.
     *
     * @param record The data for the row in the CSV file
     * @param name The name of the column in the CSV file
     * @return The optional Double value or null if it is not set or could not be parsed.
     */
    protected Double getOptionalDoubleValue(CSVRecord record, String name) {
        String doubleStr = getOptionalValue(record, name);

        if (doubleStr == null) return null;

        try {
            return Double.parseDouble(doubleStr);
        } catch (NumberFormatException e) {
            logger.error("NumberFormatException when parsing double \"{}\"", doubleStr);
            return null;
        }
    }

    /**
     * For reading values from a CSVRecord. If the column was not defined then CSVRecord.get()
     * throws an exception. Therefore for optional CSV columns better to use this function so don't
     * get exception. This way can continue processing and all errors for the data will be logged.
     * Better than just logging first error and then quitting.
     *
     * <p>Also, if the value is empty string then it is converted to null for consistency. Also
     * trims the resulting string since some agencies leave in spaces.
     *
     * @param record The data for the row in the CSV file
     * @param name The name of the column in the CSV file
     * @param required Whether this value is required. If required and the value is not set then an
     *     error is logged and null is returned.
     * @return The value, or null if it was not defined
     */
    private String getValue(CSVRecord record, String name, boolean required) {
        // If the column is not defined in the file then return null.
        // After all, the item is optional so it is fine for it to
        // not be in the file.
        if (!record.isSet(name)) {
            if (required) {
                logger.error("Column {} not defined in file \"{}\" yet it is required", name, getFileName());
            }
            return null;
        }

        // Get the value. First trim whitespace so that
        // value will be consistent. Sometimes agencies will mistakenly have
        // some whitespace in the columns.
        String value = record.get(name).trim();

        // Return the value. But if the value is empty string
        // convert to null for consistency.
        if (value.isEmpty()) {
            if (required) {
                logger.error(
                        "For file \"{}\" line number {} for column {} value was not set " + "yet it is required",
                        getFileName(),
                        lineNumber,
                        name);
            }
            return null;
        } else {
            // Successfully got value so return intern() version of it. Using
            // intern() because many strings are duplicates such as headsign
            // or shape info. By sharing the strings we can save a huge amount
            // of memory.
            return value.intern();
        }
    }
}
