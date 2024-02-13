/* (C)2023 */
package org.transitclock.core.predictiongenerator.datafilter;

import org.transitclock.config.ClassConfigValue;
import org.transitclock.utils.ClassInstantiator;

public class TravelTimeFilterFactory {
    private static TravelTimeDataFilter singleton = null;

    // The name of the class to instantiate
    private static final ClassConfigValue className = new ClassConfigValue(
            "transitclock.core.predictiongenerator.datafilter.traveltime",
            org.transitclock.core.predictiongenerator.datafilter.TravelTimeDataFilterImpl.class,
            "Specifies the name of the class used to filter travel times.");

    public static TravelTimeDataFilter getInstance() {
        if (singleton == null) {
            singleton = ClassInstantiator.instantiate(className.getValue(), TravelTimeDataFilter.class);
        }
        return singleton;
    }
}
