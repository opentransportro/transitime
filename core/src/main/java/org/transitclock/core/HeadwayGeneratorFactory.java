/* (C)2023 */
package org.transitclock.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.transitclock.config.ClassConfigValue;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheInterface;
import org.transitclock.core.dataCache.VehicleDataCache;
import org.transitclock.core.dataCache.VehicleStateManager;
import org.transitclock.core.headwaygenerator.LastArrivalsHeadwayGenerator;
import org.transitclock.core.headwaygenerator.LastDepartureHeadwayGenerator;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.utils.ClassInstantiator;

/**
 * For instantiating a HeadwayGenerator object that generates headway info when a new match is
 * generated for a vehicle. The class to be instantiated can be set using the config variable
 * transitclock.core.headwayGeneratorClass
 *
 * @author SkiBu Smith
 */
@Configuration
public class HeadwayGeneratorFactory {
    private static final ClassConfigValue className = new ClassConfigValue(
            "transitclock.core.headwayGeneratorClass",
            org.transitclock.core.HeadwayGeneratorDefaultImpl.class,
            "Specifies the name of the class used for generating headway data.");

    @Bean
    public synchronized HeadwayGenerator headwayGenerator(VehicleDataCache vehicleDataCache,
                                                          VehicleStateManager vehicleStateManager,
                                                          StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface,
                                                          DbConfig dbConfig) {
        if (className.getValue() == LastArrivalsHeadwayGenerator.class) {
            return new LastArrivalsHeadwayGenerator(vehicleDataCache, vehicleStateManager, stopArrivalDepartureCacheInterface, dbConfig);
        } else if (className.getValue() == LastDepartureHeadwayGenerator.class) {
            return new LastDepartureHeadwayGenerator(vehicleDataCache, vehicleStateManager, stopArrivalDepartureCacheInterface, dbConfig);
        }

        return new HeadwayGeneratorDefaultImpl();
    }
}
