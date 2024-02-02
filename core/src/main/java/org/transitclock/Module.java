/* (C)2023 */
package org.transitclock;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Modules are run in a separate thread and continuously process data. They are initiated by core
 * prediction system by configuring which modules should be started.
 *
 * <p>Each module should inherit from this class and implement run() and a constructor that takes
 * the agencyId as a string parameter.
 *
 * @author SkiBu Smith
 */
@Slf4j
public abstract class Module implements Runnable {

    protected final String agencyId;

    /**
     * Constructor. Subclasses must implement a constructor that takes in agencyId and calls this
     * constructor via super(agencyId).
     *
     * @param agencyId
     */
    protected Module(String agencyId) {
        this.agencyId = agencyId;
    }

    protected String getAgencyId() {
        return agencyId;
    }

    @NonNull
    public static Module createModule(Class <?> classname, String agencyId) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        // Create the module object using reflection by calling the constructor
        // and passing in agencyId
        Constructor<?> constructor = classname.getConstructor(String.class);
        return (Module) constructor.newInstance(agencyId);
    }

}
