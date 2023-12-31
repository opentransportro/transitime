/* (C)2023 */
package org.transitclock.avl;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.transitclock.db.structs.AvlReport;

/**
 * A queue of {@link AvlReportProcessor} runnables that can be used with a ThreadPoolExecutor. Implements by
 * subclassing a ArrayBlockingQueue<Runnable> where the Runnable is an AvlClient. Also keeps track
 * of the last AVL report per vehicle. When getting data from queue, if the data is obsolete (a new
 * AVL report has been received for the vehicle) then that element from the queue is thrown out and
 * the next item is retrieved until a non-obsolete one is found.
 *
 * <p>Extended ArrayBlockingQueue class so that don't have to create a blocking queue from scratch.
 *
 * <p>Note: wanted to extend from ArrayBlockingQueue<AvlClient> but that didn't work for the
 * ThreadPoolExecutor which expects a BlockingQueue<Runnable>. So had to resort to doing ugly casts.
 *
 * @author SkiBu Smith
 */
@Slf4j
public class AvlReportProcessorQueue extends ArrayBlockingQueue<AvlReportProcessor> {

    // For keeping track of the last AVL report for each vehicle. Used to
    // determine if AVL report from queue is obsolete.
    private final ConcurrentHashMap<String, AvlReport> avlDataPerVehicleMap = new ConcurrentHashMap<>();

    /**
     * Constructs the queue to have specified size.
     *
     * @param queueSize How many AVL elements can be put into the queue before it blocks.
     */
    public AvlReportProcessorQueue(int queueSize) {
        super(queueSize);
    }

    /**
     * Adds the AVL report to the map of last AVL report for each vehicle
     *
     * @param runnable the AvlClient
     */
    private void addToAvlDataPerVehicleMap(AvlReportProcessor runnable) {
        if (runnable == null)
            throw new IllegalArgumentException("Runnable must be AvlClient.");

        AvlReport avlReport = runnable.getAvlReport();
        avlDataPerVehicleMap.put(avlReport.getVehicleId(), avlReport);
    }

    /**
     * Returns true if the AVL report is older than the latest one for the vehicle and is therefore
     * obsolete and doesn't need to be processed.
     *
     * @param runnableFromQueue {@link AvlReportProcessor} from the queue containing an AvlReport
     * @return true of obsolete
     */
    private boolean isObsolete(AvlReportProcessor runnableFromQueue) {
        if (runnableFromQueue == null)
            throw new IllegalArgumentException("Runnable must be AvlClient.");

        AvlReport avlReportFromQueue = runnableFromQueue.getAvlReport();

        AvlReport lastAvlReportForVehicle = avlDataPerVehicleMap.get(avlReportFromQueue.getVehicleId());
        boolean obsolete = lastAvlReportForVehicle != null && avlReportFromQueue.getTime() < lastAvlReportForVehicle.getTime();
        if (obsolete) {
            logger.debug(
                    "AVL report from queue is obsolete (there is a newer "
                            + "one for the vehicle). Therefore ignoring this report so "
                            + "can move on to next valid report for another vehicle. "
                            + "From queue {}. Last AVL report in map {}. Size of queue "
                            + "is {}",
                    avlReportFromQueue,
                    lastAvlReportForVehicle,
                    size());
        }
        return obsolete;
    }

    /**
     * Calls superclass add() method but also updates the AVL data per vehicle map. Doesn't seem to
     * be used by ThreadPoolExecutor but still included for completeness.
     */
    @Override
    public boolean add(@NonNull AvlReportProcessor runnable) {
        addToAvlDataPerVehicleMap(runnable);
        return super.add(runnable);
    }

    /**
     * Calls superclass put() method but also updates the AVL data per vehicle map. Doesn't seem to
     * be used by ThreadPoolExecutor but still included for completeness.
     */
    @Override
    public void put(@NonNull AvlReportProcessor runnable) throws InterruptedException {
        super.put(runnable);
        addToAvlDataPerVehicleMap(runnable);
    }

    /**
     * Calls superclass offer() method but also updates the AVL data per vehicle map. Used by
     * ThreadPoolExecutor.
     */
    @Override
    public boolean offer(@NonNull AvlReportProcessor runnable) {
        AvlReport avlReport = runnable.getAvlReport();
        logger.debug("offer() remainingCapacity={} {}", remainingCapacity(), avlReport);

        boolean successful = super.offer(runnable);
        if (successful) addToAvlDataPerVehicleMap(runnable);

        logger.debug("offer() returned {} for {}", successful, avlReport);
        return successful;
    }

    /**
     * Calls superclass offer(timeout, unit) method but also updates the AVL data per vehicle map.
     * Doesn't seem to be used by ThreadPoolExecutor but still included for completeness.
     */
    @Override
    public boolean offer(AvlReportProcessor runnable, long timeout, TimeUnit unit) throws InterruptedException {
        boolean successful = super.offer(runnable, timeout, unit);
        if (successful) addToAvlDataPerVehicleMap(runnable);

        return successful;
    }

    /**
     * Calls superclass poll() method until it gets AVL data that is not obsolete. Doesn't seem to
     * be used by ThreadPoolExecutor but still included for completeness.
     */
    @Override
    public AvlReportProcessor poll() {
        AvlReportProcessor runnable;
        do {
            runnable = super.poll();
        } while (isObsolete(runnable));
        return runnable;
    }

    /**
     * Calls superclass poll(timeout, unit) method until it gets AVL data that is not obsolete. Used
     * by ThreadPoolExecutor.
     */
    @Override
    public AvlReportProcessor poll(long timeout, TimeUnit unit) throws InterruptedException {
        logger.debug("In poll(t,u) timeout={} units={}", timeout, unit);

        AvlReportProcessor runnable;
        do {
            runnable = super.poll(timeout, unit);
        } while (runnable != null && isObsolete(runnable));

        if (runnable != null) {
            logger.debug("poll(t,u) in AvlQueue returned {}", ((AvlReportProcessor) runnable).getAvlReport());
        }
        return runnable;
    }

    /**
     * Calls superclass take() method until it gets AVL data that is not obsolete. Doesn't seem to
     * be used by ThreadPoolExecutor but still included for completeness.
     */
    @Override
    @NonNull
    public AvlReportProcessor take() throws InterruptedException {
        AvlReportProcessor runnable;
        do {
            runnable = super.take();
        } while (isObsolete(runnable));
        return runnable;
    }
}
