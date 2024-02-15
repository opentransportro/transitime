/* (C)2023 */
package org.transitclock.core.dataCache;

import org.transitclock.config.ClassConfigValue;
import org.transitclock.utils.ClassInstantiator;

/**
 * @author Sean Óg Crudden Factory that will provide cache to hold dwell time model class instances
 *     for each stop.
 */
public class DwellTimeModelCacheFactory {
    private static final ClassConfigValue className = new ClassConfigValue(
            "transitclock.core.cache.dwellTimeModelCache",
            DummyDwellTimeModelCacheImpl.class,
            "Specifies the class used to cache RLS data for a stop.");

    private static DwellTimeModelCacheInterface singleton = null;

    public static DwellTimeModelCacheInterface getInstance() {
        if (singleton == null && className.getValue() != null) {
            singleton = ClassInstantiator.instantiate(className.getValue(), DwellTimeModelCacheInterface.class);
        }

        return singleton;
    }
}
