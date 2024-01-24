/* (C)2023 */
package org.transitclock.core.holdingmethod;

import org.transitclock.config.StringConfigValue;
import org.transitclock.utils.ClassInstantiator;

/**
 * @author Sean Óg Crudden
 */
public class HoldingTimeGeneratorFactory {
    // The name of the class to instantiate
    private static final StringConfigValue className = new StringConfigValue(
            "transitclock.core.holdingTimeGeneratorClass",
            null,
            "Specifies the name of the class used for generating " + "holding times.");

    private static HoldingTimeGenerator singleton = null;

    public static HoldingTimeGenerator getInstance() {
        // If the HoldingTimeGenerator hasn't been created yet then do so now
        if (singleton == null) {
            if (className.getValue() != null && !className.getValue().isEmpty())
                singleton = ClassInstantiator.instantiate(className.getValue(), HoldingTimeGenerator.class);
        }

        return singleton;
    }
}
