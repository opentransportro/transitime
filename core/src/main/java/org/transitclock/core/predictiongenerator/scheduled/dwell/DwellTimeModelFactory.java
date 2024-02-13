/* (C)2023 */
package org.transitclock.core.predictiongenerator.scheduled.dwell;

import org.transitclock.config.ClassConfigValue;
import org.transitclock.utils.ClassInstantiator;

/**
 * @author scrudden Returns the model that is to be used to estimate dwell time for a stop.
 */
public class DwellTimeModelFactory {
    // The name of the class to instantiate
    private static final ClassConfigValue className = new ClassConfigValue(
            "transitclock.core.dwelltime.model",
            org.transitclock.core.predictiongenerator.scheduled.dwell.DwellAverage.class,
            "Specifies the name of the class used to predict dwell.");

    public static DwellModel getInstance() {
        return ClassInstantiator.instantiate(className.getValue(), DwellModel.class);
    }
}
