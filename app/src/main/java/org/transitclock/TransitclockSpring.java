package org.transitclock;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.ehcache.CacheManager;
import org.hibernate.Session;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.transitclock.config.ConfigFileReader;
import org.transitclock.config.data.AgencyConfig;
import org.transitclock.config.data.CoreConfig;
import org.transitclock.config.data.DbSetupConfig;
import org.transitclock.core.ServiceUtils;
import org.transitclock.core.TimeoutHandlerModule;
import org.transitclock.core.dataCache.DwellTimeModelCacheInterface;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheInterface;
import org.transitclock.core.dataCache.TripDataHistoryCacheInterface;
import org.transitclock.core.dataCache.frequency.FrequencyBasedHistoricalAverageCache;
import org.transitclock.core.dataCache.scheduled.ScheduleBasedHistoricalAverageCache;
import org.transitclock.domain.ApiKeyManager;
import org.transitclock.domain.hibernate.HibernateUtils;
import org.transitclock.domain.structs.ActiveRevisions;
import org.transitclock.domain.structs.Agency;
import org.transitclock.domain.webstructs.ApiKey;
import org.transitclock.domain.webstructs.WebAgency;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.utils.Time;
import org.transitclock.utils.threading.NamedThreadFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class TransitclockSpring implements ApplicationRunner {
    public static void main(String[] args) {
        try {
            SpringApplication springApplication = new SpringApplication(TransitclockSpring.class);
            springApplication.addInitializers(new ApplicationInitializer(args));
            springApplication.run(args);


        } catch (Exception e) {
            Throwable rootCause = Throwables.getRootCause(e);
            throw e;
        }
    }

    private static CommandLineParameters parseAndValidateCmdLine(String[] args) {
        CommandLineParameters params = new CommandLineParameters();
        try {
            // It is tempting to use JCommander's command syntax: http://jcommander.org/#_more_complex_syntaxes_commands
            // But this seems to lead to confusing switch ordering and more difficult subsequent use of the
            // parsed commands, since there will be three separate objects.
            JCommander jc = JCommander.newBuilder()
                    .addObject(params)
                    .args(args)
                    .build();

            if (params.version) {
//                System.out.println("transitime " + projectInfo().getVersionString());
                System.exit(0);
            }

            if (params.help) {
//                System.out.println("transitime " + projectInfo().getVersionString());
                jc.setProgramName("java -Xmx4G -jar transitclock.jar");
                jc.usage();
                System.exit(0);
            }
            params.inferAndValidate();
        } catch (ParameterException pex) {
            logger.error("Parameter error: {}", pex.getMessage());
            System.exit(1);
        }
        return params;
    }

    private final TripDataHistoryCacheInterface tripDataHistoryCacheInterface;
    private final StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface;
    private final DwellTimeModelCacheInterface dwellTimeModelCacheInterface;
    private final FrequencyBasedHistoricalAverageCache frequencyBasedHistoricalAverageCache;
    private final ScheduleBasedHistoricalAverageCache scheduleBasedHistoricalAverageCache;
    private final CacheManager cacheManager;
    private final ApiKeyManager apiKeyManager;

    public TransitclockSpring(TripDataHistoryCacheInterface tripDataHistoryCacheInterface, StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface, DwellTimeModelCacheInterface dwellTimeModelCacheInterface, FrequencyBasedHistoricalAverageCache frequencyBasedHistoricalAverageCache, ScheduleBasedHistoricalAverageCache scheduleBasedHistoricalAverageCache, CacheManager cacheManager, ApiKeyManager apiKeyManager) {
        this.tripDataHistoryCacheInterface = tripDataHistoryCacheInterface;
        this.stopArrivalDepartureCacheInterface = stopArrivalDepartureCacheInterface;
        this.dwellTimeModelCacheInterface = dwellTimeModelCacheInterface;
        this.frequencyBasedHistoricalAverageCache = frequencyBasedHistoricalAverageCache;
        this.scheduleBasedHistoricalAverageCache = scheduleBasedHistoricalAverageCache;
        this.cacheManager = cacheManager;
        this.apiKeyManager = apiKeyManager;
    }

    private void populateCaches() throws Exception {
        Session session = HibernateUtils.getSession();

        Date endDate = Calendar.getInstance().getTime();

        if (!CoreConfig.cacheReloadStartTimeStr.getValue().isEmpty() && !CoreConfig.cacheReloadEndTimeStr.getValue().isEmpty()) {
            if (tripDataHistoryCacheInterface != null) {
                logger.debug(
                        "Populating TripDataHistoryCache cache for period {} to {}",
                        CoreConfig.cacheReloadStartTimeStr.getValue(),
                        CoreConfig.cacheReloadEndTimeStr.getValue());
                tripDataHistoryCacheInterface
                        .populateCacheFromDb(
                                session,
                                new Date(Time.parse(CoreConfig.cacheReloadStartTimeStr.getValue()).getTime()),
                                new Date(Time.parse(CoreConfig.cacheReloadEndTimeStr.getValue()).getTime())
                        );
            }
            if (frequencyBasedHistoricalAverageCache != null) {
                logger.debug(
                        "Populating FrequencyBasedHistoricalAverageCache cache for period {} to {}",
                        CoreConfig.cacheReloadStartTimeStr.getValue(),
                        CoreConfig.cacheReloadEndTimeStr.getValue());
                frequencyBasedHistoricalAverageCache
                        .populateCacheFromDb(
                                session,
                                new Date(Time.parse(CoreConfig.cacheReloadStartTimeStr.getValue()).getTime()),
                                new Date(Time.parse(CoreConfig.cacheReloadEndTimeStr.getValue()).getTime())
                        );
            }

            if (stopArrivalDepartureCacheInterface != null) {
                logger.debug(
                        "Populating StopArrivalDepartureCache cache for period {} to {}",
                        CoreConfig.cacheReloadStartTimeStr.getValue(),
                        CoreConfig.cacheReloadEndTimeStr.getValue());
                stopArrivalDepartureCacheInterface
                        .populateCacheFromDb(
                                session,
                                new Date(Time.parse(CoreConfig.cacheReloadStartTimeStr.getValue()).getTime()),
                                new Date(Time.parse(CoreConfig.cacheReloadEndTimeStr.getValue()).getTime())
                        );
            }

            if(dwellTimeModelCacheInterface != null) {
                dwellTimeModelCacheInterface
                        .populateCacheFromDb(session,
                                new Date(Time.parse(CoreConfig.cacheReloadStartTimeStr.getValue()).getTime()),
                                new Date(Time.parse(CoreConfig.cacheReloadEndTimeStr.getValue()).getTime()));
            }
        } else {
            for (int i = 0; i < CoreConfig.getDaysPopulateHistoricalCache(); i++) {
                Date startDate = DateUtils.addDays(endDate, -1);

                if (tripDataHistoryCacheInterface != null) {
                    logger.debug("Populating TripDataHistoryCache cache for period {} to {}", startDate, endDate);
                    tripDataHistoryCacheInterface.populateCacheFromDb(session, startDate, endDate);
                }

                if (frequencyBasedHistoricalAverageCache != null) {
                    logger.debug(
                            "Populating FrequencyBasedHistoricalAverageCache cache for period {} to" + " {}",
                            startDate,
                            endDate);
                    frequencyBasedHistoricalAverageCache.populateCacheFromDb(session, startDate, endDate);
                }

                endDate = startDate;
            }

            endDate = Calendar.getInstance().getTime();

            /* populate one day at a time to avoid memory issue */
            for (int i = 0; i < CoreConfig.getDaysPopulateHistoricalCache(); i++) {
                Date startDate = DateUtils.addDays(endDate, -1);
                if (stopArrivalDepartureCacheInterface != null) {
                    logger.debug("Populating StopArrivalDepartureCache cache for period {} to {}", startDate, endDate);
                    stopArrivalDepartureCacheInterface.populateCacheFromDb(session, startDate, endDate);
                }

                if (dwellTimeModelCacheInterface != null) {
                    dwellTimeModelCacheInterface.populateCacheFromDb(session, startDate, endDate);
                }

                endDate = startDate;
            }
            endDate = Calendar.getInstance().getTime();

            for (int i = 0; i < CoreConfig.getDaysPopulateHistoricalCache(); i++) {
                Date startDate = DateUtils.addDays(endDate, -1);

                if (scheduleBasedHistoricalAverageCache != null) {
                    logger.debug(
                            "Populating ScheduleBasedHistoricalAverageCache cache for period {} to" + " {}",
                            startDate,
                            endDate);
                    scheduleBasedHistoricalAverageCache.populateCacheFromDb(session, startDate, endDate);
                }

                endDate = startDate;
            }
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        CommandLineParameters cli = parseAndValidateCmdLine(args.getSourceArgs());

        if (cli.shouldLoadGtfs()) {
            GtfsFileProcessor processor = GtfsFileProcessor.createGtfsFileProcessor(cli);
            processor.process();
        }

        try {
            populateCaches();
        } catch (Exception e) {
            logger.error("Failed to populate cache.", e);
        }

        // Close cache if shutting down.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                logger.info("Closing cache.");
                cacheManager.close();
                logger.info("Cache closed.");
            } catch (Exception e) {
                logger.error("Cache close failed...", e);
            }
        }));

        try {
            apiKeyManager
                    .generateApiKey(
                            "Sean Og Crudden",
                            "http://www.transitclock.org",
                            "og.crudden@gmail.com",
                            "123456",
                            "foo");

            WebAgency webAgency = new WebAgency(AgencyConfig.getAgencyId(),
                    "127.0.0.1",
                    true,
                    DbSetupConfig.getDbName(),
                    DbSetupConfig.getDbType(),
                    DbSetupConfig.getDbHost(),
                    DbSetupConfig.getDbUserName(),
                    DbSetupConfig.getDbPassword());

            // Store the WebAgency
            webAgency.store("web");

            String agencyId = AgencyConfig.getAgencyId();

            // Set the timezone so that when dates are read from db or are logged
            // the time will be correct. Therefore, this needs to be done right at
            // the start of the application, before db is read.
            TimeZone timeZone = Agency.getTimeZoneFromDb(agencyId);
            TimeZone.setDefault(timeZone);

            ThreadFactory threadFactory = new NamedThreadFactory("module-thread-pool");
            Executor executor = Executors.newFixedThreadPool(10, threadFactory);

            TimeoutHandlerModule timeoutHandlerModule = new TimeoutHandlerModule(agencyId);
            executor.execute(timeoutHandlerModule);
            ModuleRegistry.registerInstance(TimeoutHandlerModule.class, timeoutHandlerModule);

            // Start any optional modules.
            var optionalModuleNames = CoreConfig.getOptionalModules();

            if (optionalModuleNames.isEmpty()) {
                logger.info("No optional modules to start up.");
            } else {
                for (Class<?> moduleName : optionalModuleNames) {
                    logger.info("Starting up optional module {}", moduleName);
                    try {
                        Module module = Module.createModule(moduleName, agencyId);
                        ModuleRegistry.registerInstance(module.getClass(), module);
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

//            var serverInstance = createWebserver();
//            serverInstance.start();
//            logger.info("Go to http://localhost:" + cli.port + " in your browser");
//            serverInstance.join();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
