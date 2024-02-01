package org.transitclock;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.transitclock.core.TimeoutHandlerModule;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@UtilityClass
public class ModuleRegistry {
    private static final Map<Class<?>, Module> singletons = new ConcurrentHashMap<>();

    public synchronized  <T extends Module> T getModule(Class<? extends Module> tClass) {
        return (T) singletons.get(tClass);
    }

    public TimeoutHandlerModule getTimeoutModule() {
        return getModule(TimeoutHandlerModule.class);
    }

    public <T extends Module> T registerInstance(Class<? extends Module> tClass, T object) {
        if (singletons.containsKey(tClass)) {
            throw new RuntimeException("Module already registered for " + tClass);
        }
        return (T) singletons.computeIfAbsent(tClass, k -> object);
    }
}
