package org.transitclock;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;

@Slf4j
public class SingletonRegistry extends Registry<Object> {
    @Override
    @NonNull
    public <T extends Object> T get(@NonNull Class<T> tClass) {
        if (!data.containsKey(tClass)) {
            logger.info("Creating instance for {}", tClass);
            try {
                T t = tClass.getDeclaredConstructor().newInstance();
                return register(t);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        return (T) data.get(tClass);
    }
}
