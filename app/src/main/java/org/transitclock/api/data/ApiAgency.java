/* (C)2023 */
package org.transitclock.api.data;

import java.util.TimeZone;

import org.transitclock.domain.structs.Agency;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Contains API info for an agency.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiAgency {

    @JsonProperty
    private String agencyId;

    // Note that this is the GTFS agency_id, which is often different
    // from the Transitime agencyId.
    @JsonProperty
    private String id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String url;

    @JsonProperty
    private String timezone;

    @JsonProperty
    private int timezoneOffsetMinutes;

    @JsonProperty
    private String lang;

    @JsonProperty
    private String phone;

    @JsonProperty
    private String fareUrl;

    @JsonProperty
    private ApiExtent extent;

    @JsonProperty
    private int configRev;

    public ApiAgency(String agencyId, Agency agency) {
        this.agencyId = agencyId;
        this.id = agency.getId();
        this.name = agency.getName();
        this.url = agency.getUrl();
        this.timezone = agency.getTimeZoneStr();
        this.lang = agency.getLang();
        this.phone = agency.getPhone();
        this.fareUrl = agency.getFareUrl();
        this.extent = new ApiExtent(agency.getExtent());
        this.configRev = agency.getConfigRev();

        // Return timezone offset in minutes since that is what Javascript uses.
        // Need to negate so it works with Javascript Date().getTimezoneOffset().
        TimeZone timezone = TimeZone.getTimeZone(this.timezone);
        this.timezoneOffsetMinutes = -timezone.getOffset(System.currentTimeMillis()) / (60 * 1000);
    }
}
