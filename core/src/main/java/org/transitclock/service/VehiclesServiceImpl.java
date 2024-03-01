/* (C)2023 */
package org.transitclock.service;

import com.querydsl.jpa.impl.JPAQuery;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.transitclock.core.BlockInfoProvider;
import org.transitclock.core.dataCache.VehicleDataCache;
import org.transitclock.domain.hibernate.HibernateUtils;
import org.transitclock.domain.structs.Block;
import org.transitclock.domain.structs.QRoute;
import org.transitclock.domain.structs.Trip;
import org.transitclock.domain.structs.VehicleConfig;
import org.transitclock.domain.structs.VehicleToBlockConfig;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.service.contract.VehiclesInterface;
import org.transitclock.service.dto.IpcActiveBlock;
import org.transitclock.service.dto.IpcBlock;
import org.transitclock.service.dto.IpcVehicle;
import org.transitclock.service.dto.IpcVehicleComplete;
import org.transitclock.service.dto.IpcVehicleConfig;
import org.transitclock.service.dto.IpcVehicleGtfsRealtime;
import org.transitclock.service.dto.IpcVehicleToBlockConfig;

import java.io.Serializable;
import java.util.*;

@Slf4j
@Component
public class VehiclesServiceImpl implements VehiclesInterface {
    private final VehicleDataCache vehicleDataCache;
    private final BlockInfoProvider blockInfoProvider;
    private final DbConfig dbConfig;
    private final BlockComparator blockComparator;

    public VehiclesServiceImpl(VehicleDataCache vehicleDataCache, BlockInfoProvider blockInfoProvider, DbConfig dbConfig) {
        this.vehicleDataCache = vehicleDataCache;
        this.blockInfoProvider = blockInfoProvider;
        this.dbConfig = dbConfig;
        this.blockComparator = new BlockComparator(dbConfig);
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.VehiclesInterface#get()
     */
    @Override
    public Collection<IpcVehicle> get() {
        return getSerializableCollection(vehicleDataCache.getVehicles());
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.VehiclesInterface#getComplete()
     */
    @Override
    public Collection<IpcVehicleComplete> getComplete() {
        return getCompleteSerializableCollection(vehicleDataCache.getVehicles());
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.VehiclesInterface#getGtfsRealtime()
     */
    @Override
    public Collection<IpcVehicleGtfsRealtime> getGtfsRealtime() {
        return getGtfsRealtimeSerializableCollection(vehicleDataCache.getVehicles());
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.VehiclesInterface#get(java.lang.String)
     */
    @Override
    public IpcVehicle get(String vehicleId) {
        return vehicleDataCache.getVehicle(vehicleId);
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.VehiclesInterface#get(java.lang.String)
     */
    @Override
    public IpcVehicleComplete getComplete(String vehicleId) {
        return vehicleDataCache.getVehicle(vehicleId);
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.VehiclesInterface#get(java.util.List)
     */
    @Override
    public Collection<IpcVehicle> get(Collection<String> vehicleIds) {
        return getSerializableCollection(vehicleDataCache.getVehicles(vehicleIds));
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.VehiclesInterface#get(java.util.List)
     */
    @Override
    public Collection<IpcVehicleComplete> getComplete(Collection<String> vehicleIds) {
        return getCompleteSerializableCollection(vehicleDataCache.getVehicles(vehicleIds));
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.VehiclesInterface#getForRoute(java.lang.String)
     */
    @Override
    public Collection<IpcVehicle> getForRoute(String routeIdOrShortName) {
        return getSerializableCollection(vehicleDataCache.getVehiclesForRoute(routeIdOrShortName));
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.VehiclesInterface#getForRoute(java.lang.String)
     */
    @Override
    public Collection<IpcVehicleComplete> getCompleteForRoute(String routeIdOrShortName) {
        return getCompleteSerializableCollection(vehicleDataCache.getVehiclesForRoute(routeIdOrShortName));
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.VehiclesInterface#getForRoute(java.util.Collection)
     */
    @Override
    public Collection<IpcVehicle> getForRoute(Collection<String> routeIdsOrShortNames) {
        return getSerializableCollection(vehicleDataCache.getVehiclesForRoute(routeIdsOrShortNames));
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.VehiclesInterface#getForRoute(java.util.Collection)
     */
    @Override
    public Collection<IpcVehicleComplete> getCompleteForRoute(Collection<String> routeIdsOrShortNames) {
        return getCompleteSerializableCollection(vehicleDataCache.getVehiclesForRoute(routeIdsOrShortNames));
    }

    /*
     * This class returns Collections of Vehicles that are to be serialized.
     * But sometimes these collections come from Map<K, T>.values(), which
     * is a Collection that is not serializable. For such non-serializable
     * collections this method returns a serializable version.
     */
    private Collection<IpcVehicle> getSerializableCollection(Collection<IpcVehicleComplete> vehicles) {
        // If vehicles is null then return empty array
        if (vehicles == null)
            return new ArrayList<>();

        return new ArrayList<>(vehicles);
    }

    /**
     * This class returns Collection of Vehicles that are to be serialized. But sometimes these
     * collections come from Map<K, T>.values(), which is a Collection that is not serializable. For
     * such non-serializable collections this method returns a serializable version. If vehicles
     * parameter is null then an empty array is returned.
     *
     * @param vehicles Original, possible not serializable, collection of vehicles. Can be null.
     * @return Serializable Collection if IpcGtfsRealtimeVehicle objects.
     */
    private Collection<IpcVehicleGtfsRealtime> getGtfsRealtimeSerializableCollection(
            Collection<IpcVehicleComplete> vehicles) {
        // If vehicles is null then return empty array.
        if (vehicles == null) return new ArrayList<IpcVehicleGtfsRealtime>();

        return new ArrayList<IpcVehicleGtfsRealtime>(vehicles);
    }

    /**
     * This class returns Collection of Vehicles that are to be serialized. But sometimes these
     * collections come from Map<K, T>.values(), which is a Collection that is not serializable. For
     * such non-serializable collections this method returns a serializable version. If vehicles
     * parameter is null then an empty array is returned.
     *
     * @param vehicles Original, possible not serializable, collection of vehicles. Can be null.
     * @return Serializable Collection if IpcCompleteVehicle objects.
     */
    private Collection<IpcVehicleComplete> getCompleteSerializableCollection(Collection<IpcVehicleComplete> vehicles) {
        // If vehicles is null then return empty array.
        if (vehicles == null) return new ArrayList<>();

        if (vehicles instanceof Serializable) {
            return vehicles;
        } else {
            return new ArrayList<IpcVehicleComplete>(vehicles);
        }
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.VehiclesInterface#getActiveBlocks()
     */
    @Override
    public Collection<IpcActiveBlock> getActiveBlocks(Collection<String> routeIds, int allowableBeforeTimeSecs) {
        // List of data to be returned
        List<IpcActiveBlock> results = new ArrayList<>();
        // Determine all the active blocks
        List<Block> blocks = blockInfoProvider.getCurrentlyActiveBlocks(routeIds, null, allowableBeforeTimeSecs, -1);
        // For each active block determine associated vehicle
        for (Block block : blocks) {
            IpcBlock ipcBlock;
            try {
                ipcBlock = new IpcBlock(block, dbConfig);
            } catch (Exception ex) {
                continue;
            }

            // If a block doesn't have a vehicle associated with it need
            // to determine which route a block is currently associated with
            // since can't get that info from the vehicle. This way the block
            // can be properly grouped with the associated route even when it
            // doesn't have a vehicle assigned.
            int activeTripIndex = block.activeTripIndex(dbConfig, new Date(), allowableBeforeTimeSecs);

            // Determine vehicles associated with the block if there are any
            Collection<String> vehicleIdsForBlock =
                    vehicleDataCache.getVehiclesByBlockId(block.getId());
            Collection<IpcVehicle> ipcVehiclesForBlock = get(vehicleIdsForBlock);

            // Create and add the IpcActiveBlock
            Trip tripForSorting = block.getTrip(activeTripIndex);
            IpcActiveBlock ipcBlockAndVehicle =
                    new IpcActiveBlock(ipcBlock, activeTripIndex, ipcVehiclesForBlock, tripForSorting);
            results.add(ipcBlockAndVehicle);
        }
        // Sort the results so that ordered by route and then block start time
        results.sort(blockComparator);

        // Return results
        return results;
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.VehiclesInterface#getActiveBlocks()
     */
    @Override
    public int getNumActiveBlocks(Collection<String> routeIds, int allowableBeforeTimeSecs) {
        // Determine all the active blocks
        List<Block> blocks = blockInfoProvider.getCurrentlyActiveBlocks(routeIds, null, allowableBeforeTimeSecs, -1);

        return blocks.size();
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.VehiclesInterface#getActiveBlocks()
     */
    @Override
    public Collection<IpcActiveBlock> getActiveBlocksWithoutVehicles(
            Collection<String> routeIds, int allowableBeforeTimeSecs) {
        // List of data to be returned
        List<IpcActiveBlock> results = new ArrayList<>();
        // Determine all the active blocks
        List<Block> blocks = blockInfoProvider.getCurrentlyActiveBlocks(routeIds, null, allowableBeforeTimeSecs, -1);
        // For each active block determine associated vehicle
        for (Block block : blocks) {
            try {
                IpcBlock ipcBlock = new IpcBlock(block, dbConfig);
                int activeTripIndex = block.activeTripIndex(dbConfig, new Date(), allowableBeforeTimeSecs);

                // Create and add the IpcActiveBlock, skipping the slow vehicle fetching
                Trip tripForSorting = block.getTrip(activeTripIndex);
                IpcActiveBlock ipcBlockAndVehicle =
                        new IpcActiveBlock(ipcBlock, activeTripIndex, new ArrayList<IpcVehicle>(), tripForSorting);
                results.add(ipcBlockAndVehicle);
            } catch (Exception e) {
                logger.warn("Error while fecthing active blocks data (probably hibernate still loading data): {}", e.getMessage());
            }
        }
        // Sort the results so that ordered by route and then block start time
        results.sort(new BlockComparator(dbConfig));

        // Return results
        return results;
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.VehiclesInterface#getActiveBlocksAndVehiclesByRouteId()
     */
    @Override
    public Collection<IpcActiveBlock> getActiveBlocksAndVehiclesByRouteId(String routeId, int allowableBeforeTimeSecs) {
        Collection<String> routeIds = new ArrayList<>();
        routeIds.add(routeId);
        return getActiveBlocksAndVehiclesByRouteId(routeIds, allowableBeforeTimeSecs);
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.VehiclesInterface#getActiveBlocksAndVehiclesByRouteName()
     */
    @Override
    public Collection<IpcActiveBlock> getActiveBlocksAndVehiclesByRouteName(
            String routeName, int allowableBeforeTimeSecs) {

        List<String> routeIds;
        try (Session session = HibernateUtils.getSession()) {
            JPAQuery<String> query = new JPAQuery<>(session);
            var qentity = QRoute.route;
            query = query
                    .from(qentity)
                    .select(qentity.id);
            if (routeName != null && !routeName.isEmpty()) {
                query.where(qentity.name.eq(routeName.trim()));
            }
            routeIds = query
                    .groupBy(qentity.id)
                    .fetch();
        }

        return getActiveBlocksAndVehiclesByRouteId(routeIds, allowableBeforeTimeSecs);
    }

    private Collection<IpcActiveBlock> getActiveBlocksAndVehiclesByRouteId(
            Collection<String> routeIds, int allowableBeforeTimeSecs) {

        // List of data to be returned
        List<IpcActiveBlock> results = new ArrayList<>();
        // Determine all the active blocks
        List<Block> blocks = blockInfoProvider.getCurrentlyActiveBlocks(routeIds, null, allowableBeforeTimeSecs, -1);
        // For each active block determine associated vehicle
        for (Block block : blocks) {
            IpcBlock ipcBlock = new IpcBlock(block, dbConfig);
            // If a block doesn't have a vehicle associated with it need
            // to determine which route a block is currently associated with
            // since can't get that info from the vehicle. This way the block
            // can be properly grouped with the associated route even when it
            // doesn't have a vehicle assigned.
            int activeTripIndex = block.activeTripIndex(dbConfig, new Date(), allowableBeforeTimeSecs);

            Trip tripForSorting = block.getTrip(activeTripIndex);

            // Check that block's active trip is for the specified route
            // (Otherwise, could be a past or future trip)
            if (routeIds != null && !routeIds.isEmpty() && !routeIds.contains(tripForSorting.getRouteId())) continue;

            // Determine vehicles associated with the block if there are any
            Collection<String> vehicleIdsForBlock =
                    vehicleDataCache.getVehiclesByBlockId(block.getId());
            Collection<IpcVehicle> ipcVehiclesForBlock = get(vehicleIdsForBlock);

            // Create and add the IpcActiveBlock
            IpcActiveBlock ipcBlockAndVehicle =
                    new IpcActiveBlock(ipcBlock, activeTripIndex, ipcVehiclesForBlock, tripForSorting);
            results.add(ipcBlockAndVehicle);
        }
        // Sort the results so that ordered by route and then block start time
        results.sort(new BlockComparator(dbConfig));

        // Return results
        return results;
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.VehiclesInterface#getVehicleConfigs()
     */
    @Override
    public Collection<IpcVehicleConfig> getVehicleConfigs() {
        Collection<IpcVehicleConfig> result = new ArrayList<>();
        for (VehicleConfig vehicleConfig : vehicleDataCache.getVehicleConfigs()) {
            result.add(new IpcVehicleConfig(vehicleConfig));
        }

        return result;
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.VehiclesInterface#getVehiclesForBlocks()
     */
    @Override
    public Collection<IpcVehicle> getVehiclesForBlocks() {
        List<String> vehicleIds = new ArrayList<>();
        List<Block> blocks = blockInfoProvider.getCurrentlyActiveBlocks();
        for (Block block : blocks) {
            Collection<String> vehicleIdsForBlock =
                    vehicleDataCache.getVehiclesByBlockId(block.getId());
            vehicleIds.addAll(vehicleIdsForBlock);
        }
        return get(vehicleIds);
    }

    @Override
    public Collection<IpcVehicleToBlockConfig> getVehicleToBlockConfig(String blockId) {
        List<IpcVehicleToBlockConfig> result = new ArrayList<>();
        Session session = HibernateUtils.getSession();
        try {
            for (VehicleToBlockConfig vTBC : VehicleToBlockConfig.getVehicleToBlockConfigsByBlockId(session, blockId)) {
                result.add(new IpcVehicleToBlockConfig(vTBC));
            }
            session.close();
        } catch (Exception ex) {
            session.close();
        }
        return result;
    }

    @Override
    public Collection<IpcVehicleToBlockConfig> getVehicleToBlockConfigByVehicleId(String vehicleId) {
        List<IpcVehicleToBlockConfig> result = new ArrayList<>();
        Session session = HibernateUtils.getSession();
        try {
            for (var vTBC : VehicleToBlockConfig.getVehicleToBlockConfigsByVehicleId(session, vehicleId)) {
                result.add(new IpcVehicleToBlockConfig(vTBC));
            }
            session.close();
        } catch (Exception ex) {
            session.close();
        }
        return result;
    }

    /**
     * For sorting a collection of IpcActiveBlock objects such that ordered by route order and then
     * by block start time.
     */
    private static class BlockComparator implements Comparator<IpcActiveBlock> {
        private final DbConfig dbConfig;
        public BlockComparator(DbConfig dbConfig) {
            this.dbConfig = dbConfig;
        }
        /**
         * Returns negative if b1<b2, zero if b1=b2, and positive if b1>b2
         */
        @Override
        public int compare(IpcActiveBlock b1, IpcActiveBlock b2) {
            if (b1 == null && b2 == null) return 0;
            if (b1 == null
                    || b1.getTripForSorting() == null
                    || b1.getTripForSorting().getRoute(dbConfig) == null
                    || b1.getTripForSorting().getRoute(dbConfig).getRouteOrder() == null) return -1;
            if (b2 == null
                    || b2.getTripForSorting() == null
                    || b2.getTripForSorting().getRoute(dbConfig) == null
                    || b2.getTripForSorting().getRoute(dbConfig).getRouteOrder() == null) return 1;

            int routeOrder1 = b1.getTripForSorting().getRoute(dbConfig).getRouteOrder();
            int routeOrder2 = b2.getTripForSorting().getRoute(dbConfig).getRouteOrder();

            if (routeOrder1 < routeOrder2) return -1;
            if (routeOrder1 > routeOrder2) return 1;

            // Route order is the same so order by trip start time
            int blockStartTime1 = b1.getTripForSorting().getBlock(dbConfig).getStartTime();
            int blockStartTime2 = b2.getTripForSorting().getBlock(dbConfig).getStartTime();
            if (blockStartTime1 < blockStartTime2) return -1;
            if (blockStartTime1 > blockStartTime2) return 1;
            return 0;
        }
    };
}
