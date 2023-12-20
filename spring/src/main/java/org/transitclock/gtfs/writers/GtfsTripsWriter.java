/* (C)2023 */
package org.transitclock.gtfs.writers;

import org.transitclock.gtfs.GtfsTrip;
import org.transitclock.utils.csv.CsvWriterBase;

import java.io.IOException;

/**
 * For writing out the trips.txt GTFS file. Useful for when need to modify the file, such as for
 * programmatically adding block info.
 *
 * @author SkiBu Smith
 */
public class GtfsTripsWriter extends CsvWriterBase {
    public GtfsTripsWriter(String fileName) {
        super(fileName, false);
    }

    @Override
    protected void writeHeader() throws IOException {
        // Write the header
        writer.append("route_id,service_id,trip_id,trip_headsign,"
                + "trip_short_name,direction_id,block_id,shape_id,"
                + "wheelchair_accessible,bikes_allowed\n");
    }

    public void write(GtfsTrip gtfsTrip) {
        try {
            // Write out the gtfsStop
            append(gtfsTrip.getRouteId()).append(',');
            append(gtfsTrip.getServiceId()).append(',');
            append(gtfsTrip.getTripId()).append(',');
            append(gtfsTrip.getTripHeadsign()).append(',');
            append(gtfsTrip.getTripShortName()).append(',');
            append(gtfsTrip.getDirectionId()).append(',');
            append(gtfsTrip.getBlockId()).append(',');
            append(gtfsTrip.getShapeId()).append(',');
            append(gtfsTrip.getWheelchairAccessible()).append(',');
            append(gtfsTrip.getBikesAllowed()).append('\n');
        } catch (IOException e) {
            // Only expect to run this in batch mode so don't really
            // need to log an error using regular logging. Printing
            // stack trace should suffice.
        }
    }
}
