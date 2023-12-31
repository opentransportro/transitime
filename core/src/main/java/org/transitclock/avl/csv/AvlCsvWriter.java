/* (C)2023 */
package org.transitclock.avl.csv;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.AvlReport.AssignmentType;
import org.transitclock.utils.ChinaGpsOffset;
import org.transitclock.utils.ChinaGpsOffset.LatLon;
import org.transitclock.utils.Geo;
import org.transitclock.utils.Time;
import org.transitclock.utils.csv.CsvWriterBase;

/**
 * For writing a CSV file containing AVL reports.
 *
 * @author SkiBu Smith
 */
@Slf4j
public class AvlCsvWriter extends CsvWriterBase {

    // Needed so can output times in proper timezone
    private final Time timeUsingTimeZone;

    /**
     * Simple constructor.
     *
     * @param fileName
     * @param timezoneStr For outputting time in proper timezone. If null then will output time in
     *     local time.
     */
    public AvlCsvWriter(String fileName, String timezoneStr) {
        super(fileName, false);

        timeUsingTimeZone = new Time(timezoneStr);
    }

    /* (non-Javadoc)
     * @see org.transitclock.utils.csv.CsvWriterBase#writeHeader()
     */
    @Override
    protected void writeHeader() throws IOException {
        // Write the header
        writer.append("vehicleId,time,justTime,latitude,longitude,speed,heading," + "assignmentId,assignmentType\n");
    }

    /**
     * Appends an AvlReport to the CSV file.
     *
     * @param avlReport The AvlReport to be appended to the CSV file
     * @param transformForChinaMap If true then will convert lat/lon so that can be displayed on map
     *     of China.
     */
    public void write(AvlReport avlReport, boolean transformForChinaMap) {
        try {
            // Write out the GtfsShape
            append(avlReport.getVehicleId());
            append(',');

            append(timeUsingTimeZone.dateTimeStrMsecForTimezone(avlReport.getTime()));
            append(',');

            append(timeUsingTimeZone.timeStrForTimezone(avlReport.getTime()));
            append(',');

            // Determine lat/lon. Offset for use in map of China if necessary.
            double lat = avlReport.getLat();
            double lon = avlReport.getLon();
            if (transformForChinaMap) {
                LatLon offsetLatLon = ChinaGpsOffset.transform(lat, lon);
                lat = offsetLatLon.getLat();
                lon = offsetLatLon.getLon();
            }
            append(Geo.format(lat));
            append(',');

            append(Geo.format(lon));
            append(',');

            if (!Float.isNaN(avlReport.getSpeed())) append(Geo.oneDigitFormat(avlReport.getSpeed()));
            append(',');

            if (!Float.isNaN(avlReport.getHeading())) append(Geo.oneDigitFormat(avlReport.getHeading()));
            append(',');

            if (avlReport.getAssignmentId() != null) append(avlReport.getAssignmentId());
            append(',');

            // Add the assignment type using the name of the enumeration
            AssignmentType assignmentType = avlReport.getAssignmentType();
            if (assignmentType != null) {
                append(assignmentType.name());
            }

            // Wrap up the record
            append('\n');
        } catch (IOException e) {
            logger.error("Error writing {}.", avlReport, e);
        }
    }

    /**
     * Appends an AvlReport to the CSV file.
     *
     * @param avlReport The AvlReport to be appended to the CSV file
     */
    public void write(AvlReport avlReport) {
        write(avlReport, false);
    }
}
