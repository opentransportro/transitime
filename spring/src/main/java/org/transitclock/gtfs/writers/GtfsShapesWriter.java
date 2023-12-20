/* (C)2023 */
package org.transitclock.gtfs.writers;

import org.transitclock.gtfs.GtfsShape;
import org.transitclock.utils.csv.CsvWriterBase;

import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Writes out a GTFS shapes.txt file. Useful for when need to modify the file, such as for
 * processing the latitudes/longitudes so that data for China can be visualized correctly on a web
 * based map.
 *
 * @author SkiBu Smith
 */
public class GtfsShapesWriter extends CsvWriterBase {

    private static DecimalFormat sixDigitFormatter = new DecimalFormat("0.000000");

    /********************** Member Functions **************************/

    /**
     * Creates file writer and writes the header.
     *
     * @param fileName
     */
    public GtfsShapesWriter(String fileName) {
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
        writer.append("shape_id,shape_pt_lat,shape_pt_lon,shape_pt_sequence," + "shape_dist_traveled\n");
    }

    public void write(GtfsShape gtfsShape) {
        try {
            // Write out the GtfsShape
            append(gtfsShape.getShapeId()).append(',');
            append(sixDigitFormatter.format(gtfsShape.getLocation().getLat()));
            append(',');
            append(sixDigitFormatter.format(gtfsShape.getLocation().getLon()));
            append(',');
            append(gtfsShape.getShapePtSequence()).append(',');
            append(gtfsShape.getShapeDistTraveled()).append('\n');
        } catch (IOException e) {
            // Only expect to run this in batch mode so don't really
            // need to log an error using regular logging. Printing
            // stack trace should suffice.
            e.printStackTrace();
        }
    }
}
