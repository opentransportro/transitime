/* (C)2023 */
package org.transitclock.core.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps track of travel times for a trip. Can be shared amongst trips if the travel times are
 * similar. But need separate travel times for every trip pattern.
 *
 * @author SkiBu Smith
 */
@Getter
@EqualsAndHashCode
@ToString
@Document(collection = "TravelTimesForTrips")
//@Table(
//        name = "TravelTimesForTrips",
//        indexes = {@Index(name = "TravelTimesRevIndex", columnList = "travelTimesRev")})
public class TravelTimesForTrip implements Serializable {

    // Need a generated ID because trying to share TravelTimesForStopPath
    // objects because having a separate set for each trip would be too much.
    // But can still have a few per path and trip pattern. Therefore
    // also need the generated ID since the other columns are not adequate
    // as an ID.
    @Id
    private Integer id;

    // Need configRev for the configuration so that when old configurations
    // cleaned out can also easily get rid of old travel times. Note: at one
    // point tried making configRevan @Id so that the config rev is part of
    // the join table so that it would be easier to delete old config data
    // from the join table. But this cause the id member to not be declared
    // as auto_increment in the SQL for creating the table, which in turn
    // caused a strange PropertyAccessException when trying to save travel
    // times. Therefore cannot make this member an @Id and have to use fancy
    // delete with a join to clear out the join table of old data.
    private final int configRev;

    // Each time update travel times it gets a new travel time rev. This
    // way can compare travel times with previous revisions. Probably only need
    // to keep the previous travel time rev around for comparison but by
    // using an integer for the rev all of the revs can be kept in the db
    // if desired.
    private final int travelTimesRev;

    private final String tripPatternId;

    // So know which trip these travel times were created for. Useful
    // for logging statements. Used when creating travel times based
    // on schedule.
    private final String tripCreatedForId;

    @DocumentReference(sort = "listIndex")
    private final List<TravelTimesForStopPath> travelTimesForStopPaths = new ArrayList<>();



    /**
     * Simple constructor.
     *
     * @param configRev
     * @param travelTimesRev
     * @param trip So can determine trip pattern ID and set which trip this was created for.
     */
    public TravelTimesForTrip(int configRev, int travelTimesRev, Trip trip) {
        this.configRev = configRev;
        this.travelTimesRev = travelTimesRev;
        this.tripPatternId = trip.getTripPattern().getKey().getId();
        this.tripCreatedForId = trip.getId();
    }


    /**
     * For when creating a new TravelTimesForTrip. This method is for adding each
     * TravelTimesForStopPath.
     *
     * @param travelTimesForPath
     */
    public void add(TravelTimesForStopPath travelTimesForPath) {
        travelTimesForStopPaths.add(travelTimesForPath);
    }


    /**
     * Returns true if every single stop path travel time is schedule based.
     */
    public boolean purelyScheduleBased() {
        for (TravelTimesForStopPath times : travelTimesForStopPaths) {
            if (!times.getHowSet().isScheduleBased()) {
                return false;
            }
        }

        // All of them travel times are schedule based so return true
        return true;
    }

    /** Returns true if all stop paths are valid. */
    public boolean isValid() {
        for (TravelTimesForStopPath times : travelTimesForStopPaths) {
            if (!times.isValid()) {
                return false;
            }
        }

        return true;
    }

    /**
     * For output list of travel times for stop paths. Uses newlines to put each one on separate
     * line so that easier to read.
     */
    private static String travelTimesToStringWithNewlines(List<TravelTimesForStopPath> travelTimesForStopPaths) {
        StringBuilder results = new StringBuilder();
        for (TravelTimesForStopPath travelTimesForSP : travelTimesForStopPaths) {
            results
                    .append("     ")
                    .append(travelTimesForSP.toStringEmphasizeTravelTimes())
                    .append("\n");
        }
        return results.toString();
    }

    /**
     * Similar to toString() but puts each travelTimesForStopPath on a separate line to try to make
     * the output more readable.
     *
     * @return
     */
    public String toStringWithNewlines() {
        return "TravelTimesForTrip ["
                + "configRev="
                + configRev
                + ", travelTimesRev="
                + travelTimesRev
                + ", tripPatternId="
                + tripPatternId
                + ", tripCreatedForId="
                + tripCreatedForId
                + ", travelTimesForStopPaths=\n"
                + travelTimesToStringWithNewlines(travelTimesForStopPaths)
                + "]";
    }

    public TravelTimesForStopPath getTravelTimesForStopPath(int index) {
        return travelTimesForStopPaths.get(index);
    }

    /**
     * @return Number of stopPaths in trip
     */
    public int numberOfStopPaths() {
        return travelTimesForStopPaths.size();
    }
}
