/* (C)2023 */
package org.transitclock.domain.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.Configuration;
import org.transitclock.domain.structs.ActiveRevisions;
import org.transitclock.domain.structs.Agency;
import org.transitclock.domain.structs.Arrival;
import org.transitclock.domain.structs.AvlReport;
import org.transitclock.domain.structs.Block;
import org.transitclock.domain.structs.Calendar;
import org.transitclock.domain.structs.CalendarDate;
import org.transitclock.domain.structs.ConfigRevision;
import org.transitclock.domain.structs.DbTest;
import org.transitclock.domain.structs.Departure;
import org.transitclock.domain.structs.ExportTable;
import org.transitclock.domain.structs.FareAttribute;
import org.transitclock.domain.structs.FareRule;
import org.transitclock.domain.structs.Frequency;
import org.transitclock.domain.structs.Headway;
import org.transitclock.domain.structs.HoldingTime;
import org.transitclock.domain.structs.Match;
import org.transitclock.domain.structs.MeasuredArrivalTime;
import org.transitclock.domain.structs.MonitoringEvent;
import org.transitclock.domain.structs.Prediction;
import org.transitclock.domain.structs.PredictionAccuracy;
import org.transitclock.domain.structs.PredictionEvent;
import org.transitclock.domain.structs.PredictionForStopPath;
import org.transitclock.domain.structs.Route;
import org.transitclock.domain.structs.Stop;
import org.transitclock.domain.structs.StopPath;
import org.transitclock.domain.structs.Transfer;
import org.transitclock.domain.structs.TravelTimesForStopPath;
import org.transitclock.domain.structs.TravelTimesForTrip;
import org.transitclock.domain.structs.Trip;
import org.transitclock.domain.structs.TripPattern;
import org.transitclock.domain.structs.VehicleConfig;
import org.transitclock.domain.structs.VehicleEvent;
import org.transitclock.domain.structs.VehicleState;
import org.transitclock.domain.structs.VehicleToBlockConfig;
import org.transitclock.domain.webstructs.ApiKey;
import org.transitclock.domain.webstructs.WebAgency;

/**
 * Yes, this is a nuisance. If Hibernate Session class is used instead of the JPA EntityManager
 * class then all annotated classes have to be explicitly added to the Configuration object. Don't
 * want to get into using the JPA EntityManager class for now (just trying to learn Hibernate based
 * on books such as Harnessing Hibernate). So need to do this.
 *
 * <p>These annotated classes could also be listed in the hibernate.cfg.xml config file but that
 * would be difficult to maintain. Each application might already have a modified hibernate config
 * file. Don't want to have to have each app modify their config file when new annotated classes are
 * added. Therefore it is best to configure these classes programmatically.
 *
 * @author SkiBu Smith
 */
@Slf4j
public class AnnotatedClassesList {

    // List here all the annotated classes that can be stored in the db
    private static final Class<?>[] classList = new Class[] {
        ActiveRevisions.class,
        Agency.class,
        Arrival.class,
        AvlReport.class,
        Block.class,
        Calendar.class,
        CalendarDate.class,
        ConfigRevision.class,
        DbTest.class,
        ExportTable.class,
        Prediction.class,
        Departure.class,
        FareAttribute.class,
        FareRule.class,
        Frequency.class,
        Match.class,
        MeasuredArrivalTime.class,
        MonitoringEvent.class,
        PredictionAccuracy.class,
        Route.class,
        Stop.class,
        StopPath.class,
        Transfer.class,
        TravelTimesForStopPath.class,
        PredictionForStopPath.class,
        TravelTimesForTrip.class,
        Trip.class,
        TripPattern.class,
        VehicleEvent.class,
        PredictionEvent.class,
        VehicleConfig.class,
        VehicleState.class,
        VehicleToBlockConfig.class,
        HoldingTime.class,
        Headway.class,

        // For website
        ApiKey.class,
        WebAgency.class,
    };

    /**
     * Adds the classes listed within this method to the Hibernate configuration. Needed so that the
     * class can be used with Hibernate.
     *
     * @param config
     */
    public static void addAnnotatedClasses(Configuration config) {
        // Add all the annotated classes to the config
        for (var c : classList) {
            logger.trace("Adding to Hibernate config the annotated class {}", c.getName());
            config.addAnnotatedClass(c);
        }
    }
}
