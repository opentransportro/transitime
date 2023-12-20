/* (C)2023 */
package org.transitclock.gtfs.writers;

import org.transitclock.gtfs.GtfsStop;
import org.transitclock.utils.csv.CsvWriterBase;

import java.io.IOException;
import java.text.DecimalFormat;

/**
 * For writing out the stops.txt GTFS file. Useful for when need to modify the file, such as for
 * processing the latitudes/longitudes so that data for China can be visualized correctly on a web
 * based map.
 *
 * @author SkiBu Smith
 */
public class GtfsStopsWriter extends CsvWriterBase {

    private static DecimalFormat sixDigitFormatter = new DecimalFormat("0.000000");

    /********************** Member Functions **************************/

    /**
     * Creates file writer and writes the header.
     *
     * @param fileName
     */
    public GtfsStopsWriter(String fileName) {
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
        writer.append("stop_id,stop_code,stop_name,stop_desc,stop_lat,"
                + "stop_lon,zone_id,stop_url,location_type,parent_station,"
                + "stop_timezone,wheelchair_boarding\n");
    }

    /**
     * Writes a GtfsStop to the file
     *
     * @param gtfsStop
     */
    public void write(GtfsStop gtfsStop) {
        try {
            // Write out the gtfsStop
            append(gtfsStop.getStopId()).append(',');
            append(gtfsStop.getStopCode()).append(',');
            append(gtfsStop.getStopName()).append(',');
            append(gtfsStop.getStopDesc()).append(',');
            append(sixDigitFormatter.format(gtfsStop.getStopLat())).append(',');
            append(sixDigitFormatter.format(gtfsStop.getStopLon())).append(',');
            append(gtfsStop.getZoneId()).append(',');
            append(gtfsStop.getStopUrl()).append(',');
            append(gtfsStop.getLocationType()).append(',');
            append(gtfsStop.getParentStation()).append(',');
            append(gtfsStop.getStopTimezone()).append(',');
            append(gtfsStop.getWheelchairBoarding()).append('\n');
        } catch (IOException e) {
            // Only expect to run this in batch mode so don't really
            // need to log an error using regular logging. Printing
            // stack trace should suffice.
            e.printStackTrace();
        }
    }
}
