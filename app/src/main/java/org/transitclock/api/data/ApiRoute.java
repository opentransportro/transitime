/* (C)2023 */
package org.transitclock.api.data;

import org.transitclock.service.dto.IpcRouteSummary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * A short description of a route. For when outputting list of routes for agency.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiRoute {

    @JsonProperty
    private String id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String shortName;

    @JsonProperty
    private String longName;

    @JsonProperty
    private String type;


    public ApiRoute(IpcRouteSummary route) {
        this.id = route.getId();
        this.name = route.getName();
        this.shortName = route.getShortName();
        this.longName = route.getLongName();
        this.type = route.getType();
    }
}
