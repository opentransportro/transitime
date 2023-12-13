/* (C)2023 */
package org.transitclock.config;

import java.util.List;

/**
 * For specifying a Float parameter that can be read in from xml config file.
 *
 * @author SkiBu Smith
 */
public class FloatConfigValue extends ConfigValue<Float> {

    /**
     * Constructor for when there is no default value.
     *
     * @param id
     * @param description
     */
    public FloatConfigValue(String id, String description) {
        super(id, description);
    }

    /**
     * Constructor.
     *
     * @param id
     * @param defaultValue
     * @param description
     */
    public FloatConfigValue(String id, Float defaultValue, String description) {
        super(id, defaultValue, description);
    }

    @Override
    protected Float convertFromString(List<String> dataList) {
        return Float.valueOf(dataList.get(0).trim());
    }
}
