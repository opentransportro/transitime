/* (C)2023 */
package org.transitclock;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.transitclock.config.ConfigFileReader;
import org.transitclock.config.data.AgencyConfig;
import org.transitclock.domain.hibernate.HibernateUtils;
import org.transitclock.gtfs.DbWriter;
import org.transitclock.gtfs.GtfsData;
import org.transitclock.gtfs.TitleFormatter;
import org.transitclock.gtfs.model.GtfsAgency;
import org.transitclock.gtfs.readers.GtfsAgencyReader;
import org.transitclock.utils.HttpGetGtfsFile;
import org.transitclock.utils.IntervalTimer;
import org.transitclock.utils.Zip;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Reads GTFS files, validates and cleans up the data, stores the data into Java objects, and then
 * stores those objects into the database.
 *
 * <p>There are a good number of options. Therefore there are addOption methods so don't have a
 * constructor with a large number of parameters.
 *
 * @author SkiBu Smith
 */
@Slf4j
public class GtfsFileProcessor {

    // Optional command line info used within this class
    private final String gtfsUrl;
    private String gtfsZipFileName;
    // Last modified time of GTFS zip file. Null if zip file not used.
    private Date zipFileLastModifiedTime;
    private final String unzipSubdirectory;
    private String gtfsDirectoryName;

    // Optional command line info used for GtfsData class
    private final String supplementDir;
    private final String regexReplaceListFileName;
    private final double pathOffsetDistance;
    private final double maxStopToPathDistance;
    private final double maxDistanceForEliminatingVertices;
    private final int defaultWaitTimeAtStopMsec;
    private final double maxSpeedKph;
    private final double maxTravelTimeSegmentLength;
    private final int configRev;
    private final boolean shouldStoreNewRevs;
    private final boolean shouldDeleteRevs;
    private final boolean trimPathBeforeFirstStopOfTrip;
    private final double maxDistanceBetweenStops;
    private final boolean disableSpecialLoopBackToBeginningCase;

    // Read in configuration files. This should be done statically before
    // the logback LoggerFactory.getLogger() is called so that logback can
    // also be configured using a transitime config file.
    static {
        ConfigFileReader.processConfig();
    }

    /**
     * Simple constructor. Stores the configurable parameters for this class. Declared private since
     * only used internally by createGtfsFileProcessor().
     */
    public GtfsFileProcessor(
            String gtfsUrl,
            String gtfsZipFileName,
            String unzipSubdirectory,
            String supplementDir,
            String regexReplaceListFileName,
            double pathOffsetDistance,
            double maxStopToPathDistance,
            double maxDistanceForEliminatingVertices,
            int defaultWaitTimeAtStopMsec,
            double maxSpeedKph,
            double maxTravelTimeSegmentLength,
            int configRev,
            boolean shouldStoreNewRevs,
            boolean shouldDeleteRevs,
            boolean trimPathBeforeFirstStopOfTrip,
            double maxDistanceBetweenStops,
            boolean disableSpecialLoopBackToBeginningCase) {
        // Read in config params if command line option specified
        this.gtfsUrl = gtfsUrl;
        this.gtfsZipFileName = gtfsZipFileName;
        this.unzipSubdirectory = unzipSubdirectory;
        this.supplementDir = supplementDir;
        this.regexReplaceListFileName = regexReplaceListFileName;
        this.pathOffsetDistance = pathOffsetDistance;
        this.maxStopToPathDistance = maxStopToPathDistance;
        this.maxDistanceForEliminatingVertices = maxDistanceForEliminatingVertices;
        this.defaultWaitTimeAtStopMsec = defaultWaitTimeAtStopMsec;
        this.maxSpeedKph = maxSpeedKph;
        this.maxTravelTimeSegmentLength = maxTravelTimeSegmentLength;
        this.configRev = configRev;
        this.shouldStoreNewRevs = shouldStoreNewRevs;
        this.shouldDeleteRevs = shouldDeleteRevs;
        this.trimPathBeforeFirstStopOfTrip = trimPathBeforeFirstStopOfTrip;
        this.maxDistanceBetweenStops = maxDistanceBetweenStops;
        this.disableSpecialLoopBackToBeginningCase = disableSpecialLoopBackToBeginningCase;
    }

    /**
     * Gets the GTFS files ready. Reads the zip file from the web if necessary. Then unpacks the zip
     * file if necessary. This method will make sure gtfsDirectoryName is set to where the unzipped
     * files can be found.
     *
     * <p>Doesn't need to do anything if GTFS files are already unzipped and available locally.
     */
    private void obtainGtfsFiles() throws IllegalArgumentException {
        if (gtfsUrl == null && gtfsZipFileName == null) {
            throw new IllegalArgumentException("For GtfsFileProcessor must specify the addOptionGtfsUrl(), "
                    + "or the addOptionGtfsZipFileName() " + "option.");
        }
        if (gtfsUrl != null && gtfsZipFileName != null) {
            throw new IllegalArgumentException("For GtfsFileProcessor both the addOptionGtfsUrl() and the "
                    + "addOptionGtfsZipFileName() options were specified but only "
                    + "allowed to specify one or the other");
        }

        if (gtfsUrl != null) {
            gtfsZipFileName = HttpGetGtfsFile.getFile(AgencyConfig.getAgencyId(), gtfsUrl, unzipSubdirectory);
        }

        if (gtfsZipFileName != null) {
            gtfsDirectoryName = Zip.unzip(gtfsZipFileName, unzipSubdirectory);
            zipFileLastModifiedTime = new Date(new File(gtfsZipFileName).lastModified());
        }
    }

    /**
     * Cleans up GTFS files. Specifically, if unzipped a GTFS zip file then removes the .txt files
     * from gtfsDirectoryName since they can be quite large and don't need them around anymore.
     */
    private void cleanupGtfsFiles() {
        // Only need to cleanup if unzipped a zip file
        if (gtfsZipFileName == null) {
            return;
        }

        File f = new File(gtfsDirectoryName);
        Arrays.stream(Objects.requireNonNull(f.list()))
                .forEach(fileName -> {
                    try {
                        if (fileName.endsWith(".txt")) {
                            Files.delete(Paths.get(gtfsDirectoryName, fileName));
                        }
                    } catch (Exception e) {
                        logger.error("Exception when cleaning up GTFS files", e);
                    }
                });

    }

    /**
     * Sets timezone for the application so that times and dates will be written correctly to the
     * database. This is especially important for calendar dates. Needs to be called before the db
     * is first accessed in order to have an effect with postgres (with mysql can do so afterwards,
     * which is strange). So needs to be done before GtfsData() object is constructed.
     *
     * <p>The timezone string is obtained from the agency.txt GTFS file.
     *
     * @param gtfsDirectoryName Where to find the GTFS files
     */
    private void setTimezone(String gtfsDirectoryName) {
        // Read in the agency.txt GTFS data from file
        GtfsAgencyReader agencyReader = new GtfsAgencyReader(gtfsDirectoryName);
        List<GtfsAgency> gtfsAgencies = agencyReader.get();
        if (gtfsAgencies.isEmpty()) {
            logger.error(
                    "Could not read in {}/agency.txt file, which is " + "needed for createDateFormatter()",
                    gtfsDirectoryName);
            System.exit(-1);
        }
        String timezoneName = gtfsAgencies.get(0).getAgencyTimezone();

        // Set system timezone so that dates and times will be written to db
        // properly
        TimeZone.setDefault(TimeZone.getTimeZone(timezoneName));
        logger.info("Set at beginning default timezone to {}", timezoneName);
    }

    /**
     * Once the GtfsFileProcessor is constructed and the options have been set then this function is
     * used to actually process the GTFS data and store it into the database.
     */
    public void process() throws IllegalArgumentException {
        // Gets the GTFS files from URL or from a zip file if need be.
        // This also sets gtfsDirectoryName member
        obtainGtfsFiles();

        // Set the timezone of the application so that times and dates will be
        // written correctly to the database. This is especially important for
        // calendar dates. This has to be done after obtainGtfsFiles() so that
        // gtfsDirectoryName member is set. Needs to be done before the db is
        // first accessed in order to have an effect with postgres (with mysql
        // can do so afterwards, which is strange). So needs to be done before
        // GtfsData() object is constructed.
        setTimezone(gtfsDirectoryName);

        // Create a title formatter
        TitleFormatter titleFormatter = new TitleFormatter(regexReplaceListFileName, true);
        var sessionFactory = HibernateUtils.getSessionFactory(AgencyConfig.getAgencyId());

        try (Session session = sessionFactory.openSession()) {
            // Process the GTFS data
            GtfsData gtfsData = new GtfsData(
                    session,
                    configRev,
                    zipFileLastModifiedTime,
                    shouldStoreNewRevs,
                    AgencyConfig.getAgencyId(),
                    gtfsDirectoryName,
                    supplementDir,
                    pathOffsetDistance,
                    maxStopToPathDistance,
                    maxDistanceForEliminatingVertices,
                    defaultWaitTimeAtStopMsec,
                    maxSpeedKph,
                    maxTravelTimeSegmentLength,
                    trimPathBeforeFirstStopOfTrip,
                    titleFormatter,
                    maxDistanceBetweenStops,
                    disableSpecialLoopBackToBeginningCase);

            // For logging how long things take
            IntervalTimer timer = new IntervalTimer();

            gtfsData.processData();
            new DbWriter(gtfsData)
                    .write(session, gtfsData.getRevs().getConfigRev(), shouldDeleteRevs);
            logger.info("Finished processing GTFS data from {} . Took {} msec.", gtfsDirectoryName, timer.elapsedMsec());
        } catch (HibernateException e) {
            logger.error("Exception when writing data to db", e);
            throw e;
        }

        // Log possibly useful info
        titleFormatter.logRegexesThatDidNotMakeDifference();

        // Do any necessary cleanup
        cleanupGtfsFiles();
    }





    /**
     * Uses the command line args to fully configure a GtfsFileProcessor object. This is where
     * defaults go for command line options.
     *
     * @param params
     * @return Fully configured GtfsFileProcessor object
     */
    public static GtfsFileProcessor createGtfsFileProcessor(CommandLineParameters params) {
        File gtfsDirectory = params.getRelativeDirectory("gtfs");

        return new GtfsFileProcessor(
                params.gtfsUrl,
                params.gtfsZip,
                gtfsDirectory.getAbsolutePath(),
                null,
                null,
                params.gtfsPathOffsetDistance,
                params.gtfsMaxStopToPathDistance,
                params.gtfsMaxDistanceForEliminatingVertices,
                params.gtfsDefaultWaitTimeAtStop,
                params.gtfsMaxSpeed,
                params.gtfsMaxTravelTimeSegmentLength,
                -1,
                true, //shouldStoreNewRevs,
                false, //shouldDeleteRevs,
                params.gtfsTrimPathBeforeFirstStopOfTrip,
                params.gtfsMaxDistanceBetweenStops,
                params.gtfsDisableSpecialLoopBackToBeginningCase);
    }
}
