/* (C)2023 */
package org.transitclock.domain.hibernate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import lombok.extern.slf4j.Slf4j;
import org.transitclock.domain.structs.ArrivalDeparture;
import org.transitclock.domain.structs.AvlReport;
import org.transitclock.domain.structs.Match;
import org.transitclock.domain.structs.MonitoringEvent;
import org.transitclock.domain.structs.Prediction;
import org.transitclock.domain.structs.PredictionAccuracy;
import org.transitclock.domain.structs.VehicleConfig;
import org.transitclock.domain.structs.VehicleEvent;
import org.transitclock.domain.structs.VehicleState;
import org.transitclock.utils.threading.ExtendedScheduledThreadPoolExecutor;
import org.transitclock.utils.threading.NamedThreadFactory;

/**
 * DataDbLogger is for storing to the db a stream of data objects. It is intended for example for
 * storing AVL reports, vehicle matches, arrivals, departures, etc. There can be quite a large
 * volume of this type of data.
 *
 * <p>The database might not always be available. It could be vacuumed, restarted, moved, etc. When
 * this happens don't want the data to be lost and don't want to tie up the core predictor.
 * Therefore a queue is used to log objects. This makes the application far more robust with respect
 * to database issues. The application simply calls add(Object o) to add the object to be stored to
 * the queue.
 *
 * <p>A separate thread is used to read from the queue and write the data to the database. If the
 * queue starts filling up then error messages are e-mailed to users alerting them that there is a
 * problem. E-mail messages are also sent out when the queue level is going down again.
 *
 * <p>A goal with this class was to make the writing to the database is efficient as possible.
 * Therefore the objects are written in batches. This reduces network traffic as well as database
 * load. But this did make handling exceptions more complicated. If there is an exception with a
 * batch then each item is individually written so that don't lose any data.
 *
 * <p>When in playback mode then don't want to store the data because it would interfere with data
 * stored when the application was run in real time. Therefore when running in playback mode set
 * shouldStoreToDb to true when calling getDataDbLogger().
 *
 * @author SkiBu Smith
 */
@Slf4j
public class DataDbLogger {
    private final DbQueue<ArrivalDeparture> arrivalDepartureQueue;
    private final DbQueue<AvlReport> avlReportQueue;
    private final DbQueue<VehicleConfig> vehicleConfigQueue;
    private final DbQueue<Prediction> predictionQueue;
    private final DbQueue<Match> matchQueue;
    private final DbQueue<PredictionAccuracy> predictionAccuracyQueue;
    private final DbQueue<MonitoringEvent> monitoringEventQueue;
    private final DbQueue<VehicleEvent> vehicleEventQueue;
    private final DbQueue<VehicleState> vehicleStateQueue;
    private final DbQueue<Object> genericQueue;

    /**
     * Constructor. Private so that factory method getDataDbLogger() has to be used. Starts up
     * separate thread that actually reads from queue and stores the data.
     *
     * @param agencyId Id of database to be written to
     * @param shouldStoreToDb Specifies whether data should actually be written to db. If in
     *     playback mode and shouldn't write data to db then set to false.
     * @param shouldPauseToReduceQueue Specifies if should pause the thread calling add() if the
     *     queue is filling up. Useful for when in batch mode and dumping a whole bunch of data to
     *     the db really quickly.
     */
    public DataDbLogger(String agencyId, boolean shouldStoreToDb, boolean shouldPauseToReduceQueue, int batchSize) {
        NamedThreadFactory threadFactory = new NamedThreadFactory("DataWriter");
        ExtendedScheduledThreadPoolExecutor executor = new ExtendedScheduledThreadPoolExecutor(5, threadFactory, new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                logger.error("Execution of {} was rejected by {}", r, executor);
            }
        });

        // So can access agencyId for logging messages
        // When running in playback mode where getting AVLReports from database
        // instead of from an AVL feed, then debugging and don't want to store
        // derived data into the database because that would interfere with the
        // derived data that was already stored in real time. For that situation
        // shouldStoreToDb should be set to false.
        // Used by add(). If queue filling up to 25% and shouldPauseToReduceQueue is
        // true then will pause the calling thread for a few seconds so that more
        // objects can be written out and not have the queue fill up.
        arrivalDepartureQueue = new DbQueue<>(executor, agencyId, shouldStoreToDb, shouldPauseToReduceQueue, ArrivalDeparture.class, batchSize);
        avlReportQueue = new DbQueue<>(executor, agencyId, shouldStoreToDb, shouldPauseToReduceQueue, AvlReport.class, batchSize);
        vehicleConfigQueue = new DbQueue<>(executor, agencyId, shouldStoreToDb, shouldPauseToReduceQueue, VehicleConfig.class, batchSize);
        predictionQueue = new DbQueue<>(executor, agencyId, shouldStoreToDb, shouldPauseToReduceQueue, Prediction.class, batchSize);
        matchQueue = new DbQueue<>(executor, agencyId, shouldStoreToDb, shouldPauseToReduceQueue, Match.class, batchSize);
        predictionAccuracyQueue = new DbQueue<>(executor, agencyId, shouldStoreToDb, shouldPauseToReduceQueue, PredictionAccuracy.class, batchSize);
        monitoringEventQueue = new DbQueue<>(executor, agencyId, shouldStoreToDb, shouldPauseToReduceQueue, MonitoringEvent.class, batchSize);
        vehicleEventQueue = new DbQueue<>(executor, agencyId, shouldStoreToDb, shouldPauseToReduceQueue, VehicleEvent.class, batchSize);
        vehicleStateQueue = new DbQueue<>(executor, agencyId, shouldStoreToDb, shouldPauseToReduceQueue, VehicleState.class, batchSize);
        genericQueue = new DbQueue<>(executor, agencyId, shouldStoreToDb, shouldPauseToReduceQueue, Object.class, batchSize);
    }

    public boolean add(ArrivalDeparture ad) {
        return arrivalDepartureQueue.add(ad);
    }

    public boolean add(AvlReport ar) {
        return avlReportQueue.add(ar);
    }

    public boolean add(VehicleConfig vc) {
        return vehicleConfigQueue.add(vc);
    }

    public boolean add(Prediction p) {
        return predictionQueue.add(p);
    }

    public boolean add(Match m) {
        return matchQueue.add(m);
    }

    public boolean add(PredictionAccuracy pa) {
        return predictionAccuracyQueue.add(pa);
    }

    public boolean add(MonitoringEvent me) {
        return monitoringEventQueue.add(me);
    }

    public boolean add(VehicleEvent ve) {
        return vehicleEventQueue.add(ve);
    }

    public boolean add(VehicleState vs) {
        return vehicleStateQueue.add(vs);
    }

    /**
     * Adds an object to be saved in the database to the queue. If queue is getting filled up then
     * an e-mail will be sent out indicating there is a problem. The queue levels at which an e-mail
     * is sent out is specified by levels. If queue has reached capacity then an error message is
     * logged.
     *
     * @param o The object that should be logged to the database
     * @return True if OK (object added to queue or logging disabled). False if queue was full.
     */
    public boolean add(Object o) {
        // this is now a catch-all -- most objects have an argument overriden method
        return genericQueue.add(o);
    }

    // the predictionQueue is the largest queue, so report on it for now
    public double queueLevel() {
        // TODO split this out into separate queues
        return predictionQueue.queueLevel();
    }

    public int queueSize() {
        return predictionQueue.queueSize();
    }
}
