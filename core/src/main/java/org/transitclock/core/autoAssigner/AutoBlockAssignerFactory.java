package org.transitclock.core.autoAssigner;

import org.springframework.stereotype.Component;
import org.transitclock.core.BlockInfoProvider;
import org.transitclock.core.TemporalMatcher;
import org.transitclock.core.TravelTimes;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.VehicleDataCache;
import org.transitclock.core.dataCache.VehicleStateManager;
import org.transitclock.gtfs.DbConfig;

@Component
public class AutoBlockAssignerFactory {
    private final VehicleDataCache vehicleDataCache;
    private final TravelTimes travelTimes;
    private final VehicleStateManager vehicleStateManager;
    private final TemporalMatcher temporalMatcher;
    private final DbConfig dbConfig;
    private final BlockInfoProvider blockInfoProvider;

    public AutoBlockAssignerFactory(VehicleDataCache vehicleDataCache, TravelTimes travelTimes, VehicleStateManager vehicleStateManager, TemporalMatcher temporalMatcher, DbConfig dbConfig, BlockInfoProvider blockInfoProvider) {
        this.vehicleDataCache = vehicleDataCache;
        this.travelTimes = travelTimes;
        this.vehicleStateManager = vehicleStateManager;
        this.temporalMatcher = temporalMatcher;
        this.dbConfig = dbConfig;
        this.blockInfoProvider = blockInfoProvider;
    }

    public AutoBlockAssigner createAssigner(VehicleState vehicleState) {
        return new AutoBlockAssigner(vehicleState, vehicleDataCache, travelTimes, vehicleStateManager, temporalMatcher, dbConfig, blockInfoProvider);
    }
}
