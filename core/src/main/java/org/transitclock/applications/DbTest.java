/* (C)2023 */
package org.transitclock.applications;

import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.configData.DbSetupConfig;
import org.transitclock.db.structs.AvlReport;

/**
 * @author SkiBu Smith
 */
public class DbTest {

    private static final Logger logger = LoggerFactory.getLogger(DbTest.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            List<AvlReport> avlReports = AvlReport.getAvlReportsFromDb(
                    new Date(), // beginTime
                    new Date(), // endTime
                    null, // vehicleId
                    null); // SQL clause
            if (avlReports != null) logger.info("Successfully connected to the database!");
        } catch (Exception e) {
            logger.error(
                    "Error occurred when trying to access database for " + "dbName={}. {}",
                    DbSetupConfig.getDbName(),
                    e.getMessage(),
                    e);
        }
    }

    /********************** Member Functions **************************/
}
