package org.transitclock.utils.threading;


import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Module
public class AsyncModule {

    @Singleton
    @Provides
    ExecutorService defaultExecutorService() {
        ThreadFactory threadFactory = new NamedThreadFactory("module-thread-pool");
        return Executors.newFixedThreadPool(10, threadFactory);
    }
}
