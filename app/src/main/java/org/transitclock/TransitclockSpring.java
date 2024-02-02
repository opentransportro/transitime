package org.transitclock;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.ehcache.CacheManager;
import org.hibernate.Session;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.transitclock.config.ConfigFileReader;
import org.transitclock.config.data.AgencyConfig;
import org.transitclock.config.data.CoreConfig;
import org.transitclock.core.ServiceUtils;
import org.transitclock.core.TimeoutHandlerModule;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheInterface;
import org.transitclock.core.dataCache.TripDataHistoryCacheInterface;
import org.transitclock.core.dataCache.frequency.FrequencyBasedHistoricalAverageCache;
import org.transitclock.core.dataCache.scheduled.ScheduleBasedHistoricalAverageCache;
import org.transitclock.domain.hibernate.HibernateUtils;
import org.transitclock.domain.structs.ActiveRevisions;
import org.transitclock.domain.structs.Agency;
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
public class TransitclockSpring implements InitializingBean, ApplicationListener<ContextRefreshedEvent> {
    public static void main(String[] args) {
        try {
            SpringApplication springApplication = new SpringApplication(TransitclockSpring.class);
            springApplication.addInitializers(new ApplicationInitializer());
            springApplication.run(args);
        } catch (Exception e) {
            Throwable rootCause = Throwables.getRootCause(e);
            throw e;
        }
    }

    private final ApplicationLifeCycle applicationLifeCycle;
    private final TripDataHistoryCacheInterface tripDataHistoryCacheInterface;
    private final StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface;
    private final CacheManager cacheManager;

    public TransitclockSpring(ApplicationLifeCycle applicationLifeCycle, TripDataHistoryCacheInterface tripDataHistoryCacheInterface, StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface, CacheManager cacheManager) {
        this.applicationLifeCycle = applicationLifeCycle;
        this.tripDataHistoryCacheInterface = tripDataHistoryCacheInterface;
        this.stopArrivalDepartureCacheInterface = stopArrivalDepartureCacheInterface;
        this.cacheManager = cacheManager;
    }

    private void populateCaches() throws Exception {
        Session session = HibernateUtils.getSession();

        Date endDate = Calendar.getInstance().getTime();

        FrequencyBasedHistoricalAverageCache frequencyBasedHistoricalAverageCache = SingletonContainer.getInstance(FrequencyBasedHistoricalAverageCache.class);
        ScheduleBasedHistoricalAverageCache scheduleBasedHistoricalAverageCache = SingletonContainer.getInstance(ScheduleBasedHistoricalAverageCache.class);

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
    public void onApplicationEvent(ContextRefreshedEvent event) {
        String agencyId = AgencyConfig.getAgencyId();

        // Set the timezone so that when dates are read from db or are logged
        // the time will be correct. Therefore, this needs to be done right at
        // the start of the application, before db is read.
        TimeZone timeZone = Agency.getTimeZoneFromDb(agencyId);
        TimeZone.setDefault(timeZone);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
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


            ThreadFactory threadFactory = new NamedThreadFactory("module-thread-pool");
            Executor executor = Executors.newFixedThreadPool(10, threadFactory);

            String agencyId = AgencyConfig.getAgencyId();
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
