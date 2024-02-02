package org.transitclock.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.internal.guava.ThreadFactoryBuilder;
import org.hibernate.SessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.transitclock.ApplicationLifeCycle;
import org.transitclock.SingletonContainer;
import org.transitclock.config.data.AgencyConfig;
import org.transitclock.config.data.CoreConfig;
import org.transitclock.core.ServiceUtils;
import org.transitclock.domain.hibernate.DataDbLogger;
import org.transitclock.domain.repository.ActiveRevisionsDao;
import org.transitclock.domain.structs.ActiveRevisions;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.utils.Time;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class BaseConfiguration {
    private final ApplicationContext applicationContext;

    /**
     * A {@link TaskScheduler} with a fixed pool size of 1
     * @return
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(1);
        threadPoolTaskScheduler.setThreadNamePrefix("TCLOCK scheduler-");
        return threadPoolTaskScheduler;
    }

    @Bean
    public ExecutorService pollerExecutor() {
        return Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("TCLOCK poller")
                .build());
    }


    /**
     * A {@link TaskExecutor} with a pool size 0-5 that allows the core thread to time out.
     *
     * @return
     */
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(0);
        threadPoolTaskExecutor.setAllowCoreThreadTimeOut(true);
        threadPoolTaskExecutor.setMaxPoolSize(5);
        threadPoolTaskExecutor.setQueueCapacity(100);
        threadPoolTaskExecutor.setThreadNamePrefix("TCLOCK executor-");

        return threadPoolTaskExecutor;
    }

    @Bean
    public ApplicationLifeCycle applicationLifeCycle() {
        return new ApplicationLifeCycle();
    }

    @Bean
    DataDbLogger dataDbLogger() {
        return DataDbLogger.getDataDbLogger(AgencyConfig.getAgencyId(), CoreConfig.storeDataInDatabase(), CoreConfig.pauseIfDbQueueFilling());
    }

    @Bean
    DbConfig dbConfig(ActiveRevisionsDao activeRevisionsDao) {
        String agencyId = AgencyConfig.getAgencyId();
        // Read in config rev from ActiveRevisions table in db
        ActiveRevisions activeRevisions = activeRevisionsDao.get();

        // If config rev not set properly then simply log error.
        // Originally would also exit() but found that want system to
        // work even without GTFS configuration so that can test AVL feed.
        if (activeRevisions == null || !activeRevisions.isValid()) {
            logger.error("ActiveRevisions in database is not valid. The configuration revs must be set to proper values. {}", activeRevisions);
        }
        int configRev = activeRevisions.getConfigRev();

        // Read in all GTFS based config data from the database
        // Contains the configuration data read from database
        DbConfig configData = new DbConfig(agencyId, configRev);

        SingletonContainer.registerInstance(DbConfig.class, configData);
        SingletonContainer.registerInstance(ServiceUtils.class, configData.getServiceUtils());
        SingletonContainer.registerInstance(Time.class, configData.getTime());

        return configData;
    }
    @Bean
    ServiceUtils serviceUtils(DbConfig dbConfig) {
        return dbConfig.getServiceUtils();
    }

    @Bean
    Time time(DbConfig dbConfig) {
        return dbConfig.getTime();
    }

    @PostConstruct
    private void postConstruct() throws BeansException {

    }

}
