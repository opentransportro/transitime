/* (C)2023 */
package org.transitclock.core.predictiongenerator.datafilter;

import org.transitclock.config.ClassConfigValue;
import org.transitclock.utils.ClassInstantiator;

public class DwellTimeFilterFactory {
    private static DwellTimeDataFilter singleton = null;

    // The name of the class to instantiate
    private static final ClassConfigValue className = new ClassConfigValue(
            "transitclock.core.predictiongenerator.datafilter.dwelltime",
            org.transitclock.core.predictiongenerator.datafilter.DwellTimeDataFilterImpl.class,
            "Specifies the name of the class used to filter dwell times.");

    public static DwellTimeDataFilter getInstance() {
        if (singleton == null) {
            singleton = ClassInstantiator.instantiate(className.getValue(), DwellTimeDataFilter.class);
        }
        return singleton;
    }
}
