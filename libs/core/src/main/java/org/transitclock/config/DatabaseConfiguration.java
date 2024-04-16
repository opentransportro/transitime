package org.transitclock.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.transitclock.ApplicationProperties;
import org.transitclock.config.data.DbSetupConfig;
import org.transitclock.core.ServiceUtils;
import org.transitclock.domain.hibernate.DataDbLogger;
import org.transitclock.domain.structs.ActiveRevision;
import org.transitclock.domain.structs.Agency;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.utils.Time;

import java.util.TimeZone;

@Slf4j
@Configuration
public class DatabaseConfiguration {
    private final ApplicationProperties properties;

    public DatabaseConfiguration(ApplicationProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    private void migrate() {
        Flyway flyway = Flyway.configure()
                .loggers("slf4j")
                .dataSource(DbSetupConfig.getConnectionUrl(), DbSetupConfig.getDbUserName(), DbSetupConfig.getDbPassword())
                .load();
        flyway.migrate();
    }

    @Bean
    DbConfig dbConfig() {
        String agencyId = properties.getCore().getAgencyId();
        // Read in config rev from ActiveRevisions table in db
        ActiveRevision activeRevision = ActiveRevision.get(agencyId);

        // If config rev not set properly then simply log error.
        // Originally would also exit() but found that want system to
        // work even without GTFS configuration so that can test AVL feed.
        if (!activeRevision.isValid()) {
            logger.error("ActiveRevisions in database is not valid. The configuration revs must be set to proper values. {}", activeRevision);
        }
        int configRev = activeRevision.getConfigRev();

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
        return new DbConfig(properties, agencyId, configRev);
    }

    @Bean
    ServiceUtils serviceUtils(DbConfig dbConfig) {
        return dbConfig.getServiceUtils();
    }

    @Bean
    Time time(DbConfig dbConfig){
        return dbConfig.getTime();
    }

    @Bean
    DataDbLogger dataDbLogger() {
        var coreConfig = properties.getCore();
        String agencyId = coreConfig.getAgencyId();
        boolean storeDataInDatabase = coreConfig.getStoreDataInDatabase();
        boolean pauseIfDbQueueFilling = coreConfig.getPauseIfDbQueueFilling();
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
        return new DataDbLogger(agencyId, storeDataInDatabase, pauseIfDbQueueFilling);
    }
}
