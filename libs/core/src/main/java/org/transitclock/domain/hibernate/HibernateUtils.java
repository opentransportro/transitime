/* (C)2023 */
package org.transitclock.domain.hibernate;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.transitclock.domain.structs.ActiveRevision;
import org.transitclock.domain.structs.Agency;
import org.transitclock.domain.structs.Arrival;
import org.transitclock.domain.structs.AvlReport;
import org.transitclock.domain.structs.Block;
import org.transitclock.domain.structs.Calendar;
import org.transitclock.domain.structs.CalendarDate;
import org.transitclock.domain.structs.ConfigRevision;
import org.transitclock.domain.structs.DbTest;
import org.transitclock.domain.structs.Departure;
import org.transitclock.domain.structs.ExportTable;
import org.transitclock.domain.structs.FareAttribute;
import org.transitclock.domain.structs.FareRule;
import org.transitclock.domain.structs.Frequency;
import org.transitclock.domain.structs.Headway;
import org.transitclock.domain.structs.HoldingTime;
import org.transitclock.domain.structs.Match;
import org.transitclock.domain.structs.MeasuredArrivalTime;
import org.transitclock.domain.structs.MonitoringEvent;
import org.transitclock.domain.structs.Prediction;
import org.transitclock.domain.structs.PredictionAccuracy;
import org.transitclock.domain.structs.PredictionEvent;
import org.transitclock.domain.structs.PredictionForStopPath;
import org.transitclock.domain.structs.Route;
import org.transitclock.domain.structs.Stop;
import org.transitclock.domain.structs.StopPath;
import org.transitclock.domain.structs.Transfer;
import org.transitclock.domain.structs.TravelTimesForStopPath;
import org.transitclock.domain.structs.TravelTimesForTrip;
import org.transitclock.domain.structs.Trip;
import org.transitclock.domain.structs.TripPattern;
import org.transitclock.domain.structs.VehicleConfig;
import org.transitclock.domain.structs.VehicleEvent;
import org.transitclock.domain.structs.VehicleState;
import org.transitclock.domain.structs.VehicleToBlockConfig;
import org.transitclock.domain.webstructs.ApiKey;
import org.transitclock.domain.webstructs.WebAgency;

import com.querydsl.jpa.impl.JPAQuery;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.procedure.ProcedureCall;
import org.hibernate.query.NativeQuery;
import org.hibernate.service.ServiceRegistry;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

@Slf4j
public class HibernateUtils {

    // List here all the annotated classes that can be stored in the db
    private static final Class<?>[] classList = new Class[] {
        ActiveRevision.class,
        Agency.class,
        Arrival.class,
        AvlReport.class,
        Block.class,
        Calendar.class,
        CalendarDate.class,
        ConfigRevision.class,
        DbTest.class,
        ExportTable.class,
        Prediction.class,
        Departure.class,
        FareAttribute.class,
        FareRule.class,
        Frequency.class,
        Match.class,
        MeasuredArrivalTime.class,
        MonitoringEvent.class,
        PredictionAccuracy.class,
        Route.class,
        Stop.class,
        StopPath.class,
        Transfer.class,
        TravelTimesForStopPath.class,
        PredictionForStopPath.class,
        TravelTimesForTrip.class,
        Trip.class,
        TripPattern.class,
        VehicleEvent.class,
        PredictionEvent.class,
        VehicleConfig.class,
        VehicleState.class,
        VehicleToBlockConfig.class,
        HoldingTime.class,
        Headway.class,

        // For website
        ApiKey.class,
        WebAgency.class,
    };
    // Cache. Keyed on database name
    private static final Map<String, SessionFactory> sessionFactoryCache = new ConcurrentHashMap<>();
    private static final Map<Thread, ThreadLocal<Session>> threadSessions = new ConcurrentHashMap<>();
    public static DataSourceProperties dataSourceProperties;
    public static void registerDatasourceProperties(DataSourceProperties props) {
        dataSourceProperties = props;
    }

    private static SessionFactory createSessionFactory(String dbName) throws HibernateException {
        Configuration config = new Configuration();

        for (Class<?> aClass : classList) {
            config.addAnnotatedClass(aClass);
        }

        // Set the db info for the URL, user name, and password. Uses the
        // property hibernate.connection.url if it is set so that everything
        // can be overwritten in a standard way. If that property not set then
        // uses values from DbSetupConfig if set. If they are not set then the
        // values will be obtained from the hibernate.cfg.xml config file.
        String dbUrl = dataSourceProperties.getUrl();
        config.setProperty(AvailableSettings.JAKARTA_JDBC_URL, dbUrl);

        String dbUserName = dataSourceProperties.getUsername();
        if (dbUserName != null) {
            config.setProperty(AvailableSettings.JAKARTA_JDBC_USER, dbUserName);
        } else {
            dbUserName = config.getProperty(AvailableSettings.JAKARTA_JDBC_USER);
        }

        if (dataSourceProperties.getPassword() != null) {
            config.setProperty(AvailableSettings.JAKARTA_JDBC_PASSWORD, dataSourceProperties.getPassword());
        }

        // Log info, but don't log password. This can just be debug logging
        // even though it is important because the C3P0 connector logs the info.
        logger.info("For Hibernate factory project using url={} username={}, and configured password",
                dbUrl,
                dbUserName);

        // Get the session factory for persistence
        Properties properties = config.getProperties();
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(properties).build();

        // Return the factory
        return config.buildSessionFactory(serviceRegistry);
    }


    /**
     * Returns a cached Hibernate SessionFactory. Returns null if there is a problem.
     *
     * @param dbName Database name
     * @return {@link SessionFactory}
     */
    public static SessionFactory getSessionFactory(String dbName) throws HibernateException {
        // Determine the database name to use. Will usually use the
        // projectId since each project has a database. But this might
        // be overridden by the transitclock.core.dbName property.
        SessionFactory factory;

        synchronized (sessionFactoryCache) {
            factory = sessionFactoryCache.get(dbName);
            // If factory not yet created for this projectId then create it
            if (factory == null || factory.isClosed()) {
                try {
                    factory = createSessionFactory(dbName);
                    sessionFactoryCache.put(dbName, factory);
                } catch (Exception e) {
                    logger.error("Could not create SessionFactory for dbName={}", dbName, e);
                    throw e;
                }
            }
        }

        return factory;
    }

    /**
     * Clears out the session factory so that a new one will be created for the dbName. This way new
     * db connections are made. This is useful for dealing with timezones and postgres. For that
     * situation want to be able to read in timezone from db so can set default timezone. Problem
     * with postgres is that once a factory is used to generate sessions the database will continue
     * to use the default timezone that was configured at that time. This means that future calls to
     * the db will use the wrong timezone! Through this function one can read in timezone from
     * database, set the default timezone, clear the factory so that future db connections will use
     * the newly configured timezone, and then successfully process dates.
     */
    public static void clearSessionFactory() {
        sessionFactoryCache.forEach((s, sessionFactory) -> {
            if(sessionFactory.isOpen()) {
                sessionFactory.close();
            }
        });
        sessionFactoryCache.clear();
    }

    /**
     * Returns session for the specified agencyId.
     *
     * <p>NOTE: Make sure you close the session after the query!! Use a try/catch around the query
     * and close the session in a finally block to make sure it happens. The system only gets a
     * limited number of sessions!!
     *
     * @param agencyId Used as the database name if the property transitclock.core.dbName is not set
     * @return The Session. Make sure you close it when done because system only gets limited number
     *     of open sessions.
     * @throws HibernateException
     */
    public static Session getSession(String agencyId) throws HibernateException {
        SessionFactory sessionFactory = getSessionFactory(agencyId);
        return sessionFactory.openSession();
    }


    public static <T> JPAQuery<T> getJPAQuery() {
        return new JPAQuery<>(getSession());
    }

    /**
     * Returns the session for the database name specified by the transitclock.db.dbName Java
     * property.
     *
     * <p>NOTE: Make sure you close the session after the query!! Use a try/catch around the query
     * and close the session in a finally block to make sure it happens. The system only gets a
     * limited number of sessions!!
     *
     * @return The Session. Make sure you close it when done because system only gets limited number
     *     of open sessions.
     */
    public static Session getSession() {
        return threadSessions
            .compute(Thread.currentThread(), (t, s) -> {
                String dbName = "psql";
                if (s == null) {
                    ThreadLocal<Session> sessionThreadLocal = new ThreadLocal<>();
                    sessionThreadLocal.set(ContextAwareSession.create(getSessionFactory(dbName)));
                    return sessionThreadLocal;
                }

                Session session = s.get();
                if (!session.isOpen()) {
                    s.set(ContextAwareSession.create(getSessionFactory(dbName)));
                }

                return s;
            })
            .get();
    }

    private static class ContextAwareSession implements Session {
        @Delegate
        private final Session session;
        private final Thread thread;

        public static ContextAwareSession create(SessionFactory sessionFactory) {
            return new ContextAwareSession(sessionFactory.openSession(), Thread.currentThread());
        }

        public ContextAwareSession(Session session, Thread thread) {
            this.session = session;
            this.thread = thread;
        }

        public Thread getContext() {
            return thread;
        }

        @Override
        public void close() throws HibernateException {
            session.close();
            threadSessions.remove(thread);
        }

        @Override
        public NativeQuery createNativeQuery(String sqlString, Class resultClass) {
            return session.createNativeQuery(sqlString, resultClass);
        }

        @Override
        public ProcedureCall createStoredProcedureQuery(String procedureName, Class... resultClasses) {
            return session.createStoredProcedureQuery(procedureName, resultClasses);
        }
    }
}
