/* (C)2023 */
package org.transitclock.applications;

import java.io.PrintWriter;
import java.util.*;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.transitclock.Module;
import org.transitclock.config.ConfigFileReader;
import org.transitclock.config.StringConfigValue;
import org.transitclock.configData.AgencyConfig;
import org.transitclock.configData.CoreConfig;
import org.transitclock.core.ServiceUtils;
import org.transitclock.core.TimeoutHandlerModule;
import org.transitclock.core.dataCache.PredictionDataCache;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheFactory;
import org.transitclock.core.dataCache.TripDataHistoryCacheFactory;
import org.transitclock.core.dataCache.VehicleDataCache;
import org.transitclock.core.dataCache.ehcache.CacheManagerFactory;
import org.transitclock.core.dataCache.frequency.FrequencyBasedHistoricalAverageCache;
import org.transitclock.core.dataCache.scheduled.ScheduleBasedHistoricalAverageCache;
import org.transitclock.db.hibernate.DataDbLogger;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.ActiveRevisions;
import org.transitclock.db.structs.Agency;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.ipc.servers.CacheQueryServer;
import org.transitclock.ipc.servers.CommandsServer;
import org.transitclock.ipc.servers.ConfigServer;
import org.transitclock.ipc.servers.HoldingTimeServer;
import org.transitclock.ipc.servers.PredictionAnalysisServer;
import org.transitclock.ipc.servers.PredictionsServer;
import org.transitclock.ipc.servers.ServerStatusServer;
import org.transitclock.ipc.servers.VehiclesServer;
import org.transitclock.monitoring.PidFile;
import org.transitclock.utils.SettableSystemTime;
import org.transitclock.utils.SystemCurrentTime;
import org.transitclock.utils.SystemTime;
import org.transitclock.utils.Time;

/**
 * The main class for running a Transitime Core real-time data processing system. Handles command
 * line arguments and then initiates AVL feed.
 *
 * @author SkiBu Smith
 */
@Slf4j
public class Core {
    private static Core SINGLETON;

    // Contains the configuration data read from database
    private final DbConfig configData;

    // For logging data such as AVL reports and arrival times to database
    private final DataDbLogger dataDbLogger;

    @Getter
    private final TimeoutHandlerModule timeoutHandlerModule;

    private final ServiceUtils service;

    /**
     *  For when want to use methods in Time. This is important when need methods that access a
     *  Calendar a lot. By putting the Calendar in Time it can be shared.
     */
    @Getter
    private final Time time;

    // So that can access the current time, even when in playback mode
    private SystemTime systemTime = new SystemCurrentTime();

    // Set by command line option. Specifies config rev to use if set
    private static String configRevStr = null;

    // Read in configuration files. This should be done statically before
    // the logback LoggerFactory.getLogger() is called so that logback can
    // also be configured using a transitime config file. The files are
    // specified using the java system property -Dtransitclock.configFiles .
    static {
        ConfigFileReader.processConfig();
    }

    private static final StringConfigValue cacheReloadStartTimeStr = new StringConfigValue(
            "transitclock.core.cacheReloadStartTimeStr",
            "",
            "Date and time of when to start reading arrivaldepartures to inform caches.");

    private static final StringConfigValue cacheReloadEndTimeStr = new StringConfigValue(
            "transitclock.core.cacheReloadEndTimeStr",
            "",
            "Date and time of when to end reading arrivaldepartures to inform caches.");

    /**
     * Construct the Core object and read in the config data. This is private so that the
     * createCore() factory method must be used.
     *
     * @param agencyId
     */
     private Core(String agencyId) {
        // Determine configuration rev to use. If one specified on command
        // line, use it. If not, then use revision stored in db.
        int configRev;
        if (configRevStr != null) {
            // Use config rev from command line
            configRev = Integer.parseInt(configRevStr);
        } else {
            // Read in config rev from ActiveRevisions table in db
            ActiveRevisions activeRevisions = ActiveRevisions.get(agencyId);

            // If config rev not set properly then simply log error.
            // Originally would also exit() but found that want system to
            // work even without GTFS configuration so that can test AVL feed.
            if (activeRevisions == null || !activeRevisions.isValid()) {
                logger.error("ActiveRevisions in database is not valid. The configuration revs must be set to proper values. {}", activeRevisions);
            }
            configRev = activeRevisions.getConfigRev();
        }

        // Set the timezone so that when dates are read from db or are logged
        // the time will be correct. Therefore this needs to be done right at
        // the start of the application, before db is read.
        TimeZone timeZone = Agency.getTimeZoneFromDb(agencyId);
        TimeZone.setDefault(timeZone);

        // Clears out the session factory so that a new one will be created for
        // future db access. This way new db connections are made. This is
        // useful for dealing with timezones and postgres. For that situation
        // want to be able to read in timezone from db so can set default
        // timezone. Problem with postgres is that once a factory is used to
        // generate sessions the database will continue to use the default
        // timezone that was configured at that time. This means that future
        // calls to the db will use the wrong timezone! Through this function
        // one can read in timezone from database, set the default timezone,
        // clear the factory so that future db connections will use the newly
        // configured timezone, and then successfully process dates.
        HibernateUtils.clearSessionFactory();

        // Read in all GTFS based config data from the database
        configData = new DbConfig(agencyId);
        configData.read(configRev);

        // Create the DataDBLogger so that generated data can be stored
        // to database via a robust queue. But don't actually log data
        // if in playback mode since then would be writing data again
        // that was first written when predictor was run in real time.
        // Note: DataDbLogger needs to be started after the timezone is set.
        // Otherwise when running for a different timezone than what the
        // computer is setup for then can log data using the wrong time!
        // This is strange since setting TimeZone.setDefault() is supposed
        // to work across all threads it appears that sometimes it wouldn't
        // work if Db logger started first.
        dataDbLogger = DataDbLogger.getDataDbLogger(agencyId, CoreConfig.storeDataInDatabase(), CoreConfig.pauseIfDbQueueFilling());

        timeoutHandlerModule = new TimeoutHandlerModule(AgencyConfig.getAgencyId());
        timeoutHandlerModule.start();

        service = new ServiceUtils(configData);
        time = new Time(configData);
    }

    /**
     * Creates the Core object for the application. There can only be one Core object per
     * application. Uses CoreConfig.getAgencyId() to determine the agencyId. This means it typically
     * uses the agency ID specified by the Java property -Dtransitclock.core.agencyId .
     *
     * <p>Usually doesn't need to be called directly because can simply use Core.getInstance().
     *
     * <p>Synchronized to ensure that don't create more than a single Core.
     *
     * @return The Core singleton, or null if could not create it
     */
    public static synchronized void createCore() {
        String agencyId = AgencyConfig.getAgencyId();

        // If agencyId not set then can't create a Core. This can happen
        // when doing testing.
        if (agencyId == null) {
            logger.error("No agencyId specified for when creating Core.");
            return;
        }

        // Make sure only can have a single Core object
        if (SINGLETON != null) {
            logger.error("Core singleton already created. Cannot create another one.");
            return;
        }

        SINGLETON = new Core(agencyId);
    }

    /**
     * For obtaining singleton Core object. Synchronized to prevent race conditions if starting lots
     * of optional modules.
     *
     * @returns the Core singleton object for this application, or null if it could not be created
     */
    public static synchronized Core getInstance() {
        if (SINGLETON == null) {
            createCore();
        }
        return SINGLETON;
    }

    /**
     * Returns true if core application. If GTFS processing or other application then not a Core
     * application and should't try to read in data such as route names for a trip.
     *
     * @return true if core application
     */
    public static boolean isCoreApplication() {
        return SINGLETON != null;
    }

    /**
     * Makes the config data available to all
     *
     * @return
     */
    public DbConfig getDbConfig() {
        return configData;
    }

    /**
     * Returns the ServiceUtils object that can be reused for efficiency.
     */
    public ServiceUtils getServiceUtils() {
        return service;
    }

    /**
     * For when need system time but might be in playback mode. If not in playback mode then the
     * time will be the time of the system clock. But if in playback mode then will be using a
     * SettableSystemTime and the time will be that of the last AVL report.
     *
     * @return The system epoch time
     */
    public long getSystemTime() {
        return systemTime.get();
    }

    /**
     * For when need system time but might be in playback mode. If not in playback mode then the
     * time will be the time of the system clock. But if in playback mode then will be using a
     * SettableSystemTime and the time will be that of the last AVL report.
     *
     * @return The system epoch time
     */
    public Date getSystemDate() {
        return new Date(getSystemTime());
    }

    /**
     * For setting the system time when in playback or batch mode.
     */
    public void setSystemTime(long systemEpochTime) {
        this.systemTime = new SettableSystemTime(systemEpochTime);
    }

    /**
     * Returns the Core logger so that each class doesn't need to create its own and have it be
     * configured properly.
     *
     * @return
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * Returns the DataDbLogger for logging data to db.
     *
     * @return
     */
    public DataDbLogger getDbLogger() {
        return dataDbLogger;
    }

    /**
     * Processes all command line options using Apache CLI. Further info at
     * http://commons.apache.org/proper/commons-cli/usage.html
     */
    @SuppressWarnings("static-access") // Needed for using OptionBuilder
    private static void processCommandLineOptions(String[] args) throws ParseException {
        // Specify the options
        Options options = new Options();
        options.addOption("h", "help", false, "Display usage and help info.");

        options.addOption(OptionBuilder.withArgName("configRev")
                .hasArg()
                .withDescription("Specifies optional configuration revision. "
                        + "If not set then the configuration rev will be read "
                        + "from the ActiveRevisions table in the database.")
                .create("configRev"));

        // Parse the options
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(options, args);

        // Handle optional config rev
        if (cmd.hasOption("configRev")) {
            configRevStr = cmd.getOptionValue("configRev");
        }

        // Handle help option
        if (cmd.hasOption("h")) {
            // Display help
            final String commandLineSyntax = "java transitclock.jar";
            final PrintWriter writer = new PrintWriter(System.out);
            final HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp(
                    writer,
                    80, // printedRowWidth
                    commandLineSyntax,
                    "args:", // header
                    options,
                    2, // spacesBeforeOption
                    2, // spacesBeforeOptionDescription
                    null, // footer
                    true); // displayUsage
            writer.close();
            System.exit(0);
        }
    }

    /**
     * Start the RMI Servers so that clients can obtain data on predictions, vehicles locations,
     * etc.
     */
    public static void startRmiServers(String agencyId) {
        PredictionsServer.start(agencyId, PredictionDataCache.getInstance());
        VehiclesServer.start(agencyId, VehicleDataCache.getInstance());
        ConfigServer.start(agencyId);
        ServerStatusServer.start(agencyId);
        CommandsServer.start(agencyId);
        CacheQueryServer.start(agencyId);
        PredictionAnalysisServer.start(agencyId);
        HoldingTimeServer.start(agencyId);
    }

    private static void populateCaches() throws Exception {
        Session session = HibernateUtils.getSession();

        Date endDate = Calendar.getInstance().getTime();

        if (!cacheReloadStartTimeStr.getValue().isEmpty() && !cacheReloadEndTimeStr.getValue().isEmpty()) {
            if (TripDataHistoryCacheFactory.getInstance() != null) {
                logger.debug(
                        "Populating TripDataHistoryCache cache for period {} to {}",
                        cacheReloadStartTimeStr.getValue(),
                        cacheReloadEndTimeStr.getValue());
                TripDataHistoryCacheFactory.getInstance()
                        .populateCacheFromDb(
                                session,
                                new Date(Time.parse(cacheReloadStartTimeStr.getValue())
                                        .getTime()),
                                new Date(Time.parse(cacheReloadEndTimeStr.getValue())
                                        .getTime()));
            }

            if (FrequencyBasedHistoricalAverageCache.getInstance() != null) {
                logger.debug(
                        "Populating FrequencyBasedHistoricalAverageCache cache for period {} to {}",
                        cacheReloadStartTimeStr.getValue(),
                        cacheReloadEndTimeStr.getValue());
                FrequencyBasedHistoricalAverageCache.getInstance()
                        .populateCacheFromDb(
                                session,
                                new Date(Time.parse(cacheReloadStartTimeStr.getValue())
                                        .getTime()),
                                new Date(Time.parse(cacheReloadEndTimeStr.getValue())
                                        .getTime()));
            }

            if (StopArrivalDepartureCacheFactory.getInstance() != null) {
                logger.debug(
                        "Populating StopArrivalDepartureCache cache for period {} to {}",
                        cacheReloadStartTimeStr.getValue(),
                        cacheReloadEndTimeStr.getValue());
                StopArrivalDepartureCacheFactory.getInstance()
                        .populateCacheFromDb(
                                session,
                                new Date(Time.parse(cacheReloadStartTimeStr.getValue())
                                        .getTime()),
                                new Date(Time.parse(cacheReloadEndTimeStr.getValue())
                                        .getTime()));
            }
            /*
            if(ScheduleBasedHistoricalAverageCache.getInstance()!=null)
            {
            	logger.debug("Populating ScheduleBasedHistoricalAverageCache cache for period {} to {}",cacheReloadStartTimeStr.getValue(),cacheReloadEndTimeStr.getValue());
            	ScheduleBasedHistoricalAverageCache.getInstance().populateCacheFromDb(session, new Date(Time.parse(cacheReloadStartTimeStr.getValue()).getTime()), new Date(Time.parse(cacheReloadEndTimeStr.getValue()).getTime()));
            }
            */
        } else {
            for (int i = 0; i < CoreConfig.getDaysPopulateHistoricalCache(); i++) {
                Date startDate = DateUtils.addDays(endDate, -1);

                if (TripDataHistoryCacheFactory.getInstance() != null) {
                    logger.debug("Populating TripDataHistoryCache cache for period {} to {}", startDate, endDate);
                    TripDataHistoryCacheFactory.getInstance().populateCacheFromDb(session, startDate, endDate);
                }

                if (FrequencyBasedHistoricalAverageCache.getInstance() != null) {
                    logger.debug(
                            "Populating FrequencyBasedHistoricalAverageCache cache for period {} to" + " {}",
                            startDate,
                            endDate);
                    FrequencyBasedHistoricalAverageCache.getInstance().populateCacheFromDb(session, startDate, endDate);
                }

                endDate = startDate;
            }

            endDate = Calendar.getInstance().getTime();

            /* populate one day at a time to avoid memory issue */
            for (int i = 0; i < CoreConfig.getDaysPopulateHistoricalCache(); i++) {
                Date startDate = DateUtils.addDays(endDate, -1);
                if (StopArrivalDepartureCacheFactory.getInstance() != null) {
                    logger.debug("Populating StopArrivalDepartureCache cache for period {} to {}", startDate, endDate);
                    StopArrivalDepartureCacheFactory.getInstance().populateCacheFromDb(session, startDate, endDate);
                }

                endDate = startDate;
            }
            endDate = Calendar.getInstance().getTime();

            for (int i = 0; i < CoreConfig.getDaysPopulateHistoricalCache(); i++) {
                Date startDate = DateUtils.addDays(endDate, -1);

                if (ScheduleBasedHistoricalAverageCache.getInstance() != null) {
                    logger.debug(
                            "Populating ScheduleBasedHistoricalAverageCache cache for period {} to" + " {}",
                            startDate,
                            endDate);
                    ScheduleBasedHistoricalAverageCache.getInstance().populateCacheFromDb(session, startDate, endDate);
                }

                endDate = startDate;
            }
        }
    }

    public static void main(String[] args) {
        try {
            try {
                processCommandLineOptions(args);
            } catch (ParseException e1) {
                logger.error("Something happened while processing command line options", e1);
                System.exit(-1);
            }

            // Write pid file so that monit can automatically start
            // or restart this application
            PidFile.createPidFile(CoreConfig.getPidFileDirectory() + AgencyConfig.getAgencyId() + ".pid");

            try {
                populateCaches();
            } catch (Exception e) {
                logger.error("Failed to populate cache.", e);
            }

            // Close cache if shutting down.
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    logger.info("Closing cache.");
                    CacheManagerFactory.getInstance().close();
                    logger.info("Cache closed.");
                } catch (Exception e) {
                    logger.error("Cache close failed...", e);
                }
            }));

            // Initialize the core now
            createCore();

            // Start any optional modules.
            List<String> optionalModuleNames = CoreConfig.getOptionalModules();
            if (optionalModuleNames.isEmpty()) {
                logger.info("No optional modules to start up.");
            } else {
                for (String moduleName : optionalModuleNames) {
                    logger.info("Starting up optional module " + moduleName);
                    Module.start(moduleName);
                }
            }

            // Start the RMI Servers so that clients can obtain data
            // on predictions, vehicles locations, etc.
            String agencyId = AgencyConfig.getAgencyId();
            Optional.ofNullable(agencyId)
                    .ifPresent(Core::startRmiServers);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
