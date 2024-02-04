package org.transitclock;

import lombok.experimental.UtilityClass;
import org.transitclock.core.TimeoutHandlerModule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class ModuleRegistry {
    private final Map<Class<?>, Module> data = new ConcurrentHashMap<>();

    public <T extends Module> T register(T module) {
        if(data.containsKey(module.getClass())) {
            throw new RuntimeException("Module " + module.getClass().getSimpleName() + " is already registered");
        }

        return (T) data.put(module.getClass(), module);
    }

    public <T extends Module> T get(Class<T> module) {
        return (T) data.get(module);
    }

    public TimeoutHandlerModule getTimeoutHandlerModule() {
        return get(TimeoutHandlerModule.class);
    }
}
