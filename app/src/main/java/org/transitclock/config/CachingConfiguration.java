/* (C)2023 */
package org.transitclock.config;

import jakarta.annotation.PostConstruct;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import org.transitclock.ApplicationProperties;
import org.transitclock.core.dataCache.DwellTimeModelCacheInterface;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheInterface;
import org.transitclock.core.dataCache.TripDataHistoryCacheInterface;
import org.transitclock.core.dataCache.frequency.FrequencyBasedHistoricalAverageCache;
import org.transitclock.core.dataCache.scheduled.ScheduleBasedHistoricalAverageCache;
import org.transitclock.domain.hibernate.HibernateUtils;
import org.transitclock.utils.Time;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;
import org.hibernate.Session;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.transitclock.utils.ApplicationShutdownSupport.addShutdownHook;

@Configuration
@Slf4j
public class CachingConfiguration {

    @PostConstruct
    void registerShutdownHook() {
        addShutdownHook("close-cache", () -> {
            try {
                logger.info("Closing cache.");
                cacheManager().close();
                logger.info("Cache closed.");
            } catch (Exception e) {
                logger.error("Cache close failed...", e);
            }
        });
    }

    @Bean
    public CacheManager cacheManager() {
        URL xmlConfigUrl = CachingConfiguration.class
            .getClassLoader()
            .getResource("ehcache.xml");
        if (xmlConfigUrl == null) {
            throw new RuntimeException("Could not find ehcache.xml");
        }
        XmlConfiguration xmlConfig = new XmlConfiguration(xmlConfigUrl);

        CacheManager cm = CacheManagerBuilder.newCacheManager(xmlConfig);
        cm.init();

        return cm;
    }

    @Bean
    public CacheInitializer cacheInitializer(FrequencyBasedHistoricalAverageCache frequencyBasedHistoricalAverageCache,
                                             ScheduleBasedHistoricalAverageCache scheduleBasedHistoricalAverageCache,
                                             TripDataHistoryCacheInterface tripDataHistoryCacheInterface,
                                             StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface,
                                             DwellTimeModelCacheInterface dwellTimeModelCacheInterface,
                                             ApplicationProperties properties) {
        return new CacheInitializer(frequencyBasedHistoricalAverageCache,
            scheduleBasedHistoricalAverageCache, tripDataHistoryCacheInterface,
            stopArrivalDepartureCacheInterface, dwellTimeModelCacheInterface,
                                    properties.getCore());
    }

    @RequiredArgsConstructor
    public static class CacheInitializer implements ApplicationListener<ApplicationStartedEvent> {
        private final FrequencyBasedHistoricalAverageCache frequencyBasedHistoricalAverageCache;
        private final ScheduleBasedHistoricalAverageCache scheduleBasedHistoricalAverageCache;
        private final TripDataHistoryCacheInterface tripDataHistoryCacheInterface;
        private final StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface;
        private final DwellTimeModelCacheInterface dwellTimeModelCacheInterface;
        private final ApplicationProperties.Core coreProperties;

        @SneakyThrows
        @Override
        public void onApplicationEvent(ApplicationStartedEvent event) {
            Session session = HibernateUtils.getSession();
            Date endDate = Calendar.getInstance().getTime();

            String cacheReloadStartTimeStr = coreProperties.getCacheReloadStartTimeStr();
            String cacheReloadEndTimeStr = coreProperties.getCacheReloadEndTimeStr();
            if (!cacheReloadStartTimeStr.isEmpty() && !cacheReloadEndTimeStr.isEmpty()) {
                if (tripDataHistoryCacheInterface != null) {
                    logger.debug("Populating TripDataHistoryCache cache for period {} to {}",
                                 cacheReloadStartTimeStr, cacheReloadEndTimeStr);
                    tripDataHistoryCacheInterface
                        .populateCacheFromDb(
                            session,
                            new Date(Time.parse(cacheReloadStartTimeStr).getTime()),
                            new Date(Time.parse(cacheReloadEndTimeStr).getTime())
                        );
                }

                if (frequencyBasedHistoricalAverageCache != null) {
                    logger.debug(
                        "Populating FrequencyBasedHistoricalAverageCache cache for period {} to {}",
                        cacheReloadStartTimeStr,
                        cacheReloadEndTimeStr);
                    frequencyBasedHistoricalAverageCache
                        .populateCacheFromDb(
                            session,
                            new Date(Time.parse(cacheReloadStartTimeStr).getTime()),
                            new Date(Time.parse(cacheReloadEndTimeStr).getTime())
                        );
                }

                if (stopArrivalDepartureCacheInterface != null) {
                    logger.debug(
                        "Populating StopArrivalDepartureCache cache for period {} to {}",
                        cacheReloadStartTimeStr,
                        cacheReloadEndTimeStr);
                    stopArrivalDepartureCacheInterface
                        .populateCacheFromDb(
                            session,
                            new Date(Time.parse(cacheReloadStartTimeStr).getTime()),
                            new Date(Time.parse(cacheReloadEndTimeStr).getTime())
                        );
                }
                if (dwellTimeModelCacheInterface != null) {
                    logger.debug(
                        "Populating DwellTimeModelCacheInterface cache for period {} to {}",
                        cacheReloadStartTimeStr,
                        cacheReloadEndTimeStr);
                    dwellTimeModelCacheInterface
                        .populateCacheFromDb(
                            session,
                            new Date(Time.parse(cacheReloadStartTimeStr).getTime()),
                            new Date(Time.parse(cacheReloadEndTimeStr).getTime())
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
                for (int i = 0; i < coreProperties.getCache().getDaysPopulateHistoricalCache(); i++) {
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
                for (int i = 0; i < coreProperties.getCache().getDaysPopulateHistoricalCache(); i++) {
                    Date startDate = DateUtils.addDays(endDate, -1);
                    if (stopArrivalDepartureCacheInterface != null) {
                        logger.debug("Populating StopArrivalDepartureCache cache for period {} to {}", startDate, endDate);
                        stopArrivalDepartureCacheInterface.populateCacheFromDb(session, startDate, endDate);
                    }

                    endDate = startDate;
                }
                endDate = Calendar.getInstance().getTime();

                for (int i = 0; i < coreProperties.getCache().getDaysPopulateHistoricalCache(); i++) {
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
    }
}
