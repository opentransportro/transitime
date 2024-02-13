/* (C)2023 */
package org.transitclock.core;

import org.transitclock.config.ClassConfigValue;
import org.transitclock.utils.ClassInstantiator;

/**
 * For instantiating a PredictionGenerator object that generates predictions when a new match is
 * generated for a vehicle. The class to be instantiated can be set using the config variable
 * transitclock.core.predictionGeneratorClass
 *
 * @author SkiBu Smith
 */
public class PredictionGeneratorFactory {

    // The name of the class to instantiate
    private static final ClassConfigValue className = new ClassConfigValue(
            "transitclock.core.predictionGeneratorClass",
            org.transitclock.core.PredictionGeneratorDefaultImpl.class,
            "Specifies the name of the class used for generating prediction data.");

    private static PredictionGenerator singleton = null;

    public static synchronized PredictionGenerator getInstance() {
        // If the PredictionGenerator hasn't been created yet then do so now
        if (singleton == null) {
            singleton = ClassInstantiator.instantiate(className.getValue(), PredictionGenerator.class);
        }

        return singleton;
    }
}
