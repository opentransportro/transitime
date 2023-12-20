/* (C)2023 */
package org.transitclock.core.domain;

import lombok.Data;
import lombok.experimental.Delegate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.transitclock.gtfs.GtfsStop;
import org.transitclock.gtfs.TitleFormatter;

import java.io.Serializable;

/**
 * For storing in db information on a stop. Based on GTFS info from stops.txt file.
 *
 * @author SkiBu Smith
 */
@Data
@Document(collection = "Stops")
public class Stop implements Serializable {

    @Data
    public static class Key {
        private final int configRev;
        private final String id;
    }

    @Id
    @Delegate
    private final Key key;

    // The stop code used for SMS and phone systems
    private final Integer code;

    // Name of the stop
    private final String name;

    // Latitude/longitude of stop
    private final Location loc;

    // If should generate special ScheduleAdherence data for this stop
    private final boolean timepointStop;

    // Indicates that vehicle can leave route path before departing this stop
    // since the driver is taking a break.
    private final Boolean layoverStop;

    // Indicates that vehicle is not supposed to depart the stop until the
    // scheduled departure time.
    private final Boolean waitStop;

    // Indicates if stop should be hidden from public
    private final boolean hidden;

    /**
     * Constructor
     *
     * @param configRev
     * @param gtfsStop
     * @param stopCodeBaseValue For when stop code not specified in GTFS. If this value is set but
     *     stop code not configured then will sets the stop code to the stop ID plus the this
     *     stopCodeBaseValue.
     * @param titleFormatter
     */
    public Stop(int configRev, GtfsStop gtfsStop, Integer stopCodeBaseValue, TitleFormatter titleFormatter) {
        // Because will be writing data to sandbox rev in the db
        this.key = new Key(configRev, gtfsStop.getStopId());

        // Some agencies like SFMTA don't bother to fill in the stop_code field
        // in the GTFS data. But if they use a numeric stopId can use that.
        Integer stopCode = gtfsStop.getStopCode();
        if (stopCode == null) {
            // stop_code was not set in GTFS data so try using stop_id
            try {
                stopCode = Integer.parseInt(key.id);
                if (stopCodeBaseValue != null) stopCode += stopCodeBaseValue;
            } catch (NumberFormatException e) {
                // Well, we tried using the stopId but it was not numeric.
                // Therefore, the stopCode will simply be null.
            }
        }
        this.code = stopCode;

        this.name = titleFormatter.processTitle(gtfsStop.getStopName());
        this.loc = new Location(gtfsStop.getStopLat(), gtfsStop.getStopLon());
        // If adherence_stop not set then the default is false
        this.timepointStop = (gtfsStop.getTimepointStop() != null ? gtfsStop.getTimepointStop() : false);
        // If layover_stop not set then the default is false
        this.layoverStop = gtfsStop.getLayoverStop();
        // If wait_stop not set then the default is false
        this.waitStop = gtfsStop.getWaitStop();
        // If hidden not set then the default is false
        this.hidden = (gtfsStop.getHidden() != null ? gtfsStop.getHidden() : false);
    }
}
