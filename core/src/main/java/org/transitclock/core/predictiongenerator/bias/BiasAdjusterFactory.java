/* (C)2023 */
package org.transitclock.core.predictiongenerator.bias;

import org.transitclock.config.ClassConfigValue;
import org.transitclock.utils.ClassInstantiator;

public class BiasAdjusterFactory {
    private static BiasAdjuster singleton = null;

    // The name of the class to instantiate
    private static final ClassConfigValue className = new ClassConfigValue(
            "transitclock.core.predictiongenerator.biasabjuster",
            null,
            "Specifies the name of the class used to adjust the bias of a predction.");

    public static BiasAdjuster getInstance() {
        if (singleton == null && className.getValue() != null) {
            singleton = ClassInstantiator.instantiate(className.getValue(), BiasAdjuster.class);
        }
        return singleton;
    }
}
