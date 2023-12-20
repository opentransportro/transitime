/* (C)2023 */
package org.transitclock.ipc.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.transitclock.applications.Core;
import org.transitclock.core.domain.Block;
import org.transitclock.core.domain.Route;
import org.transitclock.core.domain.Trip;
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

    private static final long serialVersionUID = 5707936828040534137L;

    /********************** Member Functions **************************/
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
            Route dbRoute = Core.getInstance().getDbConfig().getRouteById(routeId);
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
