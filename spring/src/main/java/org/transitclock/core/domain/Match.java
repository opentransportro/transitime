/* (C)2023 */
package org.transitclock.core.domain;

import lombok.Data;
import lombok.experimental.Delegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.transitclock.applications.Core;
import org.transitclock.core.TemporalMatch;

import java.io.Serializable;
import java.util.Date;

/**
 * For persisting the match for the vehicle. This data is later used for determining expected travel
 * times. The key/IDs for the table are vehicleId and the AVL avlTime so that the Match data can
 * easily be joined with AvlReport data to get additional information.
 *
 * <p>Serializable since Hibernate requires such.
 *
 * <p>Implements Lifecycle so that can have the onLoad() callback be called when reading in data so
 * that can intern() member strings. In order to do this the String members could not be declared as
 * final since they are updated after the constructor is called.
 *
 * @author SkiBu Smith
 */
@Data
@Document(collection = "Matches")
public class Match implements Serializable {
    @Data
    public static class Key {
        // So that know which configuration was being used when this data point
        // was created
        private final int configRev;

        // vehicleId is an @Id since might get multiple AVL reports
        // for different vehicles with the same avlTime but need a unique
        // primary key.
        private String vehicleId;

        // Need to use columnDefinition to explicitly specify that should use
        // fractional seconds. This column is an Id since shouldn't get two
        // AVL reports for the same vehicle for the same avlTime.
        @Indexed(name = "AvlTimeIndex")
        private final Date avlTime;
    }
    @Id
    @Delegate
    private final Key key;

    // So that know which service type was used when this data point was created
    private String serviceId;

    // Not truly needed because currently using only trip info for generating
    // travel times, which is the main use of Match data from the db.
    private String blockId;

    // Creating travel times on a trip by trip basis so this element is
    // important.
    private String tripId;

    // Important because generating travel times on a per stop path basis
    private final int stopPathIndex;

    // Not currently needed. Added for possible future uses of Match
    private final int segmentIndex;

    // Not currently needed. Added for possible future uses of Match
    private final float distanceAlongSegment;

    // The distanceAlongStopPath is the important item since travel times are
    // based on dividing up the stop path into travel time paths. These travel
    // time paths are independent of the path segments.
    private final float distanceAlongStopPath;

    // Whether vehicle is considered to be at a stop. Especially useful so
    // can filter out atStop matches when determining travel times since
    // instead using arrival/departure times for that situation.
    private final boolean atStop;

    private static final Logger logger = LoggerFactory.getLogger(Match.class);

    /**
     * Simple constructor
     *
     * @param vehicleState
     */
    public Match(VehicleState vehicleState) {
        this.vehicleId = vehicleState.getVehicleId();
        this.avlTime = vehicleState.getAvlReport().getDate();
        this.configRev = Core.getInstance().getDbConfig().getConfigRev();
        this.serviceId = vehicleState.getBlock().getServiceId();
        this.blockId = vehicleState.getBlock().getId();

        TemporalMatch lastMatch = vehicleState.getMatch();
        this.tripId = lastMatch != null ? lastMatch.getTrip().getId() : null;
        this.stopPathIndex = lastMatch != null ? lastMatch.getStopPathIndex() : -1;
        this.segmentIndex = lastMatch != null ? lastMatch.getSegmentIndex() : -1;
        this.distanceAlongSegment = (float) (lastMatch != null ? lastMatch.getDistanceAlongSegment() : 0.0);
        this.distanceAlongStopPath = (float) (lastMatch != null ? lastMatch.getDistanceAlongStopPath() : 0.0);
        this.atStop = vehicleState.getMatch().isAtStop();

        // Log each creation of a Match to the match.log log file
        logger.info(this.toString());
    }

    public Date getDate() {
        return getAvlTime();
    }

    public long getTime() {
        return getAvlTime().getTime();
    }

}
