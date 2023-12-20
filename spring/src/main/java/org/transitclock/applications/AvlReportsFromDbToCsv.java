/* (C)2023 */
package org.transitclock.applications;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import org.transitclock.avl.AvlCsvWriter;
import org.transitclock.core.domain.AvlReport;
import org.transitclock.utils.Time;
import org.transitclock.utils.TimeZoneSetter;

/**
 * For reading AvlReport data from database and writing it to a CSV file. Useful for then modifying
 * by hand and using as input to the core predictor system for debugging special situations.
 *
 * @author Michael
 */
public class AvlReportsFromDbToCsv {

    /**
     * Reads AVL reports from database
     *
     * @param args
     * @return List of AvlReport objects
     */
    private static List<AvlReport> getAvlReports(String[] args) {
        Date beginTime = null;
        Date endTime = null;
        try {
            String beginTimeStr = args[1];
            beginTime = Time.parse(beginTimeStr);
            String endTimeStr = args[2];
            endTime = Time.parse(endTimeStr);
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        String vehicleId = args.length > 3 ? args[3] : null;

        String sqlClause = args.length > 4 ? args[4] : null;

        List<AvlReport> avlReports = AvlReport.getAvlReportsFromDb(beginTime, endTime, vehicleId, sqlClause);

        return avlReports;
    }

    /**
     * Writes AVL reports to CSV file
     *
     * @param args
     * @param avlReports
     */
    private static void writeAvlReports(String[] args, List<AvlReport> avlReports) {
        String fileName = args[0];
        String timezoneStr = null; // Use local timezone
        AvlCsvWriter writer = new AvlCsvWriter(fileName, timezoneStr);

        for (AvlReport avlReport : avlReports) {
            writer.write(avlReport);
        }

        writer.close();
    }

    /**
     * Reads AVL data from database and writes it into a CSV file
     *
     * @param args args[1] is begin time in format MM-dd-yyyy HH:mm:ss args[2] is end time in format
     *     MM-dd-yyyy HH:mm:ss args[3] is optional vehicleId args[4] is optional SQL clause
     */
    public static void main(String[] args) {
        // Set timezone before params are processed or db connected to
        TimeZoneSetter.setTimezone();

        List<AvlReport> avlReports = getAvlReports(args);
        writeAvlReports(args, avlReports);
    }
}
