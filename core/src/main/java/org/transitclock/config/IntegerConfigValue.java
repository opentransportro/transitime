/* (C)2023 */
package org.transitclock.config;

import java.util.List;

/**
 * For specifying an Integer parameter that can be read in from xml config file.
 *
 * @author SkiBu Smith
 */
public class IntegerConfigValue extends ConfigValue<Integer> {

    /**
     * Constructor for when there is no default value.
     *
     * @param id
     * @param description
     */
    public IntegerConfigValue(String id, String description) {
        super(id, description);
    }

    /**
     * Constructor.
     *
     * @param id
     * @param defaultValue
     */
    public IntegerConfigValue(String id, Integer defaultValue, String description) {
        super(id, defaultValue, description);
    }

    @Override
    protected Integer convertFromString(List<String> dataList) {
        return Integer.valueOf(dataList.get(0).trim());
    }
}
