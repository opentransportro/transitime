/* (C)2023 */
package org.transitclock.service.dto;

import java.io.Serializable;
import org.transitclock.db.structs.Location;
import org.transitclock.db.structs.Stop;

/**
 * Contains information for a single route. Since Stop objects do not change dynamically this class
 * is not made immutable via a serialization proxy. This simplifies the class greatly.
 *
 * @author SkiBu Smith
 */
public class IpcStop implements Serializable {

    private final String id;
    private final String name;
    private final Integer code;
    private final Location loc;
    private final boolean isUiStop;
    private final String directionId;
    private Double stopPathLength;

    public Double getStopPathLength() {
        return stopPathLength;
    }

    public void setStopPathLength(Double stopPathLength) {
        this.stopPathLength = stopPathLength;
    }

    public IpcStop(Stop dbStop, boolean aUiStop, String directionId, Double stopPathLength) {
        this.id = dbStop.getId();
        this.name = dbStop.getName();
        this.code = dbStop.getCode();
        this.loc = dbStop.getLoc();
        this.isUiStop = aUiStop;
        this.directionId = directionId;
        this.stopPathLength = stopPathLength;
    }

    /**
     * Constructs a stop and sets isUiStop to true.
     *
     * @param dbStop
     */
    public IpcStop(Stop dbStop, String directionId) {
        this.id = dbStop.getId();
        this.name = dbStop.getName();
        this.code = dbStop.getCode();
        this.loc = dbStop.getLoc();
        this.isUiStop = true;
        this.directionId = directionId;
    }

    @Override
    public String toString() {
        return "IpcStop ["
                + "id="
                + id
                + ", name="
                + name
                + ", code="
                + code
                + ", loc="
                + loc
                + ", isUiStop="
                + isUiStop
                + ", directionId"
                + directionId
                + "]";
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getCode() {
        return code;
    }

    public Location getLoc() {
        return loc;
    }

    public boolean isUiStop() {
        return isUiStop;
    }

    public String getDirectionId() {
        return directionId;
    }
}
