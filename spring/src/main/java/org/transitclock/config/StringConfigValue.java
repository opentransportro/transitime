/* (C)2023 */
package org.transitclock.config;

import java.util.List;

/**
 * For specifying a String parameter that can be read in from xml config file.
 *
 * @author SkiBu Smith
 */
public class StringConfigValue extends ConfigValue<String> {

    /**
     * Constructor. For when there is no default value. Error will occur when parameter is
     * initialized if it is not configured.
     *
     * @param id
     * @param description
     */
    public StringConfigValue(String id, String description) {
        super(id, description);
    }

    /**
     * Constructor for when there is a default value.
     *
     * @param id
     * @param defaultValue Default value, but can be set to null for if null is a reasonable
     *     default.
     * @param description
     */
    public StringConfigValue(String id, String defaultValue, String description) {
        super(id, defaultValue, description);
    }

    /**
     * Constructor.
     *
     * @param id
     * @param defaultValue
     * @param description
     * @param okToLogValue If false then won't log value in log file. This is useful for passwords
     *     and such.
     */
    public StringConfigValue(String id, String defaultValue, String description, boolean okToLogValue) {
        super(id, defaultValue, description, okToLogValue);
    }

    @Override
    protected String convertFromString(List<String> dataList) {
        return dataList.get(0);
    }

    /**
     * So that can use the StringConfigValue directly as a String without explicitly calling
     * getValue().
     */
    public String toString() {
        return getValue();
    }
}
