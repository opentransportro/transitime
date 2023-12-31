/* (C)2023 */
package org.transitclock.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for instantiating a class by name by using reflection. Handles exceptions and
 * logging during the casting process.
 *
 * @author SkiBu Smith
 */
@Slf4j
public class ClassInstantiator {

    /**
     * Instantiates the named class using reflection and a no-arg constructor. If could not
     * instantiate the class then an error is logged and null is returned.
     *
     * @param className Name of the class to be instantiated
     * @param clazz So can do a cast to make sure the className is for the desired class and so can
     *     get desired class name for logging errors
     * @return The instantiated object, or null if it could not be instantiated
     */
    public static <T> T instantiate(String className, Class<T> clazz) {
        try {
            // Instantiate the object for the specified className
            Class<?> theClass = Class.forName(className);
            Object uncastInstance = theClass.getDeclaredConstructor().newInstance();

            // Make sure the created object is of the proper class. If it is not
            // then a ClassCastException is thrown.
            return clazz.cast(uncastInstance);
        } catch (ClassCastException e) {
            logger.error("Could not cast {} to a {}", className, clazz.getName(), e);
            return null;
        } catch (ClassNotFoundException
                | SecurityException
                | InstantiationException
                | IllegalAccessException
                | IllegalArgumentException e) {
            logger.error("Could not instantiate class {}. ", className, e);
            return null;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
