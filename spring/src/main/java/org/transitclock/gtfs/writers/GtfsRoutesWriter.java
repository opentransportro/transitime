/* (C)2023 */
package org.transitclock.gtfs.writers;

import org.transitclock.gtfs.GtfsRoute;
import org.transitclock.utils.csv.CsvWriterBase;

import java.io.IOException;

/**
 * For writing out the routes.txt GTFS file. Useful for when need to modify the file.
 *
 * @author SkiBu Smith
 */
public class GtfsRoutesWriter extends CsvWriterBase {

    /********************** Member Functions **************************/

    /**
     * Creates file writer and writes the header.
     *
     * @param fileName
     */
    public GtfsRoutesWriter(String fileName) {
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
        writer.append(
                "route_id,agency_id,route_short_name,route_long_name," + "route_type,route_color,route_text_color\n");
    }

    /**
     * Writes a GtfsStop to the file
     *
     * @param gtfsStop
     */
    public void write(GtfsRoute gtfsRoute) {
        try {
            // Write out the gtfsStop
            append(gtfsRoute.getRouteId()).append(',');

            append(gtfsRoute.getAgencyId()).append(',');
            append(gtfsRoute.getRouteShortName()).append(',');
            append(gtfsRoute.getRouteLongName()).append(',');
            append(gtfsRoute.getRouteType()).append(',');
            append(gtfsRoute.getRouteColor()).append(',');
            append(gtfsRoute.getRouteTextColor()).append('\n');
        } catch (IOException e) {
            // Only expect to run this in batch mode so don't really
            // need to log an error using regular logging. Printing
            // stack trace should suffice.
            e.printStackTrace();
        }
    }
}
