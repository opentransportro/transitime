package org.transitclock;

import lombok.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Registry<C> {
    private final Map<Class<?>, C> data;

    public Registry() {
        this.data = new ConcurrentHashMap<>();
    }

    @NonNull
    public <T extends C> T register(@NonNull T obj) {
        if(data.containsKey(obj.getClass())) {
            throw new RuntimeException("Object " + obj.getClass().getSimpleName() + " is already registered");
        }

        data.put(obj.getClass(), obj);
        return obj;
    }

    @NonNull
    public <T extends C> T get(Class<T> object) {
        if(!data.containsKey(object)) {
            throw new RuntimeException("Object " + object.getSimpleName() + " has to be registered before fetching.");
        }
        return (T) data.get(object);
    }
}
