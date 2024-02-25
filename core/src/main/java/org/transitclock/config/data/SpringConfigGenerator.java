package org.transitclock.config.data;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;
import org.transitclock.config.*;

import java.lang.reflect.Field;
import java.util.stream.Stream;

public class SpringConfigGenerator {
    public static void main(String[] args) {
        Stream.of(
            AgencyConfig.class,
            ApiConfig.class,
            ArrivalsDeparturesConfig.class,
            AvlConfig.class,
            BlockAssignerConfig.class,
            CoreConfig.class,
            FormattingConfig.class,
            GtfsConfig.class,
            HoldingConfig.class,
            MonitoringConfig.class,
            PredictionAccuracyConfig.class,
            ServiceConfig.class,
            TimeoutConfig.class,
            TraveltimesConfig.class,
            TripDataCacheConfig.class,
            UpdatesConfig.class,
            WebConfig.class
        ).forEach(x -> {
            System.out.printf("%n@Data%npublic static class %s {%n", x.getSimpleName().replace("Config", ""));
            ReflectionUtils.doWithFields(x, SpringConfigGenerator::computeConfigField);
            System.out.print("}");
        });
    }

    private static void computeConfigField(Field field) throws IllegalAccessException {
        field.setAccessible(true);
        ConfigValue<?> configValue = (ConfigValue<?>) field.get(null);
        Class<?> typeOfField = getFieldType(field.getType());

        if (typeOfField == Object.class)
            return;
        try {
            var fieldValue = configValue.getDefaultValue();
            if (fieldValue instanceof String) {
                fieldValue = "\"" + fieldValue + "\"";
            }
            if (fieldValue instanceof Long) {
                fieldValue =  fieldValue + "L";
            }

            if (fieldValue instanceof Float) {
                fieldValue =  fieldValue + "F";
            }
            System.out.printf("    // config param: %s%n", configValue.getID());
            System.out.printf("    // %s%n", configValue.getDescription());
            System.out.printf("    private %s %s=%s;%n%n", typeOfField.getSimpleName(), getFieldName(configValue.getID()), fieldValue);
//            System.out.printf("{\n" +
//                "  \"name\": \"%s\",\n" +
//                "  \"defaultValue\": \"%s\",\n" +
//                "  \"type\": \"%s\",\n" +
//                "  \"description\": \"%s\"\n" +
//                "},%n", configValue.getID(), configValue.getDefaultValue(), typeOfField.getName(), configValue.getDescription());
        } catch (Exception e) {
          int x = 5;
          x = x + 5;
        }
    }

    private static String getFieldName(String keyName) {
        String[] split = keyName.split("\\.");
        return split[split.length - 1];
    }

    private static Class<?> getFieldType(Class<?> field) {
        if (field.equals(BooleanConfigValue.class)) {
            return Boolean.class;
        } else if (field.equals(ClassConfigValue.class)) {
            return Class.class;
        } else if (field.equals(DoubleConfigValue.class)) {
            return Double.class;
        } else if (field.equals(FloatConfigValue.class)) {
            return Float.class;
        } else if (field.equals(IntegerConfigValue.class)) {
            return Integer.class;
        } else if (field.equals(LongConfigValue.class)) {
            return Long.class;
        } else if (field.equals(StringConfigValue.class)) {
            return String.class;
        } else if (field.equals(StringListConfigValue.class)) {
            return String.class;
        }

        return Object.class;
    }
}
