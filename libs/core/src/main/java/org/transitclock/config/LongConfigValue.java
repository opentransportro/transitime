/* (C)2023 */
package org.transitclock.config;

import java.util.List;

/**
 * For specifying a Long parameter that can be read in from xml config file.
 *
 * @author SkiBu Smith
 */
public class LongConfigValue extends ConfigValue<Long> {

    /**
     * Constructor. For when no default value.
     *
     * @param id
     * @param description
     */
    public LongConfigValue(String id, String description) {
        super(id, description);
    }

    /**
     * Constructor.
     *
     * @param id
     * @param defaultValue
     * @param description
     */
    public LongConfigValue(String id, Long defaultValue, String description) {
        super(id, defaultValue, description);
    }

    @Override
    protected Long convertFromString(List<String> dataList) {
        return Long.valueOf(dataList.get(0).trim());
    }
}
