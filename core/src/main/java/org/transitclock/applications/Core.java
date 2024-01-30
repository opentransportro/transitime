/* (C)2023 */
package org.transitclock.applications;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Session;
import org.transitclock.Module;
import org.transitclock.config.ConfigFileReader;
import org.transitclock.config.data.AgencyConfig;
import org.transitclock.config.data.CoreConfig;
import org.transitclock.core.ServiceUtils;
import org.transitclock.core.TimeoutHandlerModule;
import org.transitclock.core.dataCache.PredictionDataCache;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheFactory;
import org.transitclock.core.dataCache.TripDataHistoryCacheFactory;
import org.transitclock.core.dataCache.VehicleDataCache;
import org.transitclock.core.dataCache.ehcache.CacheManagerFactory;
import org.transitclock.core.dataCache.frequency.FrequencyBasedHistoricalAverageCache;
import org.transitclock.core.dataCache.scheduled.ScheduleBasedHistoricalAverageCache;
import org.transitclock.domain.hibernate.DataDbLogger;
import org.transitclock.domain.hibernate.HibernateUtils;
import org.transitclock.domain.structs.ActiveRevisions;
import org.transitclock.domain.structs.Agency;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.service.*;
import org.transitclock.utils.Time;
import org.transitclock.utils.threading.NamedThreadFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

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

    private final Map<Class<?>, Module> modules = new HashMap<>();

    /**
     *  For when want to use methods in Time. This is important when need methods that access a
     *  Calendar a lot. By putting the Calendar in Time it can be shared.
     */
    @Getter
    private final Time time;

    // Read in configuration files. This should be done statically before
    // the logback LoggerFactory.getLogger() is called so that logback can
    // also be configured using a transitime config file. The files are
    // specified using the java system property -Dtransitclock.configFiles .
    static {
        ConfigFileReader.processConfig();
    }

    /**
     * Construct the Core object and read in the config data. This is private so that the
     * createCore() factory method must be used.
     */
     private Core(@NonNull String agencyId) {
         // Read in config rev from ActiveRevisions table in db
         ActiveRevisions activeRevisions = ActiveRevisions.get(agencyId);

         // If config rev not set properly then simply log error.
         // Originally would also exit() but found that want system to
         // work even without GTFS configuration so that can test AVL feed.
         if (activeRevisions == null || !activeRevisions.isValid()) {
             logger.error("ActiveRevisions in database is not valid. The configuration revs must be set to proper values. {}", activeRevisions);
         }
         int configRev = activeRevisions.getConfigRev();

         // Set the timezone so that when dates are read from db or are logged
         // the time will be correct. Therefore, this needs to be done right at
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
         // HibernateUtils.clearSessionFactory();

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

         ThreadFactory threadFactory = new NamedThreadFactory("module-thread-pool");
         Executor executor = Executors.newFixedThreadPool(10, threadFactory);

         timeoutHandlerModule = new TimeoutHandlerModule(AgencyConfig.getAgencyId());
         executor.execute(timeoutHandlerModule);
         modules.put(timeoutHandlerModule.getClass(), timeoutHandlerModule);

         service = new ServiceUtils(configData);
         time = new Time(configData);

         // Start any optional modules.
         var optionalModuleNames = CoreConfig.getOptionalModules();

         if (optionalModuleNames.isEmpty()) {
             logger.info("No optional modules to start up.");
         } else {
             for (Class<?> moduleName : optionalModuleNames) {
                 logger.info("Starting up optional module {}", moduleName);
                 try {
                     Module module = createModule(moduleName, agencyId);
                     modules.put(module.getClass(), module);
                     executor.execute(module);
                 } catch (NoSuchMethodException e) {
                     logger.error("Failed to start {} because could not find constructor with agencyId arg", moduleName, e);
                 } catch (InvocationTargetException e) {
                     logger.error("Failed to start {}", moduleName, e);
                 } catch (InstantiationException e) {
                     logger.error("Failed to start {}", moduleName, e);
                 } catch (IllegalAccessException e) {
                     logger.error("Failed to start {}", moduleName, e);
                 }
             }
         }

         // Start the RMI Servers so that clients can obtain data
         // on predictions, vehicles locations, etc.
          startServices(agencyId);
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


    public static synchronized Core getInstance() {
        if (SINGLETON == null) {
            throw new RuntimeException();
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
     * Returns the DataDbLogger for logging data to db.
     *
     * @return
     */
    public DataDbLogger getDbLogger() {
        return dataDbLogger;
    }


    /**
     * Start the RMI Servers so that clients can obtain data on predictions, vehicles locations,
     * etc.
     */
    public void startServices(String agencyId) {
        PredictionsServiceImpl.start(PredictionDataCache.getInstance());
        VehiclesServiceImpl.start(VehicleDataCache.getInstance());
        ConfigServiceImpl.start();
        ServerStatusServiceImpl.start(agencyId);
        CommandsServiceImpl.start();
        CacheQueryServiceImpl.start();
        PredictionAnalysisServiceImpl.start();
        HoldingTimeServiceImpl.start();
    }

    public static void populateCaches() throws Exception {
        Session session = HibernateUtils.getSession();

        Date endDate = Calendar.getInstance().getTime();

        if (!CoreConfig.cacheReloadStartTimeStr.getValue().isEmpty() && !CoreConfig.cacheReloadEndTimeStr.getValue().isEmpty()) {
            if (TripDataHistoryCacheFactory.getInstance() != null) {
                logger.debug(
                        "Populating TripDataHistoryCache cache for period {} to {}",
                        CoreConfig.cacheReloadStartTimeStr.getValue(),
                        CoreConfig.cacheReloadEndTimeStr.getValue());
                TripDataHistoryCacheFactory.getInstance()
                        .populateCacheFromDb(
                                session,
                                new Date(Time.parse(CoreConfig.cacheReloadStartTimeStr.getValue()).getTime()),
                                new Date(Time.parse(CoreConfig.cacheReloadEndTimeStr.getValue()).getTime())
                        );
            }

            if (FrequencyBasedHistoricalAverageCache.getInstance() != null) {
                logger.debug(
                        "Populating FrequencyBasedHistoricalAverageCache cache for period {} to {}",
                        CoreConfig.cacheReloadStartTimeStr.getValue(),
                        CoreConfig.cacheReloadEndTimeStr.getValue());
                FrequencyBasedHistoricalAverageCache.getInstance()
                        .populateCacheFromDb(
                                session,
                                new Date(Time.parse(CoreConfig.cacheReloadStartTimeStr.getValue()).getTime()),
                                new Date(Time.parse(CoreConfig.cacheReloadEndTimeStr.getValue()).getTime())
                        );
            }

            if (StopArrivalDepartureCacheFactory.getInstance() != null) {
                logger.debug(
                        "Populating StopArrivalDepartureCache cache for period {} to {}",
                        CoreConfig.cacheReloadStartTimeStr.getValue(),
                        CoreConfig.cacheReloadEndTimeStr.getValue());
                StopArrivalDepartureCacheFactory.getInstance()
                        .populateCacheFromDb(
                                session,
                                new Date(Time.parse(CoreConfig.cacheReloadStartTimeStr.getValue()).getTime()),
                                new Date(Time.parse(CoreConfig.cacheReloadEndTimeStr.getValue()).getTime())
                        );
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

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @NonNull
    private static Module createModule(Class <?> classname, String agencyId) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        // Create the module object using reflection by calling the constructor
        // and passing in agencyId
        Constructor<?> constructor = classname.getConstructor(String.class);
        return (Module) constructor.newInstance(agencyId);
    }
}
