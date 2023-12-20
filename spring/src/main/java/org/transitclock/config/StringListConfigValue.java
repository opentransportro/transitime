/* (C)2023 */
package org.transitclock.config;

import java.util.List;

/**
 * For specifying a List of Strings parameter that can be read in from xml config file. When
 * parameter set as a command line argument then the ConfigValue.LIST_SEPARATOR is used (";") when
 * need to specify multiple items.
 *
 * @author SkiBu Smith
 */
public class StringListConfigValue extends ConfigValue<List<String>> {

    /**
     * When parameter set as a command line argument then the ConfigValue.LIST_SEPARATOR is used
     * (";") when need to specify multiple items.
     *
     * @param id
     * @param description
     */
    public StringListConfigValue(String id, String description) {
        super(id, description);
    }

    /**
     * When parameter set as a command line argument then the ConfigValue.LIST_SEPARATOR is used
     * (";") when need to specify multiple items.
     *
     * @param id
     * @param defaultValue
     * @param description
     */
    public StringListConfigValue(String id, List<String> defaultValue, String description) {
        super(id, defaultValue, description);
    }

    @Override
    protected List<String> convertFromString(List<String> dataList) {
        return dataList;
    }
}
