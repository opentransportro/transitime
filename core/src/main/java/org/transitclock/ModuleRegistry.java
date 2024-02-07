package org.transitclock;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.transitclock.core.TimeoutHandlerModule;
import org.transitclock.utils.threading.ExtendedScheduledThreadPoolExecutor;
import org.transitclock.utils.threading.NamedThreadFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.*;

@Slf4j
public class ModuleRegistry extends Registry<Module> {
    private final String agencyId;
    private final ScheduledExecutorService executor;

    public ModuleRegistry(@NonNull String agencyId) {
        this.agencyId = agencyId;
        ThreadFactory threadFactory = new NamedThreadFactory("module-thread-pool");
        executor = new ExtendedScheduledThreadPoolExecutor(3, threadFactory, new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                logger.error("Execution of {} was rejected by {}", r, executor);
            }
        });
    }

    public TimeoutHandlerModule getTimeoutHandlerModule() {
        return get(TimeoutHandlerModule.class);
    }

    @NonNull
    public <T extends Module> T createAndSchedule(Class<?> classname) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        // Create the module object using reflection by calling the constructor
        // and passing in agencyId
        Constructor<?> constructor = classname.getConstructor(String.class);
        T instance = (T) constructor.newInstance(agencyId);

        register(instance);


        logger.info("Starting module {} with configuration [{} ms - delay, {} ms - period, {} execution type]",
                instance.getClass().getSimpleName(),
                instance.initialExecutionDelay(),
                instance.executionPeriod(),
                instance.getExecutionType());
        Module.ExecutionType executionType = instance.getExecutionType();
        if(executionType == Module.ExecutionType.FIXED_RATE) {
            executor.scheduleAtFixedRate(instance, instance.initialExecutionDelay(), instance.executionPeriod(), TimeUnit.MILLISECONDS);
        } else if(executionType == Module.ExecutionType.FIXED_DELAY) {
            executor.scheduleWithFixedDelay(instance, instance.initialExecutionDelay(), instance.executionPeriod(), TimeUnit.MILLISECONDS);
        } else {
            executor.schedule(instance, instance.initialExecutionDelay(), TimeUnit.MILLISECONDS);
        }
        return instance;
    }
}
