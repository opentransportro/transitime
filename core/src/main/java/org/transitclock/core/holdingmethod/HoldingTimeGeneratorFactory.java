/* (C)2023 */
package org.transitclock.core.holdingmethod;

import org.transitclock.config.ClassConfigValue;
import org.transitclock.utils.ClassInstantiator;

/**
 * @author Sean Óg Crudden
 */
public class HoldingTimeGeneratorFactory {
    // The name of the class to instantiate
    private static final ClassConfigValue className = new ClassConfigValue(
            "transitclock.core.holdingTimeGeneratorClass",
            DummyHoldingTimeGeneratorImpl.class,
            "Specifies the name of the class used for generating " + "holding times.");

    private static HoldingTimeGenerator singleton = null;

    public static HoldingTimeGenerator getInstance() {
        if (singleton == null && className.getValue() != null) {
            singleton = ClassInstantiator.instantiate(className.getValue(), HoldingTimeGenerator.class);
        }

        return singleton;
    }
}
