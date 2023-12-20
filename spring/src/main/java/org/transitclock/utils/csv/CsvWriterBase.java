/* (C)2023 */
package org.transitclock.utils.csv;

import org.transitclock.utils.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * A base class for writing out a CSV file. A subclass needs
 *
 * @author SkiBu Smith
 */
public abstract class CsvWriterBase {

    protected Writer writer;

    /**
     * Creates file writer and writes the header.
     *
     * <p>To write a file one uses a subclass that specifies how to write the header and a line to
     * the file for each GTFS object. One simply constructs the subclass (which creates the file and
     * writes the header), then calls the subclass write(GtfsObject gtfsObject) method for each
     * object, and the close() to finish things up.
     *
     * @param fileName
     * @param append Set to true if should append data to CSV file if it already exists. If false
     *     then will write new file along with the header.
     */
    public CsvWriterBase(String fileName, boolean append) {
        try {
            // Create the directory if necessary.
            // First, determine directory name by finding the last slash.
            int lastSlashPos = fileName.lastIndexOf('/');
            // If there was no last slash then look for MS-DOS style back slashes
            if (lastSlashPos < 0) lastSlashPos = fileName.lastIndexOf('\\');
            // Actually create the directory if necessary
            if (lastSlashPos > 0) {
                String dirName = fileName.substring(0, lastSlashPos);
                File dir = new File(dirName);
                dir.mkdirs();
            }

            // Determine if file exists
            boolean fileAlreadyExists = (new File(fileName)).exists();

            // Create the writer. Need to use UTF-8 since sometimes will be
            // writing Chinese or other characters for route names and such.
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName, append), StandardCharsets.UTF_8));

            // Write the header if it is a new file or not appending
            if (!fileAlreadyExists || !append) writeHeader();
        } catch (IOException e) {
            // Only expect to run this in batch mode so don't really
            // need to log an error using regular logging. Printing
            // stack trace should suffice.
            e.printStackTrace();
        }
    }

    /** To be overridden. Automatically called by constructor. */
    protected abstract void writeHeader() throws IOException;

    /** Closes the file. */
    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            // Only expect to run this in batch mode so don't really
            // need to log an error using regular logging. Printing
            // stack trace should suffice.
            e.printStackTrace();
        }
    }

    /**
     * Writes a single Integer to the file
     *
     * @param i
     * @return
     * @throws IOException
     */
    protected Writer append(Integer i) throws IOException {
        if (i != null) writer.append(i.toString());
        return writer;
    }

    /**
     * Writes a single Double to the file
     *
     * @param d
     * @return
     * @throws IOException
     */
    protected Writer append(Double d) throws IOException {
        if (d != null) writer.append(StringUtils.twoDigitFormat(d));
        return writer;
    }

    /**
     * For writing strings and other objects. If the object is null then nothing is written out.
     *
     * @param o
     * @return
     * @throws IOException
     */
    protected Writer append(Object o) throws IOException {
        if (o != null) writer.append(o.toString());
        return writer;
    }
}
