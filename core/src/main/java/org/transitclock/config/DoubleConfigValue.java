/* (C)2023 */
package org.transitclock.config;

import java.util.List;

/**
 * For specifying a Double parameter that can be read in from xml config file.
 *
 * @author SkiBu Smith
 */
public class DoubleConfigValue extends ConfigValue<Double> {

    /**
     * Constructor for when there is no default value.
     *
     * @param id
     * @param description
     */
    public DoubleConfigValue(String id, String description) {
        super(id, description);
    }

    /**
     * Constructor.
     *
     * @param id
     * @param defaultValue
     * @param description
     */
    public DoubleConfigValue(String id, Double defaultValue, String description) {
        super(id, defaultValue, description);
    }

    @Override
    protected Double convertFromString(List<String> dataList) {
        return Double.valueOf(dataList.get(0).trim());
    }
}
