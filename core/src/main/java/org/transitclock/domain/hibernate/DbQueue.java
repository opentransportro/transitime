/* (C)2023 */
package org.transitclock.domain.hibernate;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.exception.SQLGrammarException;
import org.transitclock.config.data.DbSetupConfig;
import org.transitclock.utils.ExceptionUtils;
import org.transitclock.utils.IntervalTimer;
import org.transitclock.utils.Time;

/**
 * Encapsulate the queuing operations of the database. Make generic so db-side batching is more
 * effective.
 */
@Slf4j
public class DbQueue<T> {
    // For when cannot connect to data the length of time in msec between retries
    private static final long TIME_BETWEEN_RETRIES = 1000; // msec

    private static final int QUEUE_CAPACITY = 500000;

    // The queue that objects to be stored are placed in
    private final BlockingQueue<T> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);

    // When running in playback mode where getting AVLReports from database
    // instead of from an AVL feed, then debugging and don't want to store
    // derived data into the database because that would interfere with the
    // derived data that was already stored in real time. For that situation
    // shouldStoreToDb should be set to false.
    private final boolean shouldStoreToDb;

    // Used by add(). If queue filling up to 25% and shouldPauseToReduceQueue is
    // true then will pause the calling thread for a few seconds so that more
    // objects can be written out and not have the queue fill up.
    private final boolean shouldPauseToReduceQueue;

    // The queue capacity levels when an error message should be e-mailed out.
    // The max value should be 1.0.
    private final double[] levels = {0.5, 0.8, 1.00};

    // For keeping track of index into levels, which level of capacity of
    // queue being used. When level changes then an e-mail is sent out warning
    // the operators.
    private double indexOfLevelWhenMessageLogged = 0;

    // For keeping track of maximum capacity of queue that was used.
    // Used for logging when queue use is going down.
    private double maxQueueLevel = 0.0;

    // So can access projectId for logging messages
    private final String projectId;

    // collect some statistics on how the db is performing
    private long throughputCount = 0;
    private long throughputTimestamp = System.currentTimeMillis();
    private final Class<?> shortType;

    public DbQueue(ScheduledExecutorService executor, String projectId, boolean shouldStoreToDb, boolean shouldPauseToReduceQueue, Class<?> shortType) {
        this.projectId = projectId;
        this.shouldStoreToDb = shouldStoreToDb;
        this.shouldPauseToReduceQueue = shouldPauseToReduceQueue;
        this.shortType = shortType;

        // Start up separate thread that reads from the queue and
        // actually stores the data
        executor.scheduleAtFixedRate(this::processBatchOfData, 0, TIME_BETWEEN_RETRIES, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(this::logThroughputInfo, 1, 1, TimeUnit.MINUTES);
    }

    public boolean add(T t) {
        // If in playback mode then don't want to store the
        // derived data because it would interfere with the
        // derived data already stored when was running in real time.
        if (!shouldStoreToDb) return true;

        // Add the object to the queue
        boolean success = queue.offer(t);

        double level = queueLevel();
        int levelIndex = indexOfLevel(level);
        // If reached a new level then output message e-mail to warn users
        if (levelIndex > indexOfLevelWhenMessageLogged) {
            indexOfLevelWhenMessageLogged = levelIndex;
            String message = success
                    ? "DataDbLogger queue filling up "
                            + " for projectId="
                            + projectId
                            + " and type "
                            + shortType
                            + ". It is now at "
                            + String.format("%.1f", level * 100)
                            + "% capacity with "
                            + queue.size()
                            + " elements already in the queue."
                    : "DataDbLogger queue is now completely full for projectId=" + projectId + ". LOSING DATA!!!";
            logger.error(message);
        }

        // If losing data then log such
        if (!success) {
            logger.error("DataDbLogger queue is now completely full for projectId={}and type {}. LOSING DATA!!! Failed to store object=[{}]", projectId, shortType, t);
        }

        // Keep track of max queue level so can log it when queue level
        // is decreasing again.
        if (level > maxQueueLevel) maxQueueLevel = level;

        // If shouldPauseToReduceQueue (because in batch mode or such) and
        // if queue is starting to get more full then pause the calling
        // thread for 10 seconds so that separate thread can clear out
        // queue a bit.
        if (shouldPauseToReduceQueue && level > 0.2) {
            logger.info("Pausing thread adding data to DataDbLogger queue so that queue can be cleared out. Level={}%, type={}",
                    level * 100.0, shortType);
            Time.sleep(10 * Time.MS_PER_SEC);
        }

        // Return whether was successful in adding object to queue
        return success;
    }

    private List<T> drain() {
        // Get the next object from the head of the queue
        List<T> buff = new ArrayList<>(DbSetupConfig.getBatchSize());
        int count = 0;
        do {
            count = queue.drainTo(buff, DbSetupConfig.getBatchSize());
            throughputCount += count;
            if (count == 0) {
                try {
                    Thread.sleep(TIME_BETWEEN_RETRIES);
                } catch (InterruptedException ignored) {
                }
            }
        } while (buff.isEmpty());
        logger.debug("drained {} elements", count);
        // Log if went below a capacity level
        // See if queue dropped to 10% less than the previously logged level.
        // Use a margin of 10% so that don't get flood of messages if queue
        // oscillating around a level.
        double level = queueLevel();
        int levelIndexIncludingMargin = indexOfLevel(level + 0.10);
        if (levelIndexIncludingMargin < indexOfLevelWhenMessageLogged) {
            logger.error("DataDbLogger queue emptying out somewhat  for projectId={} and type {}. It is now at {}% capacity with {} elements already in the queue. The maximum capacity was {}%.", projectId, shortType, String.format("%.1f", level * 100), queue.size(), String.format("%.1f", maxQueueLevel * 100));
            indexOfLevelWhenMessageLogged = levelIndexIncludingMargin;

            // Reset the maxQueueLevel so can determine what next peak is
            maxQueueLevel = level;
        }

        // Return the result
        return buff;
    }

    /**
     * Process a batch of data, as specified by BATCH_SIZE member. The goal is to batch a few db
     * writes together to reduce load on network and on db machines. There this method will try to
     * store multiple objects from the queue at once, up to the BATCH_SIZE.
     *
     * <p>If there is an exception with an object being written then the batch of objects will be
     * written individually so that all of the good data will still be stored.
     *
     * <p>When looked at Hibernate documentation on batch writing there is mention of using: if
     * (++batchingCounter % BATCH_SIZE == 0) { session.flush(); session.clear(); } But the above
     * doesn't commit the data to the db until the transaction commit is done. Therefore the need
     * here isn't true Hibernate batch processing. Instead, need to use a transaction for each
     * batch.
     */
    public void processBatchOfData() {
        // Create an array for holding what is being written to db. If there
        // is an exception with one of the objects, such as a constraint violation,
        // then can try to write the objects one at a time to make sure that the
        // the good ones are written. This way don't lose any good data even if
        // an exception occurs while batching data.
        List<Object> objectsForThisBatch = new ArrayList<>(DbSetupConfig.getBatchSize());

        Transaction tx = null;
        Session session = null;

        try {
            session = HibernateUtils.getSession();
            tx = session.beginTransaction();

            // Get the objects to be stored from the queue
            List<T> objectsToBeStored = drain();

            objectsForThisBatch.addAll(objectsToBeStored);
            for (Object objectToBeStored : objectsForThisBatch) {
                // Write the data to the session. This doesn't yet
                // actually write the data to the db though. That is only
                // done when the session is flushed or committed.
                logger.trace("DataDbLogger batch saving object={}", objectToBeStored);
                session.merge(objectToBeStored);
            }

            // Sometimes useful for debugging via the console
            logger.debug("Committing {} objects. {} objects still in queue.", objectsForThisBatch.size(), queueSize());
            IntervalTimer timer = new IntervalTimer();

            // Actually do the commit
            tx.commit();

            // Sometimes useful for debugging via the console
            logger.debug("Done committing. Took {} msec", timer.elapsedMsec());

            session.close();
        } catch (HibernateException e) {
            // If there was a connection problem then create a whole session
            // factory so that get new connections.
            Throwable rootCause = ExceptionUtils.getRootCause(e);

            if (rootCause instanceof SocketTimeoutException
                    || rootCause instanceof SocketException
                    || (rootCause instanceof SQLException && rootCause.getMessage().contains("statement closed"))) {
                logger.error("Had a connection problem to the database. Likely "
                        + "means that the db was rebooted or that the "
                        + "connection to it was lost. Therefore creating a new "
                        + "SessionFactory so get new connections.");
                HibernateUtils.clearSessionFactory();
            } else {
                // Rollback the transaction since it likely was not committed.
                // Otherwise can get an error when using Postgres "ERROR:
                // current transaction is aborted, commands ignored until end of
                // transaction block".
                try {
                    if (tx != null) tx.rollback();
                } catch (HibernateException e2) {
                    logger.error("Error rolling back transaction after processing batch of data via DataDbLogger.", e2);
                }

                // Close session here so that can process the objects
                // individually
                // using a new session.
                try {
                    if (session != null)
                        session.close();
                } catch (HibernateException e2) {
                    logger.error("Error closing session after processing batch of data via DataDbLogger.", e2);
                }

                // If it is a SQLGrammarException then also log the SQL to
                // help in debugging.
                String additionaInfo = e instanceof SQLGrammarException ? " SQL=\"" + ((SQLGrammarException) e).getSQL() + "\"" : "";
                Throwable cause = ExceptionUtils.getRootCause(e);
                logger.warn(
                        "{} for database for project={} when batch writing "
                                + "objects: {}. Will try to write each object "
                                + "from batch individually. {}",
                        e.getClass().getSimpleName(),
                        projectId,
                        cause.getMessage(),
                        additionaInfo,
                        e);
            }

            // Write each object individually so that the valid ones will be
            // successfully written.
            for (Object o : objectsForThisBatch) {
                boolean shouldKeepTrying = false;
                do {
                    try {
                        processSingleObject(o);
                        shouldKeepTrying = false;
                    } catch (HibernateException e2) {
                        if (e instanceof ConstraintViolationException && e2 instanceof GenericJDBCException) {
                            shouldKeepTrying = false;
                        } else {
                            // Need to know if it is a problem with the database not
                            // being accessible or if there is a problem with the SQL/data.
                            // If there is a problem accessibility of the database then
                            // want to keep trying writing the old data. But if it is
                            // a problem with the SQL/data then only want to try to write
                            // the good data from the batch a single time to make sure
                            // all good data is written.
                            if (shouldKeepTryingBecauseConnectionException(e2)) {
                                shouldKeepTrying = true;
                                logger.error(
                                        "Encountered database connection "
                                                + "exception so will sleep for {} msec and "
                                                + "will then try again.",
                                        TIME_BETWEEN_RETRIES, e2);
                                Time.sleep(TIME_BETWEEN_RETRIES);
                            }
                        }
                        // Output message on what is going on
                        Throwable cause2 = ExceptionUtils.getRootCause(e2);
                        logger.error("{} when individually writing object {}. {}msg={}", e2.getClass().getSimpleName(), o, shouldKeepTrying ? "Will keep trying. " : "", cause2.getMessage(), e2);
                    }
                } while (shouldKeepTrying);
            }
        }
    }

    /**
     * Returns how much capacity of the queue is being used up.
     *
     * @return a value between 0.0 and 1.0 indicating how much of queue being used
     */
    public double queueLevel() {
        int remainingCapacity = queue.remainingCapacity();
        int totalCapacity = queue.size() + remainingCapacity;
        return 1.0 - (double) remainingCapacity / totalCapacity;
    }

    /**
     * Returns how many items are in queue to be processed
     *
     * @return items in queue
     */
    public int queueSize() {
        return queue.size();
    }

    /**
     * Returns the index into levels that the queue capacity is at. For determining if should send
     * e-mail warning message.
     *
     * @param queueLevel
     * @return
     */
    private int indexOfLevel(double queueLevel) {
        for (int i = 0; i < levels.length; ++i) {
            if (queueLevel < levels[i]) return i;
        }
        // Must be level of 1.0 so return full size of levels array
        return levels.length;
    }

    /**
     * Determines highest level cause of exception. Useful for determine the root cause of the
     * exception so that appropriate error message can be displayed.
     *
     * @param e
     * @return
     */
    @SuppressWarnings("unused")
    private Throwable getRootCause(Exception e) {
        Throwable prev = e;
        while (true) {
            Throwable next = prev.getCause();
            if (next == null) return prev;
            else prev = next;
        }
    }

    /**
     * Returns true if the exception indicates that there is a problem connecting to the database as
     * opposed to with the SQL.
     *
     * @param e
     * @return
     */
    private boolean shouldKeepTryingBecauseConnectionException(HibernateException e) {
        // Need to know if it is a problem with the database not
        // being accessible or if there is a problem with the SQL/data.
        // If there is a problem accessibility of the database then
        // want to keep trying writing the old data. But if it is
        // a problem with the SQL/data then only want to try to write
        // the good data from the batch a single time to make sure
        // all good data is written.
        // From javadocs for for org.hivernate.exception at
        // http://docs.jboss.org/hibernate/orm/3.5/javadocs/org/hibernate/exception/package-frame.html
        // can see that there are a couple of different exception types.
        // From looking at documentation and testing found out that
        // bad SQL is indicated by
        //   ConstraintViolationException
        //   DataException
        //   SQLGrammarException
        // Appears that for bad connection could get:
        //   JDBCConnectionException (was not able to verify experimentally)
        //   GenericJDBCException    (obtained when committing transaction with db turned off)
        // So if exception is JDBCConnectionException or JDBCGenericException
        // then should keep retrying until successful.
        return e instanceof JDBCConnectionException || e instanceof GenericJDBCException;
    }

    /**
     * Store just a single object into data. This is slower than batching a few at a time. Should be
     * used when the batching encounters an exception. This way can still store all of the good data
     * from a batch.
     *
     * @param objectToBeStored
     */
    private void processSingleObject(Object objectToBeStored) {
        try (Session session = HibernateUtils.getSession()) {
            Transaction tx = session.beginTransaction();
            logger.debug("Individually saving object {}", objectToBeStored);
            session.merge(objectToBeStored);
            tx.commit();
        } catch (Exception e) {
            logger.error("Something happened while processing {}", objectToBeStored, e);
        }
    }

    private void logThroughputInfo() {
        long delta = (System.currentTimeMillis() - throughputTimestamp) / 1000;
        if (throughputCount == 0) {
            logger.debug("wrote nothing");
            return;
        }

        long throughput = throughputCount;
        throughputCount = 0;
        throughputTimestamp = System.currentTimeMillis();
        double rate = (double) throughput / delta;
        logger.info("wrote {} {} messages in {}s, ({}/s) ", throughput, shortType, delta / 1000, (long) rate);
    }
}
