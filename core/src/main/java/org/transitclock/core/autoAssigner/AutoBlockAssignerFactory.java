package org.transitclock.core.autoAssigner;

import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private VehicleDataCache vehicleDataCache;
    @Autowired
    private TravelTimes travelTimes;
    @Autowired
    private VehicleStateManager vehicleStateManager;
    @Autowired
    private TemporalMatcher temporalMatcher;
    @Autowired
    private DbConfig dbConfig;
    @Autowired
    private BlockInfoProvider blockInfoProvider;

    public AutoBlockAssigner createAssigner(VehicleState vehicleState) {
        return new AutoBlockAssigner(vehicleState, vehicleDataCache, travelTimes, vehicleStateManager, temporalMatcher, dbConfig, blockInfoProvider);
    }
}
