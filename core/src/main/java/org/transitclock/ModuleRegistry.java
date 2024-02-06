package org.transitclock;

import lombok.NonNull;
import org.transitclock.core.TimeoutHandlerModule;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ModuleRegistry extends Registry<Module> {
    private final String agencyId;

    public ModuleRegistry(@NonNull String agencyId) {
        this.agencyId = agencyId;
    }

    public TimeoutHandlerModule getTimeoutHandlerModule() {
        return get(TimeoutHandlerModule.class);
    }

    @NonNull
    public <T extends Module> T create(Class<?> classname) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        // Create the module object using reflection by calling the constructor
        // and passing in agencyId
        Constructor<?> constructor = classname.getConstructor(String.class);
        T instance = (T) constructor.newInstance(agencyId);

        register(instance);

        return instance;
    }
}
