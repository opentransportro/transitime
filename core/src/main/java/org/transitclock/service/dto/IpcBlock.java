/* (C)2023 */
package org.transitclock.service.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.transitclock.SingletonContainer;
import org.transitclock.domain.structs.Block;
import org.transitclock.domain.structs.Route;
import org.transitclock.domain.structs.Trip;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.utils.Time;

/**
 * Configuration information for a Block for IPC.
 *
 * @author SkiBu Smith
 */
public class IpcBlock implements Serializable {

    private final int configRev;
    private final String id;
    private final String serviceId;
    private final int startTime; // In seconds from midnight
    private final int endTime; // In seconds from midnight

    private final List<IpcTrip> trips;
    private final List<IpcRouteSummary> routeSummaries;

    public IpcBlock(Block dbBlock) {
        configRev = dbBlock.getConfigRev();
        id = dbBlock.getId();
        serviceId = dbBlock.getServiceId();
        startTime = dbBlock.getStartTime();
        endTime = dbBlock.getEndTime();

        trips = new ArrayList<IpcTrip>();
        for (Trip dbTrip : dbBlock.getTrips()) {
            trips.add(new IpcTrip(dbTrip));
        }

        routeSummaries = new ArrayList<IpcRouteSummary>();
        for (String routeId : dbBlock.getRouteIds()) {
            Route dbRoute = SingletonContainer.getInstance(DbConfig.class).getRouteById(routeId);
            routeSummaries.add(new IpcRouteSummary(dbRoute));
        }
    }

    @Override
    public String toString() {
        return "IpcBlock ["
                + "configRev="
                + configRev
                + ", id="
                + id
                + ", serviceId="
                + serviceId
                + ", startTime="
                + Time.timeOfDayStr(startTime)
                + ", endTime="
                + Time.timeOfDayStr(endTime)
                + ", trips="
                + trips
                + ", routeSummaries="
                + routeSummaries
                + "]";
    }

    public int getConfigRev() {
        return configRev;
    }

    public String getId() {
        return id;
    }

    public String getServiceId() {
        return serviceId;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public List<IpcTrip> getTrips() {
        return trips;
    }

    public List<IpcRouteSummary> getRouteSummaries() {
        return routeSummaries;
    }
}
