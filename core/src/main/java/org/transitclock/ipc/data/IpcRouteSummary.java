/* (C)2023 */
package org.transitclock.ipc.data;

import java.io.Serializable;
import org.transitclock.db.structs.Extent;
import org.transitclock.db.structs.Route;

/**
 * Contains configuration information for a single route. For providing info to client. This class
 * is Immutable so that it is threadsafe.
 *
 * @author SkiBu Smith
 */
public class IpcRouteSummary implements Serializable {

    protected final String id;
    protected final String name;
    protected final String shortName;
    protected final String longName;
    protected final Extent extent;
    protected final String type;
    protected final String color;
    protected final String textColor;

    /**
     * Constructs a new RouteSummary object using a Route object from the database. Used by the
     * server to create an object to be transmitted via RMI.
     *
     * @param dbRoute
     */
    public IpcRouteSummary(Route dbRoute) {
        this.id = dbRoute.getId();
        this.name = dbRoute.getName();
        this.shortName = dbRoute.getShortName();
        this.longName = dbRoute.getLongName();
        this.extent = dbRoute.getExtent();
        this.type = dbRoute.getType();
        this.color = dbRoute.getColor();
        this.textColor = dbRoute.getTextColor();
    }

    /**
     * For need to clone a IpcRouteSummary but with a new route name.
     *
     * @param toCopy
     * @param newRouteName
     */
    public IpcRouteSummary(IpcRouteSummary toCopy, String newRouteName) {
        this.id = toCopy.getId();
        this.name = newRouteName;
        this.shortName = toCopy.getShortName();
        this.longName = toCopy.getLongName();
        this.extent = toCopy.getExtent();
        this.type = toCopy.getType();
        this.color = toCopy.getColor();
        this.textColor = toCopy.getTextColor();
    }

    /**
     * @return the GTFS route_id.
     */
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }

    public Extent getExtent() {
        return extent;
    }

    public String getType() {
        return type;
    }

    public String getColor() {
        return color;
    }

    public String getTextColor() {
        return textColor;
    }

    @Override
    public String toString() {
        return "IpcRouteSummary ["
                + "id="
                + id
                + ", name="
                + name
                + ", shortName="
                + shortName
                + ", longName="
                + longName
                + ", extent="
                + extent
                + ", type="
                + type
                + ", color="
                + color
                + ", textColor="
                + textColor
                + "]";
    }
}
