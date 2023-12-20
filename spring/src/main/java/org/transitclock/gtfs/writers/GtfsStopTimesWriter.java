/* (C)2023 */
package org.transitclock.gtfs.writers;

import org.transitclock.utils.StringUtils;
import org.transitclock.utils.Time;
import org.transitclock.gtfs.GtfsStopTime;
import org.transitclock.utils.csv.CsvWriterBase;

import java.io.IOException;
import java.io.Writer;

/**
 * For writing a GTFS stop_times.txt file. This class is useful when updating the GTFS stop_times
 * based on historic AVL data.
 *
 * <p>Since this is pretty simple not using a general CSV class to do the writing.
 *
 * @author SkiBu Smith
 */
public class GtfsStopTimesWriter extends CsvWriterBase {

    /********************** Member Functions **************************/

    /**
     * Creates file writer and writes the header.
     *
     * @param fileName
     */
    public GtfsStopTimesWriter(String fileName) {
        super(fileName, false);
    }

    /**
     * Writes the header to the file.
     *
     * @throws IOException
     */
    @Override
    protected void writeHeader() throws IOException {
        // Write the header
        writer.append("trip_id,arrival_time,departure_time,stop_id,"
                + "stop_sequence,stop_headsign,pickup_type,drop_off_type,"
                + "shape_dist_traveled\n");
    }

    /**
     * For handling values which can be null.
     *
     * @param o the object to be written
     * @return the Writer
     * @throws IOException
     */
    protected Writer append(Object o) throws IOException {
        if (o != null) writer.append(o.toString());
        return writer;
    }

    /**
     * Writes a single Double to the file. If the Double value is not null then will use at least 6
     * characters in order to try to line up the results in the stop_times.txt file.
     *
     * @param d
     * @return
     * @throws IOException
     */
    @Override
    protected Writer append(Double d) throws IOException {
        if (d != null) {
            String paddedStr = StringUtils.padWithBlanks(StringUtils.twoDigitFormat(d), 6);
            writer.append(paddedStr);
        }
        return writer;
    }

    /**
     * Writing time values are is special case because need to convert the timeOfDay in seconds to a
     * time of day string such as 11:53:01. If null is passed in then will write out 8 blank
     * characters so that the resulting data in the stop_times.txt file will line up.
     *
     * @param timeOfDay the object to be written
     * @return the Writer
     * @throws IOException
     */
    protected Writer appendTime(Integer timeOfDay) throws IOException {
        if (timeOfDay == null) writer.append("        ");
        else writer.append(Time.timeOfDayStr(timeOfDay));
        return writer;
    }

    /**
     * Writes a GtfsStopTime to the file
     *
     * @param stopTime
     */
    public void write(GtfsStopTime stopTime) {
        try {
            // Write the data
            append(stopTime.getTripId()).append(',');
            appendTime(stopTime.getArrivalTimeSecs()).append(',');
            appendTime(stopTime.getDepartureTimeSecs()).append(',');
            String paddedStopId = StringUtils.padWithBlanks(stopTime.getStopId(), 5);
            append(paddedStopId).append(',');
            String paddedStopSequence = StringUtils.padWithBlanks(Integer.toString(stopTime.getStopSequence()), 2);
            append(paddedStopSequence).append(',');
            append(stopTime.getStopHeadsign()).append(',');
            append(stopTime.getPickupType()).append(',');
            append(stopTime.getDropOffType()).append(',');
            append(stopTime.getShapeDistTraveled()).append('\n');
        } catch (IOException e) {
            // Only expect to run this in batch mode so don't really
            // need to log an error using regular logging. Printing
            // stack trace should suffice.
            e.printStackTrace();
        }
    }
}
