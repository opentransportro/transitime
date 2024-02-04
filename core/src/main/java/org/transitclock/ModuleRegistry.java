package org.transitclock;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.transitclock.core.TimeoutHandlerModule;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModuleRegistry {
    private final Map<Class<?>, Module> data = new ConcurrentHashMap<>();
    private final String agencyId;

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

    @NonNull
    public Module createModule(Class <?> classname) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        // Create the module object using reflection by calling the constructor
        // and passing in agencyId
        Constructor<?> constructor = classname.getConstructor(String.class);
        return (Module) constructor.newInstance(agencyId);
    }
}
