/* (C)2023 */
package org.transitclock.core.domain;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Delegate;
import org.hibernate.HibernateException;
import org.hibernate.collection.internal.PersistentList;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.SessionImpl;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.transitclock.applications.Core;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.configData.AgencyConfig;
import org.transitclock.configData.CoreConfig;
import org.transitclock.core.SpatialMatch;
import org.transitclock.utils.Time;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.utils.IntervalTimer;

import java.io.Serializable;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.*;

/**
 * Represents assignment for a vehicle for a day. Obtained by combining data from multiple GTFS
 * files.
 *
 * <p>Thought a good deal about how to represent all the stops in the block. Wanted to have a list
 * of trip patterns instead of trips because that would be more efficient. But then realized that
 * for each trip need different times and such. Therefore decided to use full list of Trips. This
 * will make the data in the database unfortunately quite large.
 *
 * @author SkiBu Smith
 */
@Document(collection = "Blocks")
public final class Block implements Serializable {
    @Data
    @Builder(toBuilder = true, setterPrefix = "with")
    public static class Key {
        private final int configRev;

        private final String blockId;

        private final String serviceId;
    }


    @Id
    @Delegate
    private final Key key;
    // Start time of block assignment. In seconds from midnight. Can be less
    // than 0 to indicate that block starts before midnight of the current
    // day. Can be greater than one day to indicate that block starts after
    // midnight of the current day.
    private final int startTime;

    // End time of block assignment. In seconds from midnight. Can be less
    // than 0 to indicate that block ends before midnight of the current
    // day. Can be greater than one day to indicate that block ends after
    // midnight of the current day.
    private final int endTime;

    // Need to have a ManyToMany instead of OneToMany relationship
    // for the List of Trips because several Blocks can refer to the
    // same trip. This is because "UNSCHEDULED" blocks can be created
    // using regular trips. For this situation don't want Hibernate to
    // try to store the same trip twice because then would get a uniqueness
    // violation.
    //
    // Use CascadeType.SAVE_UPDATE so that when the TripPattern is stored
    // the Paths are automatically stored.
    //
    // Use FetchType.LAZY so that don't read in all trip data at once since
    // that in turn reads in trip pattern and travel time info, which can
    // be voluminous and therefore slow. The trips will be read in when
    // getTrips() is called.
//    @Cascade({CascadeType.SAVE_UPDATE})
    @DocumentReference(lazy = true)
    private final List<Trip> trips;

    // Sometimes will get vehicle assignment by routeId. This means that need
    // to know which blocks are associated with a route. Getting the routeIds
    // from the Trip objects is problematic because then all the Trip data
    // needs to be lazy loaded such that if need to look at all blocks then
    // need to load in all trips. That takes too long for when doing debugging.
    // So to speed things up the routeIds for a block are stored here.
    // NOTE: since trying to use serialization need to use ArrayList<> instead
    // of List<> since List<> doesn't implement Serializable.
    private final HashSet<String> routeIds;

    private static BooleanConfigValue blockLoading = new BooleanConfigValue(
            "transitclock.blockLoading.agressive",
            false,
            "Set true to eagerly fetch all blocks into memory on startup");

    private static final Logger logger = LoggerFactory.getLogger(Block.class);

    /**
     * This constructor called when processing GTFS data and creating a Block to be stored in the
     * database. Note: startTime and endTime could in theory be determined here by looking at first
     * and last TripElements. But what if they haven't been set in GTFS? Want the calling function
     * to do this kind of error checking because it does other error checking and can log issues
     * appropriately.
     *
     * @param configRev
     * @param blockId
     * @param startTime
     * @param endTime
     * @param trips
     */
    public Block(int configRev, String blockId, String serviceId, int startTime, int endTime, List<Trip> trips) {
        this.key = Key.builder()
                .withConfigRev(configRev)
                .withBlockId(blockId)
                .withServiceId(serviceId)
                .build();
        this.startTime = startTime;
        this.endTime = endTime;
        this.trips = trips;

        // Obtain the set of route IDs from the trips
        this.routeIds = new HashSet<>();
        for (Trip trip : trips) {
            this.routeIds.add(trip.getRouteId());
        }
    }


    /**
     * Finds the trip for the block where secondsIntoDay lies between the end time of the previous
     * trip and the end time of the current trip. Does not take into account wrapping around
     * midnight.
     *
     * @param secondsIntoDay
     * @return index of trip, or -1 if no match
     */
    private int activeTripIndex(int secondsIntoDay) {
        List<Trip> trips = getTrips();
        int previousTripEndTimeSecs = trips.get(0).getStartTime();
        for (int i = 0; i < trips.size(); ++i) {
            Trip trip = trips.get(i);
            int tripEndTimeSecs = trip.getEndTime();
            if (secondsIntoDay > previousTripEndTimeSecs && secondsIntoDay < tripEndTimeSecs) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Determines which trip is currently active and returns the associated index of that trip such
     * that getTrip(int) can be called to get the trip. Trip is active if the date lies between the
     * end time of the previous trip and the end time of the current trip. To handle wrapping around
     * midnight if there isn't a match initially then will check past midnight and before midnight
     * as well.
     *
     * <p>Often consider a block to be active before its scheduled start time. For this situation
     * the first trip is returned if it is within allowableBeforeTimeSecs before the block is
     * supposed to start.
     *
     * @param date The current time, to be used for determining if trip active.
     * @param allowableBeforeTimeSecs How much before the block time the block is considered to be
     *     active. Needed because often consider a block to be active before its scheduled start
     *     time. For this situation need to return first trip.
     * @return index of trip, or -1 if no match
     */
    public int activeTripIndex(Date date, int allowableBeforeTimeSecs) {
        // Find matching trip
        int secondsIntoDay = Core.getInstance().getTime().getSecondsIntoDay(date);
        int index = activeTripIndex(secondsIntoDay);

        // If match not found check a day into future and day into past
        if (index < 0) {
            index = activeTripIndex(secondsIntoDay + Time.SEC_PER_DAY);
            if (index < 0) index = activeTripIndex(secondsIntoDay - Time.SEC_PER_DAY);
        }

        // Date is not when trip is supposed to be active. But since blocks
        // are considered to be active allowableBeforeTimeSecs before their
        // scheduled start time if the block is active then must be matching
        // to the first trip, so return 0.
        if (index < 0 && isActive(date, allowableBeforeTimeSecs, -1)) return 0;

        // Return result
        return index;
    }

    /**
     * Returns true if the service ID for the block is for the time specified by offset parameter.
     * Useful for determining if the service ID for the block is valid for today, yesterday, or for
     * tomorrow.
     *
     * @param date The date want to see if the block is active for
     * @param offset Use 0 for today, -Time.DAY_IN_MSECS for yesterday, and Time.DAY_IN_MSECS for
     *     today.
     * @return true if service ID for block as valid for other day
     */
    private boolean serviceClassIsValidForDay(Date date, long offset) {
        long dateToCheck = date.getTime() + offset;
        List<String> currentServiceIds = Core.getInstance().getServiceUtils().getServiceIdsForDay(dateToCheck);

        return currentServiceIds.contains(serviceId);
    }

    /**
     * Returns true if the time of day of the date passed in is between allowableBeforeTimeSecs
     * before the startTime and the endTime for the block. No leeway is provided for the end time.
     * Note: does not look to see if the service associated with the block is active. Only looks at
     * time of day.
     *
     * @param date The time checking to see whether block is active for
     * @param allowableBeforeTimeSecs Block considered active if within this number of seconds
     *     before the block start time. Set to 0 if want to know if date is actually between the
     *     block start and end times.
     * @param allowableAfterStartTimeSecs If set to value greater than or equal to zero then block
     *     considered active only if within this number of seconds after the start time. If less
     *     then zero then block considered active up to the block end time.
     * @return True if the block is active for the specified date
     */
    public boolean isActive(Date date, int allowableBeforeTimeSecs, int allowableAfterStartTimeSecs) {
        int secsInDay = Core.getInstance().getTime().getSecondsIntoDay(date);

        // Determine the allowable start and end times for when the block
        // is to be considered active
        int allowableStartTime = startTime - allowableBeforeTimeSecs;
        int allowableEndTime = allowableAfterStartTimeSecs < 0 ? endTime : startTime + allowableAfterStartTimeSecs;

        // Handle normal situation where times are between midnight in the
        // morning and midnight in the evening
        boolean serviceClassValidToday = serviceClassIsValidForDay(date, 0);
        if (serviceClassValidToday) {
            if (secsInDay > allowableStartTime && secsInDay < allowableEndTime) {
                return true;
            }
        }

        // If the service class was valid yesterday then try adding 24 hours to
        // the time when checking if block active. This way handle situations
        // past midnight, but only if the block was actually active yesterday.
        boolean serviceClassValidYesterday = serviceClassIsValidForDay(date, -Time.DAY_IN_MSECS);
        if (serviceClassValidYesterday) {
            int secsInDayPastMidnight = secsInDay + Time.DAY_IN_SECS;
            if (secsInDayPastMidnight > allowableStartTime && secsInDayPastMidnight < allowableEndTime) {
                return true;
            }
        }

        // If the service class is valid tomorrow then try subtracting 24 hours
        // from the time when checking if block active. This way handle
        // situations before midnight, but won't include a block that isn't
        // actually active the next day.
        boolean serviceClassValidTomorrow = serviceClassIsValidForDay(date, Time.DAY_IN_MSECS);
        if (serviceClassValidTomorrow) {
            int secsInDayBeforeMidnight = secsInDay - Time.SEC_PER_DAY;
            return secsInDayBeforeMidnight > allowableStartTime && secsInDayBeforeMidnight < allowableEndTime;
        }

        // It simply ain't active
        return false;
    }

    /**
     * Returns true if the time of day of the date passed in is between allowableBeforeTimeSecs
     * before the startTime and the endTime for the block. No leeway is provided for the end time.
     * Note: does not look to see if the service associated with the block is active. Only looks at
     * time of day.
     *
     * @param epochTime The time checking to see whether block is active for
     * @param allowableBeforeTimeSecs Block considered active if within this number of seconds
     *     before the block start time. Set to 0 if want to know if date is actually between the
     *     block start and end times.
     * @param allowableAfterStartTimeSecs If set to value greater than or equal to zero then block
     *     considered active only if within this number of seconds after the start time. If less
     *     then zero then block considered active up to the block end time.
     * @return True if the block is active for the specified date
     */
    public boolean isActive(long epochTime, int allowableBeforeTimeSecs, int allowableAfterStartTimeSecs) {
        return isActive(new Date(epochTime), allowableBeforeTimeSecs, allowableAfterStartTimeSecs);
    }

    /**
     * Returns true if the time of day of the epoch time passed in is between
     * allowableBeforeTimeSecs before the startTime and the endTime for the block. No leeway is
     * provided for the end time. Note: does not look to see if the service associated with the
     * block is active. Only looks at time of day.
     *
     * @param epochTime
     * @param allowableBeforeTimeSecs
     * @return
     */
    public boolean isActive(long epochTime, int allowableBeforeTimeSecs) {
        return isActive(new Date(epochTime), allowableBeforeTimeSecs, -1);
    }

    /**
     * Returns true if the time of day of the date passed in is between the startTime and the
     * endTime for the block. No leeway is provided. Note: does not look to see if the service
     * associated with the block is active. Only looks at time of day.
     *
     * @param date
     * @return True if the block is active.
     */
    public boolean isActive(Date date) {
        return isActive(date, 0, -1);
    }

    /**
     * Returns true if the time of day of the date passed in is between the startTime and the
     * endTime for the block. No leeway is provided. Note: does not look to see if the service
     * associated with the block is active. Only looks at time of day.
     *
     * @param epochTime
     * @return True if the block is active.
     */
    public boolean isActive(long epochTime) {
        return isActive(new Date(epochTime), 0, -1);
    }

    /**
     * Returns true if the time of day of the date passed in is between allowableBeforeTimeSecs
     * before the start time and the start time.
     *
     * @param date
     * @param allowableBeforeTimeSecs
     * @return true if within allowableBeforeTimeSecs before the start time of the block
     */
    public boolean isBeforeStartTime(Date date, int allowableBeforeTimeSecs) {
        int secsInDayForAvlReport = Core.getInstance().getTime().getSecondsIntoDay(date);

        return (secsInDayForAvlReport > startTime - allowableBeforeTimeSecs && secsInDayForAvlReport < startTime)
                // also handle where date before midnight but start time is after
                || (secsInDayForAvlReport > startTime + Time.SEC_PER_DAY - allowableBeforeTimeSecs
                        && secsInDayForAvlReport < startTime + Time.SEC_PER_DAY);
    }

    /**
     * If the trip is active at the secsInDayForAvlReport then it is added to the tripsThatMatchTime
     * list. Trip is considered active if it is within start time of trip minus
     * CoreConfig.getAllowableEarlyForLayoverSeconds() and within the end time of the trip. No
     * leniency is made for the end time since once a trip is over really don't want to assign
     * vehicle to that trip. Yes, vehicles often run late, but that should only be taken account
     * when matching to already predictable vehicle.
     *
     * @param vehicleId for logging messages
     * @param secsInDayForAvlReport
     * @param trip
     * @param tripsThatMatchTime
     * @return
     */
    private static boolean addTripIfActive(
            String vehicleId, int secsInDayForAvlReport, Trip trip, List<Trip> tripsThatMatchTime) {
        int startTime = trip.getStartTime();
        int endTime = trip.getEndTime();

        int allowableEarlyTimeSecs = CoreConfig.getAllowableEarlyForLayoverSeconds();
        if (secsInDayForAvlReport > startTime - allowableEarlyTimeSecs && secsInDayForAvlReport < endTime) {
            tripsThatMatchTime.add(trip);

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Determined that for blockId={} that a trip is "
                                + "considered to be active for AVL time. "
                                + "TripId={}, tripIndex={} AVLTime={}, "
                                + "startTime={}, endTime={}, "
                                + "allowableEarlyForLayover={} secs, allowableLate={} secs, "
                                + "vehicleId={}",
                        trip.getBlock().getId(),
                        trip.getId(),
                        trip.getBlock().getTripIndex(trip),
                        Time.timeOfDayStr(secsInDayForAvlReport),
                        Time.timeOfDayStr(trip.getStartTime()),
                        Time.timeOfDayStr(trip.getEndTime()),
                        CoreConfig.getAllowableEarlyForLayoverSeconds(),
                        CoreConfig.getAllowableLateSeconds(),
                        vehicleId);
            }

            return true;
        }

        if (logger.isDebugEnabled())
            logger.debug(
                    "block {} is not active for vehicleId {}", trip.getBlock().getId(), vehicleId);

        // Not a match so return false
        return false;
    }

    /**
     * For this block determines which trips are currently active. Should work even for trips that
     * start before midnight or go till after midnight. Trip is considered active if it is within
     * start time of trip minus CoreConfig.getAllowableEarlyForLayoverSeconds() and within the end
     * time of the trip. No leniency is made for the end time since once a trip is over really don't
     * want to assign vehicle to that trip.
     *
     * @param avlReport
     * @return List of Trips that are active. If none are active an empty list is returned.
     */
    public List<Trip> getTripsCurrentlyActive(AvlReport avlReport) {
        // Set for returning results
        List<Trip> tripsThatMatchTime = new ArrayList<>();

        // Convenience variable
        String vehicleId = avlReport.getVehicleId();

        // Go through trips and find ones
        List<Trip> trips = getTrips();
        for (Trip trip : trips) {
            // If time of avlReport is within reasonable time of the trip
            // time then this trip should be returned.
            int secsInDayForAvlReport = Core.getInstance().getTime().getSecondsIntoDay(avlReport.getDate());

            // If the trip is active then add it to the list of active trips
            boolean tripIsActive = addTripIfActive(vehicleId, secsInDayForAvlReport, trip, tripsThatMatchTime);

            // if trip wasn't active might be because trip actually starts before
            // midnight so should check for that special case.
            if (!tripIsActive)
                tripIsActive =
                        addTripIfActive(vehicleId, secsInDayForAvlReport - Time.SEC_PER_DAY, trip, tripsThatMatchTime);

            // if trip still wasn't active might be because trip goes past
            // midnight so should check for that special case.
            if (!tripIsActive)
                tripIsActive =
                        addTripIfActive(vehicleId, secsInDayForAvlReport + Time.SEC_PER_DAY, trip, tripsThatMatchTime);
        }

        // Returns results
        return tripsThatMatchTime;
    }

    /**
     * @return the blockId
     */
    public String getId() {
        return key.blockId;
    }


    /**
     * Uses lazy initialization to determine the trips for the block.
     *
     * @return the trips as an unmodifiable collection
     */
    public List<Trip> getTrips() {
        // If trips already lazy loaded then simply return them
        if (Hibernate.isInitialized(trips)) return Collections.unmodifiableList(trips);

        // Trips not yet lazy loaded so do so now.
        // It appears that lazy initialization is problematic when have multiple
        // simultaneous threads. Get "org.hibernate.AssertionFailure: force
        // initialize loading collection". Therefore need to make sure that
        // only loading lazy sub-data serially. Since it is desirable to have
        // trips collection be lazy loaded so that app starts right away without
        // loading all the sub-data for every block assignment need to make
        // sure this is done in a serialized way. Having app not load all data
        // at startup is especially important when debugging.
        // trips not yet initialized so synchronize so only a single
        // thread can initialize at once and then access something
        // in trips that will cause it to be lazy loaded.
        synchronized (lazyLoadingSyncObject) {
            logger.debug(
                    "About to do lazy load for trips data for " + "blockId={} serviceId={}...", blockId, serviceId);
            IntervalTimer timer = new IntervalTimer();

            // Access the collection so that it is lazy loaded.
            // Problems can be difficult to debug so log error along
            // with the SQL.
            try {
                // First see if the session associated with trips is different
                // from the current global session. This can happen if a new
                // global session was created when trips for another block was
                // loaded and it was found that the old session was no longer
                // valid, such as when the db is rebooted.
                if (trips instanceof PersistentList) {
                    // Get the current session associated with the trips.
                    // Can be null.
                    PersistentList persistentListTrips = (PersistentList) trips;
                    SessionImplementor session = persistentListTrips.getSession();

                    // If the session is different from the global
                    // session then need to attach the new session to the
                    // object.
                    DbConfig dbConfig = Core.getInstance().getDbConfig();
                    Session globalLazyLoadSession = dbConfig.getGlobalSession();
                    if (session != globalLazyLoadSession) {
                        // The persistent object is using an old session so
                        // switch to new one
                        logger.info(
                                "For blockId={} was using an old session "
                                        + "(hash={}) instead of the current "
                                        + "globalLazyLoadSession (hash={}). Therefore "
                                        + "switching the Block to use the new "
                                        + "globalLazyLoadSession.",
                                getId(),
                                session == null ? null : session.hashCode(),
                                globalLazyLoadSession.hashCode());

                        globalLazyLoadSession.update(this);
                    }
                } else {
                    logger.error("Blocks.trips member is not a PersistentList!?!?. ");
                    // not exiting here....
                }

                // Actually lazy-load the trips
                trips.get(0);
            } catch (JDBCException e) {
                // TODO this is an anti-pattern
                // If root cause of exception is a SocketTimeoutException
                // then somehow lost connection to the database. Might have
                // been rebooted or such. For this situation need to attach
                // object to new session.
                Throwable rootCause = HibernateUtils.getRootCause(e);
                if (rootCause instanceof SocketTimeoutException || rootCause instanceof SocketException) {
                    logger.error(
                            "Socket timeout in getTrips() for "
                                    + "blockId={}. Database might have been "
                                    + "rebooted. Creating a new session.",
                            this.getId(),
                            e);

                    if (!(rootCause instanceof SocketException
                            || rootCause instanceof SocketTimeoutException
                            || rootCause instanceof PSQLException)) {
                        logger.error(
                                "For agencyId={} in Blocks.getTrips() for "
                                        + "blockId={} encountered exception whose root "
                                        + "cause was not a SocketException, "
                                        + "SocketTimeoutException, or PSQLException,"
                                        + "which therefore is unexpected. Therefore should "
                                        + "investigate. Root cause is {}.",
                                AgencyConfig.getAgencyId(),
                                this.getId(),
                                rootCause,
                                e);
                    }

                    // Even though there was a timeout meaning that the
                    // session is no longer any good the Block object
                    // might still be associated with the old session.
                    // In order to attach the Block to a newly created
                    // session need to first close the old session or else
                    // system will complain that trying to add a object
                    // to two live sessions. Tried using session.evict(this)
                    // but still got exception "Illegal attempt to associate
                    // a collection with two open sessions"
                    PersistentList persistentListTrips = (PersistentList) trips;
                    SessionImplementor sessionImpl = persistentListTrips.getSession();
                    SessionImpl session = (SessionImpl) sessionImpl;
                    if (!session.isClosed()) {
                        try {
                            // Note: this causes a stack trace to be output
                            // to stdout by Hibernate. Seems that this
                            // cannot be avoided since need to close the
                            // session.
                            session.close();
                        } catch (HibernateException e1) {
                            logger.error(
                                    "Exception occurred when trying "
                                            + "to close session when lazy loading "
                                            + "data after socket timeout occurred.",
                                    e1);
                        }
                    }

                    // Get new session, update object to use it, and try again.
                    // Note: before calling get(0) to load the data first made
                    // sure that the session used for the Block.trips is the same
                    // as the current session. Therefore if made it here then it
                    // means that definitely need to create new session.
                    DbConfig dbConfig = Core.getInstance().getDbConfig();
                    logger.info("CREATING NEW SESSION");
                    dbConfig.createNewGlobalSession();
                    Session globalLazyLoadSession = dbConfig.getGlobalSession();
                    globalLazyLoadSession.update(this);

                    // Now that have attached a new session lazy load the trips
                    // data
                    trips.get(0);
                } else {
                    // Not a socket timeout. Therefore don't know handle
                    // to handle so just log and throw the exception
                    logger.error(
                            "In Block.getTrips() got JDBCException. " + "SQL=\"{}\" msg={}",
                            e.getSQL(),
                            e.getSQLException().getMessage(),
                            e);
                    throw e;
                }

                // Actually lazy-load the trips
                trips.get(0);
            }

            logger.debug(
                    "Finished lazy load for trips data for " + "blockId={} serviceId={}. Took {} msec",
                    blockId,
                    serviceId,
                    timer.elapsedMsec());
        }

        return Collections.unmodifiableList(trips);
    }

    /**
     * Returns true if block assignment has no schedule (is frequency based)
     *
     * @return true if no schedule
     */
    public boolean isNoSchedule() {
        return getTrips().get(0).isNoSchedule();
    }

    /**
     * Returns true if block assignment has a schedule (is not frequency based)
     *
     * @return true if has schedule
     */
    public boolean hasSchedule() {
        return !isNoSchedule();
    }

    /**
     * Returns the trip specified by the tripIndex
     *
     * @param tripIndex
     * @return the Trip specified by tripIndex. If index out of range returns null
     */
    public Trip getTrip(int tripIndex) {
        // If index out of range return null
        if (tripIndex < 0 || tripIndex >= getTrips().size()) {
            return null;
        }

        // Return the specified trip
        return getTrips().get(tripIndex);
    }

    /**
     * Returns the trip for the block as specified by the tripId parameter
     *
     * @param tripId Which trip is to be returned
     * @return The Trip that matches the tripId
     */
    public Trip getTrip(String tripId) {
        for (Trip trip : getTrips()) if (trip.getId().equals(tripId)) return trip;

        // The tripId was not found for this block so return null
        return null;
    }

    /**
     * Returns for the specified trip the index into the trips list for the block
     *
     * @param trip Specifies which trip looking for
     * @return Index into trips of the specified trip
     */
    public int getTripIndex(Trip trip) {
        List<Trip> tripsList = getTrips();
        for (int i = 0; i < tripsList.size(); ++i) {
            if (tripsList.get(i) == trip) return i;
        }
        return -1;
    }

    /**
     * Returns collection of route IDs associated with the Block. Each route ID will only be
     * included once.
     *
     * @return
     */
    public Set<String> getRouteIds() {
        return routeIds;
        //      Note: previously was getting them from trips but this requires a call
        //      to getTrips() which loads in all the data from db for the trips for
        //      the block, which is quite slow. By generating the routeIds when the
        //      GTFS data is processed and storing them in the db then don't need
        //      the application to load in all trip data, which is great for debugging.
        //		Set<String> routeIdsSet = new HashSet<String>();
        //		for (Trip trip : getTrips()) {
        //			routeIdsSet.add(trip.getRouteId());
        //		}
        //		return routeIdsSet;
    }

    /**
     * Returns the trip patterns associated with the block. Note that these are not in any kind of
     * fixed order since trip patterns are used multiple times in a block.
     *
     * @return
     */
    public Collection<TripPattern> getTripPatterns() {
        // Create map of trip patterns for this block so that can
        // return just a single copy of each one. Keyed on trip pattern ID
        Map<String, TripPattern> tripPatternsMap = new HashMap<>();
        for (Trip trip : getTrips()) {
            TripPattern tripPattern = trip.getTripPattern();
            tripPatternsMap.put(tripPattern.getId(), tripPattern);
        }

        // Return the collection of unique trip patterns
        return tripPatternsMap.values();
    }

    /**
     * Returns the travel time for the specified path. Does not include stop times.
     *
     * @param tripIndex
     * @param stopPathIndex
     * @return
     */
    public int getStopPathTravelTime(int tripIndex, int stopPathIndex) {
        Trip trip = getTrip(tripIndex);
        TravelTimesForStopPath travelTimesForPath = trip.getTravelTimesForStopPath(stopPathIndex);

        return travelTimesForPath.getStopPathTravelTimeMsec();
    }

    /**
     * Returns the time in msec for how long expected to be at the stop at the end of the stop path.
     *
     * @param tripIndex
     * @param stopPathIndex
     * @return
     */
    public int getPathStopTime(int tripIndex, int stopPathIndex) {
        Trip trip = getTrip(tripIndex);
        TravelTimesForStopPath travelTimesForPath = trip.getTravelTimesForStopPath(stopPathIndex);
        return travelTimesForPath.getStopTimeMsec();
    }

    /**
     * Returns the Vector of the segment specified.
     *
     * @param tripIndex
     * @param stopPathIndex
     * @param segmentIndex
     * @return The Vector for the specified segment or null if the indices are out of range.
     */
    public Vector getSegmentVector(int tripIndex, int stopPathIndex, int segmentIndex) {
        Trip trip = getTrip(tripIndex);
        if (trip == null) return null;
        StopPath stopPath = trip.getStopPath(stopPathIndex);
        if (stopPath == null) return null;
        Vector segmentVector = stopPath.getSegmentVector(segmentIndex);
        return segmentVector;
    }

    /**
     * Returns number of segments for the path specified
     *
     * @param tripIndex
     * @param stopPathIndex
     * @return Number of segments for specified path or -1 if an index is out of range.
     */
    public int numSegments(int tripIndex, int stopPathIndex) {
        // Determine number of segments
        Trip trip = getTrip(tripIndex);
        if (trip == null) return -1;
        StopPath path = trip.getStopPath(stopPathIndex);
        if (path == null) return -1;
        return path.getSegmentVectors().size();
    }

    /**
     * Returns number of stopPaths for the trip specified.
     *
     * @param tripIndex
     * @return Number of stopPaths for specified trip or -1 if index is out of range.
     */
    public int numStopPaths(int tripIndex) {
        Trip trip = getTrip(tripIndex);
        if (trip == null) return -1;

        TravelTimesForTrip travelTimesForTrip = trip.getTravelTimes();
        List<TravelTimesForStopPath> travelTimesForStopPaths = travelTimesForTrip.getTravelTimesForStopPaths();
        return travelTimesForStopPaths.size();
    }

    /**
     * Returns the number of trips.
     *
     * @return
     */
    public int numTrips() {
        return getTrips().size();
    }

    /**
     * Returns true if path is for a stop that is configured to be layover stop for the trip
     * pattern. A layover stop is when a vehicle can leave route path before departing this stop
     * since the driver is taking a break.
     *
     * @param tripIndex
     * @param stopPathIndex
     * @return
     */
    public boolean isLayover(int tripIndex, int stopPathIndex) {
        return getStopPath(tripIndex, stopPathIndex).isLayoverStop();
    }

    /**
     * Indicates that vehicle is not supposed to depart the stop until the scheduled departure time.
     *
     * @return true if a wait stop
     */
    public boolean isWaitStop(int tripIndex, int stopPathIndex) {
        return getStopPath(tripIndex, stopPathIndex).isWaitStop();
    }

    /**
     * Returns the path specified by tripIndex and stopPathIndex params.
     *
     * @param tripIndex
     * @param stopPathIndex
     * @return the StopPath or null if tripIndex or stopPathIndex are out of range.
     */
    public StopPath getStopPath(int tripIndex, int stopPathIndex) {
        // Get the trip
        Trip trip = getTrip(tripIndex);
        if (trip == null) {
            logger.error(
                    "In Block.getStopPath() tripIndex={} is out of range " + "(stopPathIndex={}) for block={}",
                    tripIndex,
                    stopPathIndex,
                    this);
            return null;
        }

        // Get the stop path
        StopPath stopPath = trip.getStopPath(stopPathIndex);
        if (stopPath == null) {
            logger.error(
                    "In Block.getStopPath() stopPathIndex={} is out of " + "range for tripIndex={} trip={} of block={}",
                    stopPathIndex,
                    tripIndex,
                    trip,
                    this);
            return null;
        }

        // Return the stop path
        return stopPath;
    }

    /**
     * Returns the location of the first stop of the block.
     *
     * @return
     */
    public Location getStartLoc() {
        StopPath firstStopPath = getStopPath(0, 0);
        if (firstStopPath != null) return firstStopPath.getEndOfPathLocation();
        else return null;
    }

    /**
     * Returns the previous path specified by tripIndex and stopPathIndex params. If wrapping back
     * past beginning of block (where tripIndex becomes negative) then returns null.
     *
     * @param tripIndex
     * @param stopPathIndex
     * @return
     */
    public StopPath getPreviousPath(int tripIndex, int stopPathIndex) {
        // First, determine trip and path index for the previous path
        --stopPathIndex;
        if (stopPathIndex < 0) {
            --tripIndex;
            if (tripIndex < 0) return null;
            stopPathIndex = getTrip(tripIndex).getTripPattern().getStopPaths().size() - 1;
        }

        // Return the previous path
        return getStopPath(tripIndex, stopPathIndex);
    }

    /**
     * Returns the ScheduleTime for that stop specified by the trip and path indices.
     *
     * @param tripIndex
     * @param stopPathIndex
     * @return the schedule time for the specified stop. Returns null if no schedule time associated
     *     with stop
     */
    public ScheduleTime getScheduleTime(int tripIndex, int stopPathIndex) {
        Trip trip = getTrip(tripIndex);
        return trip.getScheduleTime(stopPathIndex);
    }

    /**
     * Returns true if on last trip of block and within the specified distance of the end of that
     * last trip.
     *
     * @param match
     * @param distance
     * @return True if within distance of end of block
     */
    public boolean nearEndOfBlock(SpatialMatch match, double distance) {
        // If not last trip of block then not considered near end
        // so return false.
        if (match.getTripIndex() != trips.size() - 1) return false;

        return match.withinDistanceOfEndOfTrip(distance);
    }

    /**
     * Returns true if this block assignment should be exclusive, such that when a vehicle is
     * assigned to this block any other vehicles assigned to this block will have their assignments
     * removed.
     *
     * <p>Current it is configured using Java property instead of in the database.
     *
     * @return True if this block assignment should be exclusively assigned to only a single vehicle
     *     at a time
     */
    public boolean shouldBeExclusive() {
        return CoreConfig.exclusiveBlockAssignments();
    }
}
