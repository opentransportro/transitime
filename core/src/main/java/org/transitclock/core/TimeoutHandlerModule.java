/* (C)2023 */
package org.transitclock.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.transitclock.ApplicationProperties;
import org.transitclock.Module;
import org.transitclock.core.avl.AvlProcessor;
import org.transitclock.core.avl.AvlReportRegistry;
import org.transitclock.core.dataCache.VehicleDataCache;
import org.transitclock.core.dataCache.VehicleStatusManager;
import org.transitclock.domain.structs.AvlReport;
import org.transitclock.domain.structs.Block;
import org.transitclock.domain.structs.VehicleEvent;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.utils.SystemTime;
import org.transitclock.utils.Time;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * For handling when a vehicle doesn't report its position for too long. Makes the vehicle
 * unpredictable if a timeout occurs.
 *
 * <p>Note: only predictable vehicles are timed out. This is because vehicles that are not in
 * service are likely to get turned off and not report their position for a long period of time.
 * Plus since they are already not predictable there is no need to be make them unpredictable when
 * there is a timeout.
 *
 * @author SkiBu Smith
 */
@Slf4j
@Component
public class TimeoutHandlerModule implements Module {
    private final VehicleDataCache vehicleDataCache;
    private final VehicleStatusManager vehicleStatusManager;
    private final AvlProcessor avlProcessor;
    private final DbConfig dbConfig;
    private final AvlReportRegistry avlReportRegistry;
    private final ApplicationProperties.Timeout timeoutConfig;
    private final Time time;
    private final ApplicationProperties applicationProperties;

    public TimeoutHandlerModule(VehicleDataCache vehicleDataCache,
                                VehicleStatusManager vehicleStatusManager,
                                AvlProcessor avlProcessor,
                                DbConfig dbConfig,
                                AvlReportRegistry avlReportRegistry,
                                ApplicationProperties properties,
                                Time time, ApplicationProperties applicationProperties) {
        this.vehicleDataCache = vehicleDataCache;
        this.vehicleStatusManager = vehicleStatusManager;
        this.avlProcessor = avlProcessor;
        this.dbConfig = dbConfig;
        this.avlReportRegistry = avlReportRegistry;
        this.timeoutConfig = properties.getTimeout();
        this.time = time;
        this.applicationProperties = applicationProperties;
    }


    /**
     * Removes the specified vehicle from the VehicleDataCache if configured to do so
     *
     * @param vehicleId Vehicle to remove
     */
    public void removeFromVehicleDataCache(String vehicleId) {
        if (timeoutConfig.getRemoveTimedOutVehiclesFromVehicleDataCache()) {
            logger.info("Removing vehicleId={} from VehicleDataCache", vehicleId);
            avlProcessor.removeFromVehicleDataCache(vehicleId);
        }
    }

    /**
     * For regular predictable vehicle that is not a schedule based prediction nor a vehicle at a
     * wait stop. If haven't reported in too long makes the vehicle unpredictable and logs
     * situation.
     */
    private void handlePredictablePossibleTimeout(
        VehicleStatus vehicleStatus, long now, Iterator<AvlReport> mapIterator) {
        // If haven't reported in too long...
        long maxNoAvl = timeoutConfig.getAllowableNoAvlSecs() * Time.MS_PER_SEC;
        if (now > vehicleStatus.getAvlReport().getTime() + maxNoAvl) {
            // Make vehicle unpredictable
            String eventDescription = "Vehicle timed out because it "
                    + "has not reported in "
                    + Time.elapsedTimeStr(now - vehicleStatus.getAvlReport().getTime())
                    + " while allowable time without an AVL report is "
                    + Time.elapsedTimeStr(maxNoAvl)
                    + " and so was made unpredictable.";
            avlProcessor
                    .makeVehicleUnpredictable(vehicleStatus.getVehicleId(), eventDescription, VehicleEvent.TIMEOUT);

            // Also log the situation
            logger.info("For vehicleId={} {}", vehicleStatus.getVehicleId(), eventDescription);

            // Remove vehicle from map for next time looking for timeouts
            mapIterator.remove();

            // Remove vehicle from cache if configured to do so
            removeFromVehicleDataCache(vehicleStatus.getVehicleId());
        }
    }

    /**
     * For not predictable vehicle. If not removing vehicles from cache, removes the vehicle from
     * the map to avoid looking at it again. If configured to remove timed out vehicles from cache,
     * and haven't reported in too long, removes the vehicle from map and cache.
     *
     * @param mapIterator So can remove AVL report from map
     */
    private void handleNotPredictablePossibleTimeout(VehicleStatus vehicleStatus,
                                                     long now,
                                                     Iterator<AvlReport> mapIterator) {
        if (!timeoutConfig.getRemoveTimedOutVehiclesFromVehicleDataCache()) {
            // Remove vehicle from map for next time looking for timeouts and return
            mapIterator.remove();
            return;
        }

        // If haven't reported in too long...
        long maxNoAvl = timeoutConfig.getAllowableNoAvlSecs() * Time.MS_PER_SEC;
        if (now > vehicleStatus.getAvlReport().getTime() + maxNoAvl) {

            // Log the situation
            logger.info("For not predictable vehicleId={} generated timeout " + "event.", vehicleStatus.getVehicleId());

            // Remove vehicle from map for next time looking for timeouts
            mapIterator.remove();

            // Remove vehicle from cache
            removeFromVehicleDataCache(vehicleStatus.getVehicleId());
        }
    }

    /**
     * For schedule based predictions. If past the scheduled departure time by more than allowed
     * amount then the schedule based vehicle is removed. Useful for situations such as when using
     * schedule based vehicles and auto assigner but the auto assigner can't find a vehicle for a
     * while, indicating no such vehicle in service.
     */
    private void handleSchedBasedPredsPossibleTimeout(VehicleStatus vehicleStatus,
                                                      long now,
                                                      Iterator<AvlReport> mapIterator) {
        // If should timeout the schedule based vehicle...
        String shouldTimeoutEventDescription = shouldTimeoutVehicle(vehicleStatus, now);
        if (shouldTimeoutEventDescription != null) {
            avlProcessor
                    .makeVehicleUnpredictable(
                            vehicleStatus.getVehicleId(), shouldTimeoutEventDescription, VehicleEvent.TIMEOUT);

            // Also log the situation
            logger.info(
                    "For schedule based vehicleId={} generated timeout " + "event. {}",
                    vehicleStatus.getVehicleId(),
                    shouldTimeoutEventDescription);

            // Remove vehicle from map for next time looking for timeouts
            mapIterator.remove();

            // Remove vehicle from cache if configured to do so
            removeFromVehicleDataCache(vehicleStatus.getVehicleId());
        }
    }


    /**
     * Determines if schedule based vehicle should be timed out. A schedule based vehicle should be
     * timed out if the block is now over (now is passed the end time of the block) or if passed the
     * start time of the block by more than the allowable number of minutes (a real vehicle was
     * never assigned to the block).
     *
     * @param vehicleStatus
     * @param now Current time
     * @return An event description if the schedule based vehicle should be timed out and
     *     predictions should be removed, otherwise null
     */
    public String shouldTimeoutVehicle(VehicleStatus vehicleStatus, long now) {
        // Make sure method only called for schedule based vehicles
        if (!vehicleStatus.isForSchedBasedPreds()) {
            logger.error(
                    "Called SchedBasedPredictionsModule.shouldTimeoutVehicle() "
                            + "for vehicle that is not schedule based. {}",
                vehicleStatus);
            return null;
        }
        Block block = vehicleStatus.getBlock();
        if (block == null) {
            logger.error(
                    "Called SchedBasedPredictionsModule.shouldTimeoutVehicle() "
                            + "for vehicle that does not have a block assigned. {}",
                vehicleStatus);
            return null;
        }

        // If block not active anymore then must have reached end of block then
        // should remove the schedule based vehicle
        if (!block.isActive(dbConfig, now, applicationProperties.getPrediction().getBeforeStartTimeMinutes() * Time.SEC_PER_MIN)) {
            return "Schedule based predictions to be "
                    + "removed for block "
                    + vehicleStatus.getBlock().getId()
                    + " because the block is no longer active."
                    + " Block start time is "
                    + Time.timeOfDayShortStr(block.getStartTime())
                    + " and block end time is "
                    + Time.timeOfDayShortStr(block.getEndTime());
        }

        // If block is active but it is beyond the allowable number of minutes past the
        // the block start time then should remove the schedule based vehicle
        if (applicationProperties.getPrediction().getAfterStartTimeMinutes() >= 0) {
            long scheduledDepartureTime = vehicleStatus.getMatch().getScheduledWaitStopTime(dbConfig.getTime());
            if (scheduledDepartureTime >= 0) {
                // There is a scheduled departure time. Make sure not too
                // far past it
                long maxNoAvl = applicationProperties.getPrediction().getAfterStartTimeMinutes() * Time.MS_PER_MIN;
                if (now > scheduledDepartureTime + maxNoAvl) {
                    String shouldTimeoutEventDescription = "Schedule based predictions removed for block "
                            + vehicleStatus.getBlock().getId()
                            + " because it is now "
                            + Time.elapsedTimeStr(now - scheduledDepartureTime)
                            + " since the scheduled start time for the block"
                            + Time.dateTimeStr(scheduledDepartureTime)
                            + " while allowable time without an AVL report is "
                            + Time.elapsedTimeStr(maxNoAvl)
                            + ".";
                    if (!applicationProperties.getPrediction().getCancelTripOnTimeout()) {
                        return shouldTimeoutEventDescription;
                    } else if (!vehicleStatus.isCanceled()
                            && applicationProperties.getPrediction().getCancelTripOnTimeout()) // TODO: Check if it works on state changed
                    {
                        logger.info("Canceling trip...");
                        vehicleStatus.setCanceled(true);
                        vehicleDataCache.updateVehicle(vehicleStatus);
                        AvlReport avlReport = vehicleStatus.getAvlReport();
                        avlProcessor.processAvlReport(avlReport);
                    }
                }
            }
        }

        // Vehicle doesn't need to be timed out
        return null;
    }


    /**
     * It is a wait stop which means that vehicle can be stopped and turned off for a while such
     * that don't expect to get any AVL reports. Only timeout if past more that the allowed time for
     * wait stops
     */
    private void handleWaitStopPossibleTimeout(VehicleStatus vehicleStatus,
                                               long now,
                                               Iterator<AvlReport> mapIterator) {

        // we can't easily determine wait stop time for frequency based trips
        // so don't timeout based on stop info
        if (vehicleStatus.getBlock().isNoSchedule()) {
            logger.debug("not timing out frequency based assignment {}", vehicleStatus);
            return;
        }

        // If hasn't been too long between AVL reports then everything is fine
        // and simply return
        long maxNoAvl = timeoutConfig.getAllowableNoAvlSecs() * Time.MS_PER_SEC;
        if (now < vehicleStatus.getAvlReport().getTime() + maxNoAvl) return;

        // It has been a long time since an AVL report so see if also past the
        // scheduled time for the wait stop
        long scheduledDepartureTime = vehicleStatus.getMatch().getScheduledWaitStopTime(dbConfig.getTime());
        if (scheduledDepartureTime >= 0) {
            // There is a scheduled departure time. Make sure not too
            // far past it
            long maxNoAvlAfterSchedDepartSecs = timeoutConfig.getAllowableNoAvlAfterSchedDepartSecs() * Time.MS_PER_SEC;
            if (now > scheduledDepartureTime + maxNoAvlAfterSchedDepartSecs) {
                // Make vehicle unpredictable
                String stopId = "none (vehicle not matched)";
                if (vehicleStatus.getMatch() != null) {
                    if (vehicleStatus.getMatch().getAtEndStop() != null) {
                        stopId = vehicleStatus.getMatch().getAtStop().getStopId();
                    }
                }
                String eventDescription = "Vehicle timed out because it "
                        + "has not reported AVL location in "
                        + Time.elapsedTimeStr(now - vehicleStatus.getAvlReport().getTime())
                        + " and it is "
                        + Time.elapsedTimeStr(now - scheduledDepartureTime)
                        + " since the scheduled departure time "
                        + Time.dateTimeStr(scheduledDepartureTime)
                        + " for the wait stop ID "
                        + stopId
                        + " while allowable time without an AVL report is "
                        + Time.elapsedTimeStr(maxNoAvl)
                        + " and maximum allowed time after scheduled departure "
                        + "time without AVL is "
                        + Time.elapsedTimeStr(maxNoAvlAfterSchedDepartSecs)
                        + ". Therefore vehicle was made unpredictable.";
                avlProcessor
                        .makeVehicleUnpredictable(vehicleStatus.getVehicleId(), eventDescription, VehicleEvent.TIMEOUT);

                // Also log the situation
                logger.info("For vehicleId={} {}", vehicleStatus.getVehicleId(), eventDescription);

                // Remove vehicle from map for next time looking for timeouts
                mapIterator.remove();

                // Remove vehicle from cache if configured to do so
                removeFromVehicleDataCache(vehicleStatus.getVehicleId());
            }
        }
    }

    public void handlePossibleTimeouts() {
        // Determine what now is. Don't use System.currentTimeMillis() since
        // that doesn't work for playback.
        long now = SystemTime.getMillis();

        // Using an Iterator instead of for(AvlReport a : map.values())
        // because removing elements while iterating. Way to do this without
        // getting concurrent access exception is to use an Iterator.
        Iterator<AvlReport> mapIterator = avlReportRegistry.avlReportList().iterator();
        while (mapIterator.hasNext()) {
            AvlReport avlReport = mapIterator.next();

            // Get state of vehicle and handle based on it
            VehicleStatus vehicleStatus = vehicleStatusManager.getStatus(avlReport.getVehicleId());

            // Need to synchronize on vehicleState since it might be getting
            // modified via a separate main AVL processing executor thread.
            synchronized (vehicleStatus) {
                if (!vehicleStatus.isPredictable()) {
                    // Vehicle is not predictable
                    handleNotPredictablePossibleTimeout(vehicleStatus, now, mapIterator);
                } else if (vehicleStatus.isForSchedBasedPreds()) {
                    // Handle schedule based predictions vehicle
                    handleSchedBasedPredsPossibleTimeout(vehicleStatus, now, mapIterator);
                } else if (vehicleStatus.isWaitStop()) {
                    // Handle where vehicle is at a wait stop
                    handleWaitStopPossibleTimeout(vehicleStatus, now, mapIterator);
                } else {
                    // Not a special case. Simply determine if vehicle
                    // timed out
                    handlePredictablePossibleTimeout(vehicleStatus, now, mapIterator);
                }
            }
        }
    }

    @Scheduled(fixedRateString = "${transitclock.timeout.pollingRateSecs:30}",
            timeUnit = TimeUnit.SECONDS,
            initialDelayString = "${transitclock.timeout.pollingRateSecs:30}")
    public void run() {
        handlePossibleTimeouts();
    }
}
