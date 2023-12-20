/* (C)2023 */
package org.transitclock.core.domain;

import lombok.*;
import lombok.experimental.Delegate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.classic.Lifecycle;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.transitclock.applications.Core;
import org.transitclock.configData.AgencyConfig;
import org.transitclock.configData.DbSetupConfig;
import org.transitclock.core.TemporalDifference;
import org.transitclock.utils.Geo;
import org.transitclock.utils.Time;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.utils.IntervalTimer;

import java.io.Serializable;
import java.util.Date;

/**
 * For persisting an Arrival or a Departure time. Should use Arrival or Departure subclasses.
 *
 * <p>Implements Lifecycle so that can have the onLoad() callback be called when reading in data so
 * that can intern() member strings. In order to do this the String members could not be declared as
 * final since they are updated after the constructor is called. By interning the member strings
 * less than half (about 40%) of the RAM is used. This is very important when reading in large
 * batches of ArrivalDeparture objects!
 *
 * @author SkiBu Smith
 */
@Getter @Setter
@EqualsAndHashCode
@Document(collection = "ArrivalsDepartures")
//@Table(
//        name = "ArrivalsDepartures",
//        indexes = {
//            @Index(name = "ArrivalsDeparturesTimeIndex", columnList = "time"),
//            @Index(name = "ArrivalsDeparturesRouteTimeIndex", columnList = "routeShortName, time")
//        })
public abstract class ArrivalDeparture implements Serializable {

    @Data
    @Builder(setterPrefix = "with")
    public static class Key {

        private String vehicleId;

        // Originally did not use msec precision (datetime(3)) specification
        // because arrival/departure times are only estimates and having such
        // precision is not generally appropriate. But found that then some
        // arrival and departures for a stop would have the same time and when
        // one would query for the arrivals/departures and order by time one
        // could get a departure before an arrival. To avoid this kind of
        // incorrect ordering using the additional precision. And this way
        // don't have to add an entire second to a departure time to make
        // sure that it is after the arrival. Adding a second is an
        // exaggeration because it implies the vehicle was stopped for a second
        // when most likely it zoomed by the stop. It looks better to add
        // only a msec to make the departure after the arrival.
        private final Date time;

        private String stopId;

        // From the GTFS stop_times.txt file for the trip. The gtfsStopSeq can
        // be different from stopPathIndex. The stopIndex is included here so that
        // it is easy to find the corresponding stop in the stop_times.txt file.
        // It needs to be part of the @Id because can have loops for a route
        // such that a stop is served twice on a trip. Otherwise would get a
        // constraint violation.
        private final int gtfsStopSeq;

        private final boolean isArrival;

        private String tripId;
        // The revision of the configuration data that was being used
        private final int configRev;
    }

    @Id
    @Delegate
    private final Key key;

    // So can match the ArrivalDeparture time to the AvlReport that
    // generated it by using vehicleId and avlTime.
    private final Date avlTime;

    // The schedule time will only be set if the schedule info was available
    // from the GTFS data and it is the proper type of arrival or departure
    // stop (there is an arrival schedule time and this is the last stop for
    // a trip and and this is an arrival time OR there is a departure schedule
    // time and this is not the last stop for a trip and this is a departure
    // time. Otherwise will be null.
    private final Date scheduledTime;

    private String blockId;

    private String routeId;

    // routeShortName is included because for some agencies the
    // route_id changes when there are schedule updates. But the
    // routeShortName is more likely to stay consistent. Therefore
    // it is better for when querying for arrival/departure data
    // over a timespan.
    private String routeShortName;

    private String serviceId;

    private String directionId;

    // The index of which trip this is within the block.
    private final int tripIndex;

    /* this is required for frequenecy based services */
    private final Date freqStartTime;

    // The index of which stop path this is within the trip.
    // Different from the GTFS gtfsStopSeq. The stopPathIndex starts
    // at 0 and increments by one for every stop. The GTFS gtfsStopSeq
    // on the other hand doesn't need to be sequential.
    private final int stopPathIndex;

    // The order of the stop for the direction of the route. This can
    // be useful for displaying data in proper stop order. The member
    // stopPathIndex is for the current trip, but since a route's
    // direction can have multiple trip patterns the stopPathIndex
    // is not sufficient for properly ordering data for a route/direction.
    // Declared an Integer instead of an int because might not always
    // be set.
    private final Integer stopOrder;

    // Sometimes want to look at travel times using arrival/departure times.
    // This would be complicated if had to get the path length by using
    // tripIndex to determine trip to determine trip pattern to determine
    // StopPath to determine length. So simply storing the stop path
    // length along with arrivals/departures so that it is easy to obtain
    // for post-processing.
    private final float stopPathLength;

    // So can easily create copy constructor withUpdatedTime()
    @Transient
    private final Block block;

    // Needed because some methods need to know if dealing with arrivals or
    // departures.

    public enum ArrivalsOrDepartures {
        ARRIVALS,
        DEPARTURES
    };

    private static final Logger logger = LoggerFactory.getLogger(ArrivalDeparture.class);


    protected ArrivalDeparture(
            int configRev,
            String vehicleId,
            Date time,
            Date avlTime,
            Block block,
            int tripIndex,
            int stopPathIndex,
            boolean isArrival,
            Date freqStartTime) {
        var keyBuilder = Key.builder()
                .withVehicleId(vehicleId)
                .withTime(time)
                .withIsArrival(isArrival)
                .withConfigRev(configRev);

        this.avlTime = avlTime;
        this.block = block;
        this.tripIndex = tripIndex;
        this.stopPathIndex = stopPathIndex;
        this.freqStartTime = freqStartTime;

        // Some useful convenience variables

        if (block != null) {
            Trip trip = block.getTrip(tripIndex);
            StopPath stopPath = trip.getStopPath(stopPathIndex);
            String stopId = stopPath.getStopId();
            // Determine and store stop order
            this.stopOrder = trip.getRoute().getStopOrder(trip.getDirectionId(), stopId, stopPathIndex);

            // Determine the schedule time, which is a bit complicated.
            // Of course, only do this for schedule based assignments.
            // The schedule time will only be set if the schedule info was available
            // from the GTFS data and it is the proper type of arrival or departure
            // stop (there is an arrival schedule time and this is the last stop for
            // a trip and and this is an arrival time OR there is a departure schedule
            // time and this is not the last stop for a trip and this is a departure
            // time.
            Date scheduledEpochTime = null;
            if (!trip.isNoSchedule()) {
                ScheduleTime scheduleTime = trip.getScheduleTime(stopPathIndex);
                if (stopPath.isLastStopInTrip() && scheduleTime.getArrivalTime() != null && isArrival) {
                    long epochTime = Core.getInstance().getTime().getEpochTime(scheduleTime.getArrivalTime(), time);
                    scheduledEpochTime = new Date(epochTime);
                } else if (!stopPath.isLastStopInTrip() && scheduleTime.getDepartureTime() != null && !isArrival) {
                    long epochTime = Core.getInstance().getTime().getEpochTime(scheduleTime.getDepartureTime(), time);
                    scheduledEpochTime = new Date(epochTime);
                }
            }
            this.scheduledTime = scheduledEpochTime;

            this.blockId = block.getId();
            this.directionId = trip.getDirectionId();
            keyBuilder
                    .withTripId(trip.getId())
                    .withStopId(stopId)
                    .withGtfsStopSeq(stopPath.getGtfsStopSeq());
            this.stopPathLength = (float) stopPath.getLength();
            this.routeId = trip.getRouteId();
            this.routeShortName = trip.getRouteShortName();
            this.serviceId = block.getServiceId();
        } else {
            keyBuilder
                    .withTripId("")
                    .withStopId("")
                    .withGtfsStopSeq(0);
            this.stopPathLength = 0;
            this.scheduledTime = null;
            this.serviceId = "";
            this.stopOrder = 0;
        }
        this.key = keyBuilder.build();
    }

    @Override
    public String toString() {
        return (isArrival() ? "Arrival  " : "Departure")
                + " ["
                + "key="
                + ", route="
                + routeId
                + ", rteName="
                + routeShortName
                + ", directionId="
                + directionId
                + ", stopIdx="
                + stopPathIndex
                + ", freqStartTime="
                + freqStartTime
                + ", stopOrder="
                + stopOrder
                + ", avlTime="
                + Time.timeStrMsec(avlTime)
                + ", tripIdx="
                + tripIndex
                + ", block="
                + blockId
                + ", srv="
                + serviceId
                + ", pathLnth="
                + Geo.distanceFormat(stopPathLength)
                + (scheduledTime != null ? ", schedTime=" + Time.timeStr(scheduledTime) : "")
                + (scheduledTime != null
                        ? ", schedAdh=" + new TemporalDifference(scheduledTime.getTime() - key.time.getTime())
                        : "")
                + "]";
    }

    public Date getDate() {
        return key.time;
    }

    public long getTime() {
        return key.time.getTime();
    }

    public boolean isDeparture() {
        return !isArrival();
    }

    /**
     * Returns the trip short name for the trip associated with the arrival/departure.
     *
     * @return trip short name for the trip associated with the arrival/departure or null if there
     *     is a problem
     */
    public String getTripShortName() {
        if (!Core.isCoreApplication()) {
            logger.error(
                    "For agencyId={} alling ArrivalDeparture.getTripShortName() "
                            + "but it is not part of core application",
                    AgencyConfig.getAgencyId());
            return null;
        }

        Trip trip = Core.getInstance().getDbConfig().getTrip(tripId);
        if (trip != null) return trip.getShortName();
        else return null;
    }

    /**
     * The schedule time will only be set if the schedule info was available from the GTFS data and
     * it is the proper type of arrival or departure stop (there is an arrival schedule time and
     * this is the last stop for a trip and this is an arrival time OR there is a departure
     * schedule time and this is not the last stop for a trip and this is a departure time.
     * Otherwise, will be null.
     *
     * @return
     */
    public Date getScheduledDate() {
        return scheduledTime;
    }

    public long getScheduledTime() {
        return scheduledTime.getTime();
    }

    /**
     * Returns the schedule adherence for the stop if there was a schedule time. Otherwise returns
     * null.
     *
     * @return
     */
    public TemporalDifference getScheduleAdherence() {
        // If there is no schedule time for this stop then there
        // is no schedule adherence information.
        if (scheduledTime == null) {
            return null;
        }

        // Return the schedule adherence
        return new TemporalDifference(scheduledTime.getTime() - key.time.getTime());
    }

    /**
     * Returns the Stop object associated with the arrival/departure. Will only be valid for the
     * Core system where the configuration has been read in.
     *
     * @return The Stop associated with the arrival/departure
     */
    public Stop getStop() {
        return Core.getInstance().getDbConfig().getStop(key.stopId);
    }

    /**
     * @return the gtfsStopSequence associated with the arrival/departure
     */
    public int getGtfsStopSequence() {
        return key.getGtfsStopSeq();
    }
}
