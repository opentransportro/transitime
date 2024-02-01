package org.transitclock;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@UtilityClass
public class SingletonContainer {
    private static final Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();

    public synchronized  <T> T getInstance(Class<T> tClass) {
        if (!singletons.containsKey(tClass)) {
            logger.info("Creating instance for {}", tClass);
            T t;
            try {
                t = tClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            return registerInstance(tClass, t);
        }
        return (T) singletons.get(tClass);
    }

    public <T> T registerInstance(Class<T> tClass, T object) {
        return (T) singletons.computeIfAbsent(tClass, k -> object);
    }
}
