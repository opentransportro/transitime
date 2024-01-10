package org.transitclock.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dagger.Module;
import dagger.Provides;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.transitclock.utils.threading.NamedThreadFactory;

import javax.inject.Singleton;
import javax.sql.DataSource;

@Module
public class DatabaseModule {
    @Provides
    @Singleton
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl( "jdbc:postgresql://localhost:5432/STPT" );
        config.setUsername( "postgres" );
        config.setPassword( "transitclock" );
        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(300000);
        config.setConnectionTimeout(120000);
        config.setLeakDetectionThreshold(300000);
        config.setMinimumIdle(5);

        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        //config.addDataSourceProperty("hibernate.hbm2ddl.auto", "create");
        HikariDataSource hikariDataSource = new HikariDataSource(config);
        hikariDataSource.setThreadFactory(new NamedThreadFactory("hikar-cp"));
        return hikariDataSource;
    }

    @Provides
    @Singleton
    public SessionFactory sessionFactory(DataSource dataSource) {
        Configuration config = new Configuration();
        config.configure();
        return config.buildSessionFactory(
                new StandardServiceRegistryBuilder()
                        .applySettings(config.getProperties())
                        .applySetting(Environment.DATASOURCE, dataSource)
                        .build()
        );
    }

    @Provides
    public Session session(SessionFactory sessionFactory) {
        return sessionFactory.openSession();
    }
}
