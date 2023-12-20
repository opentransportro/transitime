/* (C)2023 */
package org.transitclock.gtfs.writers;

import org.transitclock.utils.StringUtils;
import org.transitclock.gtfs.GtfsExtendedStopTime;

import java.io.IOException;

/**
 * For writing out extended GTFS stop_times.txt file that contains additional info beyond the usual
 * stop_times.txt file such as statistical info determined when creating the schedule. This class is
 * useful when updating the GTFS stop_times based on historic AVL data.
 *
 * <p>Since this is pretty simple not using a general CSV class to do the writing.
 *
 * @author SkiBu Smith
 */
public class GtfsExtendedStopTimesWriter extends GtfsStopTimesWriter {

    /********************** Member Functions **************************/

    /**
     * Creates file writer and writes the header.
     *
     * @param fileName
     */
    public GtfsExtendedStopTimesWriter(String fileName) {
        super(fileName);
    }

    /**
     * Writes the header to the file.
     *
     * @throws IOException
     */
    protected void writeHeader() throws IOException {
        writer.append("trip_id,arrival_time,departure_time,stop_id,"
                + "stop_sequence,stop_headsign,pickup_type,drop_off_type,"
                + "shape_dist_traveled,"
                + " original_arrival_time,min_arrival_time,max_arrival_time,"
                + "arrivals_std_dev,number_arrivals,"
                + " original_departure_time,min_departure_time,max_departure_time,"
                + "departures_std_dev,number_departures"
                + "\n");
    }

    /**
     * Writes a GtfsExtendedStopTimes to the file.
     *
     * @param fileName
     * @param stopTimes
     */
    public void write(GtfsExtendedStopTime stopTime) {
        try {
            // Write out the normal GTFS stop times data
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
            append(stopTime.getShapeDistTraveled()).append(',');

            // Write out the extended stop times data.
            // Add a space as a visual separator before outputting arrival data
            writer.append(' ');
            appendTime(stopTime.getArrivalOrigTimeSecs()).append(',');
            appendTime(stopTime.getArrivalMinTimeSecs()).append(',');
            appendTime(stopTime.getArrivalMaxTimeSecs()).append(',');
            append(stopTime.getArrivalStdDev()).append(',');
            String paddedNumArrDatapoints =
                    StringUtils.padWithBlanks(Integer.toString(stopTime.getArrivalNumberDatapoints()), 2);
            append(paddedNumArrDatapoints).append(',');

            // Add a space as a visual separator before outputting departure data
            writer.append(' ');
            appendTime(stopTime.getDepartureOrigTimeSecs()).append(',');
            appendTime(stopTime.getDepartureMinTimeSecs()).append(',');
            appendTime(stopTime.getDepartureMaxTimeSecs()).append(',');
            append(stopTime.getDepartureStdDev()).append(',');
            String paddedNumDepDatapoints =
                    StringUtils.padWithBlanks(Integer.toString(stopTime.getDepartureNumberDatapoints()), 2);
            append(paddedNumDepDatapoints).append('\n');
        } catch (IOException e) {
            // Only expect to run this in batch mode so don't really
            // need to log an error using regular logging. Printing
            // stack trace should suffice.
            e.printStackTrace();
        }
    }
}
