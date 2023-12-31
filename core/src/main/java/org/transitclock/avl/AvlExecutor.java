/* (C)2023 */
package org.transitclock.avl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.configData.AgencyConfig;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.utils.threading.NamedThreadFactory;

/**
 * A singleton thread executor for executing AVL reports. For when not using JMS to handle queue of
 * AVL reports. One can dump AVL reports into this executor and then have them be executed, possibly
 * using multiple threads. The number of threads is specified using the Java property
 * transitclock.avl.numThreads . The queue size is set using the Java property
 * transitclock.avl.queueSize .
 *
 * <p>Causes AvlClient.run() to be called on each AvlReport, unless using test executor, in which
 * case the AvlClientTester() is called.
 *
 * @author SkiBu Smith
 */
@Slf4j
public class AvlExecutor {

    private static final IntegerConfigValue avlQueueSize = new IntegerConfigValue(
            "transitclock.avl.queueSize",
            2000,
            "How many items to go into the blocking AVL queue "
                    + "before need to wait for queue to have space. Should "
                    + "be approximately 50% more than the number of reports "
                    + "that will be read during a single AVL polling cycle. "
                    + "If too big then wasteful. If too small then not all the "
                    + "data will be rejected by the ThreadPoolExecutor. ");

    private static IntegerConfigValue numAvlThreads = new IntegerConfigValue(
            "transitclock.avl.numThreads",
            1,
            "How many threads to be used for processing the AVL "
                    + "data. For most applications just using a single thread "
                    + "is probably sufficient and it makes the logging simpler "
                    + "since the messages will not be interleaved. But for "
                    + "large systems with lots of vehicles then should use "
                    + "multiple threads, such as 3-15 so that more of the cores "
                    + "are used.");

    private static final int MAX_THREADS = 25;
    private static AvlExecutor singleton;
    private final ThreadPoolExecutor avlClientExecutor;
    private final AvlReportProcessorFactory avlReportProcessorFactory;

    public AvlExecutor(AvlReportProcessorFactory avlReportProcessorFactory) {
        this.avlReportProcessorFactory = avlReportProcessorFactory;

        int numberThreads = numAvlThreads.getValue();
        final int maxAVLQueueSize = avlQueueSize.getValue();

        // Make sure that numberThreads is reasonable
        if (numberThreads < 1) {
            logger.error("Number of threads must be at least 1 but {} was " + "specified. Therefore using 1 thread.", numberThreads);
            numberThreads = 1;
        }

        if (numberThreads > MAX_THREADS) {
            logger.error("Number of threads must be no greater than {} but {} was specified. Therefore using {} threads.", MAX_THREADS, numberThreads, MAX_THREADS);
            numberThreads = MAX_THREADS;
        }

        logger.info("Starting AvlExecutor for directly handling AVL reports via a queue instead of JMS. maxAVLQueueSize={} and numberThreads={}", maxAVLQueueSize, numberThreads);

        AvlReportProcessorQueue workQueue = new AvlReportProcessorQueue(maxAVLQueueSize);
        NamedThreadFactory avlClientThreadFactory = new NamedThreadFactory("avlClient");

        // Called when queue fills up
        RejectedExecutionHandler rejectedHandler = (arg0, arg1) -> {
            logger.error("Rejected AVL report {} in AvlExecutor for agencyId={}. The work queue with capacity {}  must be full.",
                    ((AvlReportProcessor) arg0).getAvlReport(),
                    AgencyConfig.getAgencyId(),
                    maxAVLQueueSize);
        };

        avlClientExecutor = new ThreadPoolExecutor(1,
                numberThreads,
                1,
                TimeUnit.HOURS,
                (BlockingQueue) workQueue,
                avlClientThreadFactory,
                rejectedHandler);
    }

    /**
     * Returns singleton instance. Not synchronized since it is OK if an executor is replaced by a
     * new one.
     *
     * @return the singleton AvlExecutor
     */
    public static synchronized AvlExecutor getInstance() {
        if (singleton == null) {
            singleton = new AvlExecutor(new DefaultAvlReportProcessorFactory());
        }
        return singleton;
    }

    /**
     * Instead of writing AVL report to JMS topic this method directly processes it. By doing this
     * one can bypass the need for a JMS server. Uses a thread executor so that can both use
     * multiple threads and queue up requests. This is especially important if getting a dump of AVL
     * data from either polling a feed or from an AVL feed hitting the Transitime web server and the
     * AVL data getting then pushed to the core system in batches.
     *
     * <p>Uses a queue so that if system gets behind in processing AVL data then AVL data is written
     * to a queue that keeps track of the latest AVL report per vehicle. If another AVL report is to
     * be added to the queue then the previous one is removed since there is no point processing an
     * old AVL report for a vehicle when new data is available.
     *
     * <p>Causes AvlClient.run() to be called on each AvlReport, unless using test executor, in
     * which case the AvlClientTester() is called.
     *
     * @param avlReport The AVL report to be processed
     */
    public void processAvlReport(AvlReport avlReport) {
        AvlReportProcessor client = avlReportProcessorFactory.createClient(avlReport);
        avlClientExecutor.execute(client);
    }
}
