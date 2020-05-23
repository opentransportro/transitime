package org.transitclock.core.predictiongenerator.bias;

import org.transitclock.config.StringConfigValue;
import org.transitclock.utils.ClassInstantiator;

public class BiasAdjusterFactory {
    // The name of the class to instantiate
    private static final StringConfigValue className = new StringConfigValue("transitclock.core.predictiongenerator.biasadjuster",
            "org.transitclock.core.predictiongenerator.bias.ExponentialBiasAdjuster",
            "Specifies the name of the class used to adjust the bias of a predction.");
    private static BiasAdjuster singleton = null;

    public static BiasAdjuster getInstance() {
        if (className != null && className.getValue() != null && className.getValue().length() > 0) {
            if (singleton == null)
                singleton = ClassInstantiator.instantiate(className.getValue(), BiasAdjuster.class);
            return singleton;

        } else {
            return null;
        }
    }
}
