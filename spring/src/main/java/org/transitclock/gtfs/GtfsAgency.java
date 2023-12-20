/* (C)2023 */
package org.transitclock.gtfs;

import lombok.Getter;
import lombok.ToString;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.utils.csv.CsvBase;

/**
 * @author SkiBu Smith
 */
@ToString
@Getter
public class GtfsAgency extends CsvBase {

    private final String agencyId;
    private final String agencyName;
    private final String agencyUrl;
    /**
     * -- GETTER --
     *  Valid timezone format is at http://en.wikipedia.org/wiki/List_of_tz_zones
     *
     * @return
     */
    // Valid timezone format is at http://en.wikipedia.org/wiki/List_of_tz_zones
    private final String agencyTimezone;
    private final String agencyLang;
    private final String agencyPhone;
    private final String agencyFareUrl;

    /**
     * @param record
     * @param supplemental
     * @param fileName for logging errors
     */
    public GtfsAgency(CSVRecord record, boolean supplemental, String fileName) {
        super(record, supplemental, fileName);

        agencyId = getOptionalValue(record, "agency_id");
        agencyName = getRequiredUnlessSupplementalValue(record, "agency_name");
        agencyUrl = getRequiredUnlessSupplementalValue(record, "agency_url");
        agencyTimezone = getRequiredUnlessSupplementalValue(record, "agency_timezone");
        agencyLang = getOptionalValue(record, "agency_lang");
        agencyPhone = getOptionalValue(record, "agency_phone");
        agencyFareUrl = getOptionalValue(record, "agency_fare_url");
    }

    /**
     * When combining a regular agency with a supplemental agency need to create a whole new object
     * since this class is Immutable to make it safer to use.
     */
    public GtfsAgency(GtfsAgency o, GtfsAgency s) {
        super(o);

        // Use short variable names
        agencyId = o.agencyId;
        agencyName = s.agencyName == null ? o.agencyName : s.agencyName;
        agencyUrl = s.agencyUrl == null ? o.agencyUrl : s.agencyUrl;
        agencyTimezone = s.agencyTimezone == null ? o.agencyTimezone : s.agencyTimezone;
        agencyLang = s.agencyLang == null ? o.agencyLang : s.agencyLang;
        agencyPhone = s.agencyPhone == null ? o.agencyPhone : s.agencyPhone;
        agencyFareUrl = s.agencyFareUrl == null ? o.agencyFareUrl : s.agencyFareUrl;
    }

}
