package org.transitclock;

import lombok.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Registry<C> {
    protected final Map<Class<?>, C> data;

    public Registry() {
        this.data = new ConcurrentHashMap<>();
    }

    @NonNull
    public <T extends C> T register(@NonNull T obj) {
        if(data.containsKey(obj.getClass())) {
            throw new RuntimeException("Object " + obj.getClass().getSimpleName() + " is already registered");
        }

        data.put(obj.getClass(), obj);
        registerImplementedInterfaces(obj.getClass(), obj);

        return obj;
    }

    private <T extends C> void registerImplementedInterfaces(Class<?> clazz, T obj) {
        for (Class<?> anInterface : clazz.getInterfaces()) {
            if (!anInterface.getPackageName().startsWith("java.lang")) {
                data.put(anInterface, obj);
            }
            registerImplementedInterfaces(anInterface, obj);
        }
    }

    @NonNull
    public <T extends C> T get(Class<T> object) {
        if(!data.containsKey(object)) {
            throw new RuntimeException("Object " + object.getSimpleName() + " has to be registered before fetching.");
        }
        return (T) data.get(object);
    }
}
