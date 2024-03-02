/* (C)2023 */
package org.transitclock.core.headwaygenerator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheInterface;
import org.transitclock.core.dataCache.VehicleDataCache;
import org.transitclock.core.dataCache.VehicleStateManager;
import org.transitclock.gtfs.DbConfig;

/**
 * For instantiating a HeadwayGenerator object that generates headway info when a new match is
 * generated for a vehicle. The class to be instantiated can be set using the config variable
 * transitclock.core.headwayGeneratorClass
 *
 * @author SkiBu Smith
 */
@Configuration
public class HeadwayGeneratorFactory {
    @Value("${transitclock.factory.headway-generator:org.transitclock.core.headwaygenerator.HeadwayGeneratorDefaultImpl}")
    private Class<?> neededClass;

    @Bean
    public HeadwayGenerator headwayGenerator(VehicleDataCache vehicleDataCache,
                                                          VehicleStateManager vehicleStateManager,
                                                          StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface,
                                                          DbConfig dbConfig) {
        if (neededClass == LastArrivalsHeadwayGenerator.class) {
            return new LastArrivalsHeadwayGenerator(vehicleDataCache, vehicleStateManager, stopArrivalDepartureCacheInterface, dbConfig);
        } else if (neededClass == LastDepartureHeadwayGenerator.class) {
            return new LastDepartureHeadwayGenerator(vehicleDataCache, vehicleStateManager, stopArrivalDepartureCacheInterface, dbConfig);
        }

        return new HeadwayGeneratorDefaultImpl();
    }
}
