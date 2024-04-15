/* (C)2023 */
package org.transitclock.service.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.transitclock.domain.structs.ScheduleTime;
import org.transitclock.domain.structs.StopPath;
import org.transitclock.domain.structs.Trip;
import org.transitclock.gtfs.DbConfig;

/**
 * For describing a trip as part of a schedule
 *
 * @author SkiBu Smith
 */
public class IpcSchedTrip implements Serializable {

    private final String blockId;
    private final String tripId;
    private final String tripShortName;
    private final String tripHeadsign;
    private final List<IpcSchedTime> scheduleTimes;

    /**
     * Constructor. Goes through the complete ordered list of stops for the direction and creates
     * the corresponding IpcSchedTime objects for each one, even if there is no scheduled time for
     * that stop for the specified trip.
     */
    public IpcSchedTrip(Trip trip, DbConfig dbConfig) {
        this.blockId = trip.getBlockId();
        this.tripId = trip.getId();
        this.tripShortName = trip.getShortName();
        this.tripHeadsign = trip.getHeadsign();
        this.scheduleTimes = new ArrayList<>();

        // Actually fill in the schedule times
        // First, get list of ordered stop IDs for the direction
        List<String> orderedStopIds =
                trip.getRoute(dbConfig).getOrderedStopsByDirection(dbConfig).get(trip.getDirectionId());

        // Set the schedule times for each ordered stop for the route/direction.
        // If no scheduled time for the stop then use a null time.
        int currentStopIdxInOrderedStops = 0;
        for (int stopIdxInTrip = 0; stopIdxInTrip < trip.getNumberStopPaths(); ++stopIdxInTrip) {
            StopPath stopPathInTrip = trip.getStopPath(stopIdxInTrip);
            String stopIdInTrip = stopPathInTrip.getStopId();

            // Find the stop from the schedules for the trip in the list of
            // ordered stops...
            int stopIdxInOrderedStops;
            for (stopIdxInOrderedStops = currentStopIdxInOrderedStops;
                    stopIdxInOrderedStops < orderedStopIds.size();
                    ++stopIdxInOrderedStops) {
                // If found the schedule time in the trip then use it
                if (orderedStopIds.get(stopIdxInOrderedStops).equals(stopIdInTrip)) {
                    // Add a null schedule times needed for when there is an ordered
                    // stop but no corresponding schedule time
                    for (int i = currentStopIdxInOrderedStops; i < stopIdxInOrderedStops; ++i) {
                        // Create a IpcScheduleTime with a time of null so can still
                        // be added to the schedule trip.
                        String stopId = orderedStopIds.get(i);
                        addNullScheduleTime(stopId, dbConfig);
                    }

                    // Update currentStopIdxInOrderedStops since have dealt with up to
                    // this stop
                    currentStopIdxInOrderedStops = stopIdxInOrderedStops + 1;

                    ScheduleTime scheduleTime = trip.getScheduleTime(stopIdxInTrip);
                    String stopName = dbConfig.getStop(stopPathInTrip.getStopId()).getName();
                    IpcSchedTime ipcScheduleTime =
                            new IpcSchedTime(stopIdInTrip, stopName, scheduleTime.getTime());
                    scheduleTimes.add(ipcScheduleTime);

                    // Done with this stop in the trip so continue to the
                    // next one
                    break;
                }
            }
        }

        // For remaining ordered stops where there wasn't a schedule time add a
        // null schedule times
        for (int i = currentStopIdxInOrderedStops; i < orderedStopIds.size(); ++i) {
            // Create a IpcScheduleTime with a time of null so can still
            // be added to the schedule trip.
            String stopId = orderedStopIds.get(i);
            addNullScheduleTime(stopId, dbConfig);
        }

        // FIXME
        //		// Go through all the ordered stops for the route/direction and
        //		// create corresponding IpcScheduleTime
        //		int stopIdxInTrip = 0;
        //		for (String stopId : orderedStopIds) {
        //			// Find stop in schedule for trip that corresponds to the
        //			// current stop from the ordered stops by direction. If
        //			// no corresponding stop then ipcScheduleTime will be null.
        //			IpcSchedTime ipcScheduleTime = null;
        //			// Go through stops in trip to find corresponding one to get
        //			// schedule time
        //			while (stopIdxInTrip < trip.getNumberStopPaths()) {
        //				StopPath stopPathInTrip = trip.getStopPath(stopIdxInTrip);
        //				String stopIdInTrip = stopPathInTrip.getStopId();
        //
        //				// If found corresponding stop in schedule...
        //				if (stopId.equals(stopIdInTrip)) {
        //					// If corresponding stop in schedule actually has a time...
        //					ScheduleTime scheduleTime = trip
        //							.getScheduleTime(stopIdxInTrip);
        //					if (scheduleTime != null) {
        //						ipcScheduleTime = new IpcSchedTime(stopId,
        //								stopPathInTrip.getStopName(),
        //								scheduleTime.getTime());
        //					}
        //
        //					// Determined corresponding schedule time for stop so
        //					// continue on to next stop in the ordered stops for
        //					// direction for the route.
        //					++stopIdxInTrip;
        //					break;
        //				}
        //
        //				// Corresponding stop in trip not found so try next one
        //				++stopIdxInTrip;
        //			}
        //
        //			// If the stop from the ordered stops from the route/direction
        //			// didn't have a corresponding stop in the trip then still add
        //			// add a IpcScheduleTime to the trip, but with a null time.
        //			// This way all trips for a schedule will have the same stops.
        //			// Just some will have a null time.
        //			if (ipcScheduleTime == null) {
        //				// Create a IpcScheduleTime with a time of null so can still
        //				// be added to the schedule trip.
        //				String stopName = Core.getInstance().getDbConfig()
        //						.getStop(stopId).getName();
        //				ipcScheduleTime = new IpcSchedTime(stopId, stopName, null);
        //			}
        //
        //			// Add the (possibly null) schedule time
        //			scheduleTimes.add(ipcScheduleTime);
        //		}
    }

    private void addNullScheduleTime(String stopId, DbConfig dbConfig) {
        // Create a IpcScheduleTime with a time of null so can still
        // be added to the schedule trip.
        String stopName = dbConfig.getStop(stopId).getName();
        IpcSchedTime ipcScheduleTime = new IpcSchedTime(stopId, stopName, null);
        scheduleTimes.add(ipcScheduleTime);
    }

    @Override
    public String toString() {
        return "IpcScheduleTrip ["
                + "blockId="
                + blockId
                + ", tripId="
                + tripId
                + ", tripShortName="
                + tripShortName
                + ", scheduleTimes="
                + scheduleTimes
                + "]";
    }

    public String getBlockId() {
        return blockId;
    }

    public String getTripId() {
        return tripId;
    }

    public String getTripShortName() {
        return tripShortName;
    }

    public String getTripHeadsign() {
        return tripHeadsign;
    }

    public List<IpcSchedTime> getSchedTimes() {
        return scheduleTimes;
    }
}
