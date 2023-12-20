/* (C)2023 */
package org.transitclock.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.transitclock.utils.Time;
import org.transitclock.gtfs.GtfsAgency;

import java.io.Serializable;
import java.util.List;
import java.util.TimeZone;

/**
 * Contains data from the agency.txt GTFS file. This class is for reading/writing that data to the
 * db.
 *
 * @author SkiBu Smith
 */
@ToString
@EqualsAndHashCode
@Document(collection = "agencies")
public class Agency implements Serializable {

    @Data
    public static final class Key {
        private final int configRev;
        private final String agencyName;
    }

    @Id
    @Delegate
    private final Key key;

    private final String agencyId;

    private final String agencyUrl;

    // Note: agencyTimezone can be reasonable long. At least as long
    // as "America/Los_Angeles". Valid timezone format is at
    // http://en.wikipedia.org/wiki/List_of_tz_zones
    private final String agencyTimezone;

    private final String agencyLang;

    private final String agencyPhone;

    private final String agencyFareUrl;

    private final Extent extent;

    @Transient
    private TimeZone timezone = null;

    @Transient
    private Time time = null;

    public Agency(int configRev, GtfsAgency gtfsAgency, List<Route> routes) {
        this.key = new Key(configRev, gtfsAgency.getAgencyName());
        this.agencyId = gtfsAgency.getAgencyId();
        this.agencyUrl = gtfsAgency.getAgencyUrl();
        this.agencyTimezone = gtfsAgency.getAgencyTimezone();
        this.agencyLang = gtfsAgency.getAgencyLang();
        this.agencyPhone = gtfsAgency.getAgencyPhone();
        this.agencyFareUrl = gtfsAgency.getAgencyFareUrl();

        Extent extent = new Extent();
        for (Route route : routes) {
            extent.add(route.getExtent());
        }
        this.extent = extent;
    }

    /**
     * Returns cached TimeZone object for agency. Useful for creating Calendar objects and such.
     *
     * @return The TimeZone object for this agency
     */
    public TimeZone getTimeZone() {
        if (timezone == null) timezone = TimeZone.getTimeZone(agencyTimezone);
        return timezone;
    }

    /**
     * Returns cached Time object which allows one to easly convert epoch time to time of day and
     * such.
     *
     * @return Time object
     */
    public Time getTime() {
        if (time == null) time = new Time(agencyTimezone);
        return time;
    }


    public int getConfigRev() {
        return key.getConfigRev();
    }

    /**
     * Note that this method returns the GTFS agency_id which is usually different from the
     * Transitime agencyId
     *
     * @return the agencyId
     */
    public String getId() {
        return agencyId;
    }

    public String getName() {
        return key.getAgencyName();
    }

    public String getUrl() {
        return agencyUrl;
    }

    public String getTimeZoneStr() {
        return agencyTimezone;
    }

    public String getLang() {
        return agencyLang;
    }

    public String getPhone() {
        return agencyPhone;
    }

    public String getFareUrl() {
        return agencyFareUrl;
    }

    public Extent getExtent() {
        return extent;
    }
}
