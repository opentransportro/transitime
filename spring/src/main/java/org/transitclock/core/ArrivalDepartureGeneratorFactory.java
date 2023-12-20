/* (C)2023 */
package org.transitclock.core;

import org.transitclock.config.StringConfigValue;
import org.transitclock.utils.ClassInstantiator;

/**
 * For instantiating a ArrivalDepartureGenerator object that generates arrival/departure data when a
 * new match is generated for a vehicle. The class to be instantiated can be set using the config
 * variable transitclock.core.arrivalDepartureGeneratorClass
 *
 * @author SkiBu Smith
 */
public class ArrivalDepartureGeneratorFactory {

    // The name of the class to instantiate
    private static StringConfigValue className = new StringConfigValue(
            "transitclock.core.arrivalDepartureGeneratorClass",
            "org.transitclock.core.ArrivalDepartureGeneratorDefaultImpl",
            "Specifies the name of the class used for generating " + "arrival/departure data.");

    private static ArrivalDepartureGenerator singleton = null;

    /********************** Member Functions **************************/
    public static ArrivalDepartureGenerator getInstance() {
        // If the PredictionGenerator hasn't been created yet then do so now
        if (singleton == null) {
            singleton = ClassInstantiator.instantiate(className.getValue(), ArrivalDepartureGenerator.class);
        }

        return singleton;
    }
}
