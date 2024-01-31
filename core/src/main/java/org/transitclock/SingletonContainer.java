package org.transitclock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonContainer {
    private static final Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();

    public <T> T getInstance(Class<T> tClass) {
        return (T) singletons.get(tClass);
    }

    public <T> T registerInstance(Class<T> tClass, T object) {
        return (T) singletons.computeIfAbsent(tClass, k -> object);
    }
}
