package org.transitclock;

import dagger.BindsInstance;
import dagger.Component;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.transitclock.db.DatabaseModule;
import org.transitclock.utils.threading.AsyncModule;

import javax.inject.Singleton;
import javax.sql.DataSource;
import java.util.concurrent.ExecutorService;

@Singleton
@Component(modules = {
    AsyncModule.class,
    DatabaseModule.class,
})
public interface ApplicationFactory {
    @Singleton
    DataSource dataSource();

    @Singleton
    SessionFactory sessionFactory();

    @Singleton
    ExecutorService executorService();

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder commandLineParameters(CommandLineParameters cli);

        ApplicationFactory build();
    }
}
