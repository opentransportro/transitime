/* (C)2023 */
package org.transitclock.ipc.data;

import org.transitclock.applications.Core;
import org.transitclock.core.domain.Location;
import org.transitclock.core.domain.Stop;
import org.transitclock.core.domain.StopPath;
import org.transitclock.utils.Geo;

import java.io.Serializable;
import java.util.List;

/**
 * Configuration information for a StopPath for IPC.
 *
 * @author SkiBu Smith
 */
public class IpcStopPath implements Serializable {

    private final int configRev;
    private final String stopPathId;
    private final String stopId;
    private final String stopName;
    private final int gtfsStopSeq;
    private final boolean layoverStop;
    private final boolean waitStop;
    private final boolean scheduleAdherenceStop;
    private final Integer breakTime;
    private List<Location> locations;
    private double pathLength;

    private static final long serialVersionUID = -3355335358825870977L;

    /********************** Member Functions **************************/
    public IpcStopPath(StopPath dbStopPath) {
        this.configRev = dbStopPath.getConfigRev();
        this.stopPathId = dbStopPath.getStopPathId();
        this.stopId = dbStopPath.getStopId();
        Stop stop = Core.getInstance().getDbConfig().getStop(dbStopPath.getStopId());
        this.stopName = stop.getName();
        this.gtfsStopSeq = dbStopPath.getGtfsStopSeq();
        this.layoverStop = dbStopPath.isLayoverStop();
        this.waitStop = dbStopPath.isWaitStop();
        this.scheduleAdherenceStop = dbStopPath.isScheduleAdherenceStop();
        this.breakTime = dbStopPath.getBreakTimeSec();
        this.locations = dbStopPath.getLocations();
        this.pathLength = dbStopPath.getLength();
    }

    @Override
    public String toString() {
        return "IpcStopPath ["
                + "configRev="
                + configRev
                + ", stopPathId="
                + stopPathId
                + ", stopId="
                + stopId
                + ", stopName="
                + stopName
                + ", gtfsStopSeq="
                + gtfsStopSeq
                + ", layoverStop="
                + layoverStop
                + ", waitStop="
                + waitStop
                + ", scheduleAdherenceStop="
                + scheduleAdherenceStop
                + ", breakTime="
                + breakTime
                + ", locations="
                + locations
                + ", pathLength="
                + Geo.distanceFormat(pathLength)
                + "]";
    }

    public int getConfigRev() {
        return configRev;
    }

    public String getStopPathId() {
        return stopPathId;
    }

    public String getStopId() {
        return stopId;
    }

    public String getStopName() {
        return stopName;
    }

    public int getGtfsStopSeq() {
        return gtfsStopSeq;
    }

    public boolean isLayoverStop() {
        return layoverStop;
    }

    public boolean isWaitStop() {
        return waitStop;
    }

    public boolean isScheduleAdherenceStop() {
        return scheduleAdherenceStop;
    }

    public Integer getBreakTime() {
        return breakTime;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public double getPathLength() {
        return pathLength;
    }
}
