package org.transitclock.core.avl.assigner;

import org.springframework.stereotype.Component;
import org.transitclock.core.avl.time.TemporalMatcher;
import org.transitclock.core.TravelTimes;
import org.transitclock.core.VehicleStatus;
import org.transitclock.core.dataCache.VehicleDataCache;
import org.transitclock.core.dataCache.VehicleStatusManager;
import org.transitclock.gtfs.DbConfig;

@Component
public class AutoBlockAssignerFactory {
    private final VehicleDataCache vehicleDataCache;
    private final TravelTimes travelTimes;
    private final VehicleStatusManager vehicleStatusManager;
    private final TemporalMatcher temporalMatcher;
    private final DbConfig dbConfig;
    private final BlockInfoProvider blockInfoProvider;

    public AutoBlockAssignerFactory(VehicleDataCache vehicleDataCache, TravelTimes travelTimes, VehicleStatusManager vehicleStatusManager, TemporalMatcher temporalMatcher, DbConfig dbConfig, BlockInfoProvider blockInfoProvider) {
        this.vehicleDataCache = vehicleDataCache;
        this.travelTimes = travelTimes;
        this.vehicleStatusManager = vehicleStatusManager;
        this.temporalMatcher = temporalMatcher;
        this.dbConfig = dbConfig;
        this.blockInfoProvider = blockInfoProvider;
    }

    public AutoBlockAssigner createAssigner(VehicleStatus vehicleStatus) {
        return new AutoBlockAssigner(vehicleStatus, vehicleDataCache, travelTimes, vehicleStatusManager, temporalMatcher, dbConfig, blockInfoProvider);
    }
}
