/* (C)2023 */
package org.transitclock.config;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** For injecting a class dependency via an input configuration parameter. */
public class ClassListConfigValue extends ConfigValue<List<Class>> {

    public ClassListConfigValue(String id, List<Class> defaultValue, String description) {
        super(id, defaultValue, description);
    }

    @Override
    protected List<Class> convertFromString(List<String> dataStr) {
        List<Class> classes = dataStr.stream()
                .map(it -> {
                    try {
                        return Class.forName(it.trim());
                    } catch (ClassNotFoundException ignored) {
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        return classes;
    }
}
